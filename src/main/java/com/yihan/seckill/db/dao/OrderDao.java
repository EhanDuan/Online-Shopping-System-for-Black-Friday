package com.yihan.seckill.db.dao;

import com.yihan.seckill.db.po.SeckillOrder;

public interface OrderDao {

    void insertOrder(SeckillOrder order);

    SeckillOrder queryOrder(String orderNo);

    void updateOrder(SeckillOrder order);


}
