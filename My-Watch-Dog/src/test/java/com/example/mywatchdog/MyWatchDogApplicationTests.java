package com.example.mywatchdog;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MyWatchDogApplicationTests {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 手写看门狗续约机制
     * @throws InterruptedException
     */
    @Test
    void watchDog() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // 设置一个key
        redisTemplate.opsForValue().set("test","--", Duration.ofSeconds(10));
        // 通过该方法实现自动续期  5s续约一次
        reExpire(new HashedWheelTimer());

        // 阻塞主线程,保证子线程正常执行
        countDownLatch.await();
    }

    /**
     * 执行结果:(删除缓存后,停止续期)
     * 执行续约,续约前,val=5
     * 执行续约,续约前,val=5
     * 执行续约,续约前,val=5
     * 执行续约,续约前,val=5
     * 停止续期
     */

    /**
     * 续约方法
     * @param timer
     */
    public void reExpire(Timer timer){

        // 延迟5s后执行
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                long val = expire("test");
                if(val>0){
                    System.out.println("执行续约,续约前,val="+val);
                    // key还存活,续约
                    redisTemplate.opsForValue().set("test","--", Duration.ofSeconds(10));
                    // 递归调用,进行下次续约
                    reExpire(timer);
                }else{
                    // key已经失效,不续约
                    System.out.println("停止续期");
                }
            }
        },5, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 判断key是否过期
     * @param key
     * @return
     */
    public boolean isExpire(String key) {
        return expire(key) > 1?false:true;
    }

    /**
     * 从redis中获取key对应的过期时间;
     * 如果该值有过期时间，就返回相应的过期时间;
     * 如果该值没有设置过期时间，就返回-1;
     * 如果没有该值，就返回-2;
     * @param key
     * @return
     */
    public long expire(String key) {
        return redisTemplate.opsForValue().getOperations().getExpire(key);
    }
}
