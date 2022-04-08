package com.yihan.seckill.db.mappers;

import com.yihan.seckill.db.po.SeckillOrder;

public interface SeckillOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SeckillOrder record);

    int insertSelective(SeckillOrder record);

    SeckillOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SeckillOrder record);

    int updateByPrimaryKey(SeckillOrder record);

    SeckillOrder selectByOrderNo(String orderNo);
}