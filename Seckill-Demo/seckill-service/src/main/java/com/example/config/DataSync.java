package com.example.config;

import com.example.domain.Goods;
import com.example.mapper.GoodsMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 库存同步方法
 */
@Component
public class DataSync {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 我希望这个方法再项目启动以后
     * 并且再这个类的属性注入完毕以后执行
     * bean生命周期了
     * 实例化 new
     * 属性赋值
     * 初始化  (前PostConstruct/中InitializingBean/后BeanPostProcessor)
     * 使用
     * 销毁
     */
    @PostConstruct
    public void initData() {
        List<Goods> goodsList = goodsMapper.selectSeckillGoods();
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        goodsList.forEach(goods -> {
            redisTemplate.opsForValue().set("goodsId:" + goods.getGoodsId(), goods.getTotalStocks().toString());
        });
    }


/*    @Scheduled(cron = "0 0 10 0 0 ?")
    public void initData(){

    }*/

}
