package com.example.service.impl;


import com.example.domain.Goods;
import com.example.domain.Order;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderMapper;
import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Author: DLJD
 * @Date: 2023/4/24
 */
@Service
public class GoodsServiceImpl implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;

    @Autowired
    private OrderMapper orderMapper;


    @Override
    public int insert(Goods record) {
        return goodsMapper.insert(record);
    }


    /////////////////////////////////

    /**
     * 方案一(有问题的方案):
     * 锁加载调用方法的地方 要加载事务外面
     * @param userId
     * @param goodsId
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // rr
    public void realSeckillCaseA(Integer userId, Integer goodsId) {
        // 扣减库存  插入订单表
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        int finalStock = goods.getTotalStocks() - 1;
        if (finalStock < 0) {
            // 这个地方 如果生产方控制好的话 是不会出现的
            // 只是记录日志 让代码停下来   这里的异常用户无法感知
            throw new RuntimeException("库存不足：" + goodsId);
        }
        goods.setTotalStocks(finalStock);
        goods.setUpdateTime(new Date());
        // update goods set stocks =  1 where id = 1  没有行锁
        int i = goodsMapper.updateByPrimaryKey(goods);
        if (i > 0) {
            Order order = new Order();
            order.setGoodsid(goodsId);
            order.setUserid(userId);
            order.setCreatetime(new Date());
            orderMapper.insert(order);
        }
    }


    /**
     * 行锁(innodb)方案 mysql  不适合用于并发量特别大的场景
     * 因为压力最终都在数据库承担
     * @param userId
     * @param goodsId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void realSeckillCaseB(Integer userId, Integer goodsId) {
        // update goods set total_stocks = total_stocks - 1 where goods_id = goodsId and total_stocks - 1 >= 0;
        // 使用到了mysql的行锁,多个线程并发执行时 通过mysql来进行排队更新 total_stocks = total_stocks - 1 更新的字段自身参与计算 自动使用行锁(需要事务支持)
        // 通过mysql来控制锁
        int i = goodsMapper.updateStock(goodsId);
        if (i > 0) {
            Order order = new Order();
            order.setGoodsid(goodsId);
            order.setUserid(userId);
            order.setCreatetime(new Date());
            orderMapper.insert(order);
        }
    }

    /**
     * 使用redis setnx
     * @param userId
     * @param goodsId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void realSeckillCaseC(Integer userId, Integer goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        int finalStock = goods.getTotalStocks() - 1;
        if (finalStock < 0) {
            throw new RuntimeException("库存不足：" + goodsId);
        }
        goods.setTotalStocks(finalStock);
        goods.setUpdateTime(new Date());
        int i = goodsMapper.updateByPrimaryKey(goods);
        if (i > 0) {
            Order order = new Order();
            order.setGoodsid(goodsId);
            order.setUserid(userId);
            order.setCreatetime(new Date());
            orderMapper.insert(order);
        }
    }
}
