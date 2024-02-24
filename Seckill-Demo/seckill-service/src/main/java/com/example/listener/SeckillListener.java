package com.example.listener;

import com.example.service.GoodsService;
import com.example.service.OrderService;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
@RocketMQMessageListener(topic = "seckillTopic",
        consumerGroup = "seckill-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadNumber = 40
)
public class SeckillListener implements RocketMQListener<MessageExt> {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 方案一(有问题的方案):
     * 扣减库存
     * 写订单表
     * @param message
     */
    public void onMessageA(MessageExt message) {
        System.out.println("开始消费onMessageA");
        String msg = new String(message.getBody());
        // userId + "-" + goodsId
        Integer userId = Integer.parseInt(msg.split("-")[0]);
        Integer goodsId = Integer.parseInt(msg.split("-")[1]);
        // 方案一: 再事务外面加锁 可以实现线程安全  没法集群
        // jvm  EntrySet WaitSet
        synchronized (this) {
            goodsService.realSeckillCaseA(userId, goodsId);
        }
        /**
         * 问题一:存在并发问题 需要加锁
         * 消费者端使用的是并发消费模式,40个线程去执行消费这些消息,
         * 某一时刻假设库存此时剩余2,A B线程同时执行查询商品的操作,然后取出库存2,扣减-1,然后更新到数据库,那么最终生成了2份订单,但是库存还剩余1
         *
         *
         *
         * 问题二: 加锁位置问题, 加在事务内  还是加在事务外?  加在事务外部
         * 若加载事务内  库存1000  则如下:
         * -------
         * 开始事务
         * 加锁
         * 执行业务代码
         * 释放
         * 提交事务
         * ----------
         * 假设1: mysql事务隔离级别为默认  可重复读(mysql默认) RR
         * 因为是先开事务在开锁,AB两个线程同时开启了事务,对于A和B来说,此时数据库中数据 相当于每人都拿取了一份镜像 两份都一样
         * 此时假设A抢到锁,然后执行业务代码,库存数据修改(-1),无论最终A线程事务提交后B线程去执行,还是没有提交B就抢到了锁资源进行执行,对于B来说
         * 它拿的数据始终是开启事务那一刻从数据库中拿到的镜像数据,还是1000
         *
         * 假设2: mysql事务隔离级别设置为读提交RC(一个事务内,可以读取都另外一个事务提交的数据)
         * 这种也不行,假设A线程释放锁之后,还没来得及提交事务,此时B获取了锁资源,进行数据读取,那么因为A线程的数据还没有
         * 提交到数据库,所以B线程在执行业务代码时拿到的数据也是没有被修改的数据
         *
         * 问题三:集群问题
         * 即使做到了线程安全(单机版没问题),但无法做集群 ,多台机器JVM不共享, synchronized (this) {锁不住,真正业务场景下
         * 肯定消费者是多台机器集群部署,然后去消费消息的
         */
    }


    /**
     * 方案二  分布式锁  mysql(行锁)   不适合并发较大场景
     * @param message
     */
    public void onMessageB(MessageExt message) {
        System.out.println("开始消费onMessageB");
        String msg = new String(message.getBody());
        // userId + "-" + goodsId
        Integer userId = Integer.parseInt(msg.split("-")[0]);
        Integer goodsId = Integer.parseInt(msg.split("-")[1]);
        goodsService.realSeckillCaseB(userId, goodsId);
        /**
         * 行锁(innodb)方案 mysql  能够解决分布式情况的数据安全问题  但不适合用于并发量特别大的场景
         * 因为压力最终都在数据库承担
         */
    }


    /**
     * 如果不想写死循环 这里可以设置大一点的时间 其实我觉得while(true)都一样
     * 因为值太小的话,理论上存在一种极端情况,就是某个线程自旋到最后
     * 级即当currentThreadTime<ZX_TIME 不成立时
     * 都没有执行业务代码,那么就会丢失一条订单信息,这肯定是不行的,所以为了保证每个线程都确保执行一次业务代码(这些消息对应是秒杀情况下的订单)
     * 还是写while(true)吧
     */

    int ZX_TIME = 20000;
    /**
     * 方案三: redis setnx 分布式锁  压力会分摊到redis和程序中执行  缓解db的压力
     * @param message
     */
    @Override
    public void onMessage(MessageExt message) {
        System.out.println("开始消费onMessageC");
        String msg = new String(message.getBody());
        Integer userId = Integer.parseInt(msg.split("-")[0]);
        Integer goodsId = Integer.parseInt(msg.split("-")[1]);
        int currentThreadTime = 0;
        //while (currentThreadTime<<ZX_TIME) {
        // 线程自旋 为了让没有抢到锁的线程自旋尝试
        while (true) {
            // 这里给一个key的过期时间,可以避免死锁的发生
            /**
             * 分布式锁一定要加过期时间
             * Duration.ofSeconds(30) 这个时间可以设置的很大,但是不能设置的很小,一般要大于业务代码执行时间
             * 假设goodsService.realSeckillCaseC(userId, goodsId);执行完成平均需要2秒,那么设置过期时间一定要>2秒,否则锁不住,引起线程安全问题
             * 可以了解一下 Redis：Redisson分布式锁的锁续期原理
             * 首先它对锁的控制不是起决定性作用,因为正常情况下,锁都是在finally中手动删除的
             * 如果程序在运行期间，机器突然挂了(当前机器中的A线程在执行的过程中卡住了)，代码层面没有走到 finally 代码块，即在宕机前，锁并没有被删除掉
             * 那么没有过期时间的话,锁得不到释放,导致的结果就是别的正常的机器都无法执行消费功能,所以分布式锁一定要加过期时间,时间可以设置的大一点都无所谓
             */
            Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock:" + goodsId, "", Duration.ofSeconds(30));
            if (flag) {
                // 拿到锁成功
                try {
                    goodsService.realSeckillCaseC(userId, goodsId);
                    return;
                } finally {
                    // 删除 一定要写在finally中,确保锁一定被释放
                    redisTemplate.delete("lock:" + goodsId);
                }
            } else {
                currentThreadTime += 200;
                try {
                    // 没有抢到锁,休眠一会在尝试抢,直到抢到为止,因为唯一的出口在if分支内的return处
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
