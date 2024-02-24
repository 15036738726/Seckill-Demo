package com.example.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.example.domain.Order;
import com.example.mapper.OrderMapper;
import com.example.service.OrderService;
/**
 * @Author: DLJD
 * @Date:   2023/4/24
 */
@Service
public class OrderServiceImpl implements OrderService{

    @Resource
    private OrderMapper orderMapper;

    @Override
    public int insert(Order record) {
        return orderMapper.insert(record);
    }

}
