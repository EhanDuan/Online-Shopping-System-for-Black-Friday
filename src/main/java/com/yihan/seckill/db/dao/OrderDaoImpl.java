package com.yihan.seckill.db.dao;

import com.yihan.seckill.db.mappers.SeckillOrderMapper;
import com.yihan.seckill.db.po.SeckillOrder;

import javax.annotation.Resource;

public class OrderDaoImpl implements OrderDao{

    @Resource
    private SeckillOrderMapper mapper;

    @Override
    public void insertOrder(SeckillOrder order) {
        mapper.insert(order);
    }

    @Override
    public SeckillOrder queryOrder(String orderNo) {
        return mapper.selectByOrderNo(orderNo);
    }

    @Override
    public void updateOrder(SeckillOrder order) {
        mapper.updateByPrimaryKey(order);
    }
}
