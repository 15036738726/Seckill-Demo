package com.example.service;

import com.example.domain.Goods;

/**
 * @Author: DLJD
 * @Date: 2023/4/24
 */
public interface GoodsService {

    int insert(Goods record);


    /**
     * 真正处理秒杀的业务
     * @param userId
     * @param goodsId
     */
    void realSeckillCaseA(Integer userId, Integer goodsId);

    void realSeckillCaseB(Integer userId, Integer goodsId);

    void realSeckillCaseC(Integer userId, Integer goodsId);
}
