package com.yihan.seckill.db.dao;

import com.yihan.seckill.db.mappers.SeckillCommodityMapper;
import com.yihan.seckill.db.po.SeckillCommodity;
import com.yihan.seckill.db.mappers.SeckillCommodityMapper;
import com.yihan.seckill.db.po.SeckillCommodity;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SeckillCommodityDaoImpl implements SeckillCommodityDao {

    @Resource
    private SeckillCommodityMapper seckillCommodityMapper;

    @Override
    public SeckillCommodity querySeckillCommodityById(long commodityId) {
        return seckillCommodityMapper.selectByPrimaryKey(commodityId);
    }
}
