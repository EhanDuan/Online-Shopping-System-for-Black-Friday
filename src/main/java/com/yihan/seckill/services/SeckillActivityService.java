package com.yihan.seckill.services;

import com.alibaba.fastjson.JSON;
import com.yihan.seckill.db.dao.OrderDao;
import com.yihan.seckill.db.dao.SeckillActivityDao;
import com.yihan.seckill.db.po.SeckillActivity;
import com.yihan.seckill.db.po.SeckillOrder;
import com.yihan.seckill.mq.RocketMQService;
import com.yihan.seckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.TreeMap;

@Slf4j
@Service
public class SeckillActivityService {
    TreeMap treemap;
    @Resource
    private RedisService redisService;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RocketMQService rocketMQService;

    @Resource
    private OrderDao orderDao;

    private SnowFlake snowFlake = new SnowFlake(1, 1);

    public boolean seckillStockValidator(long activityId){
        String key = "stock:" + activityId;
        return redisService.stockDeductValidation(key);
    }

    public SeckillOrder createOrder(long seckillActivityId, long userId) throws Exception{
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillOrder order = new SeckillOrder();

        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        //发送创建订单的消息
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        return order;
    }


    public void payOrderProcess(String orderNo) {
        SeckillOrder order = orderDao.queryOrder(orderNo);
        boolean deductStockResult = seckillActivityDao.deductStock(order.getSeckillActivityId());

        if(deductStockResult){
            order.setPayTime(new Date());
            //  2 ：成功支付
            order.setOrderStatus(2);
            orderDao.updateOrder(order);
        }
    }
}
