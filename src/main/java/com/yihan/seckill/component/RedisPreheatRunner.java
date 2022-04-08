package com.yihan.seckill.component;

import com.yihan.seckill.db.dao.SeckillActivityDao;
import com.yihan.seckill.db.po.SeckillActivity;
import com.yihan.seckill.services.RedisService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.annotation.Resource;
import java.util.List;

public class RedisPreheatRunner implements ApplicationRunner {

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);

        for (SeckillActivity seckillActivity : seckillActivities) {
            redisService.setValue("stock:" + seckillActivity.getId(), (long) seckillActivity.getAvailableStock());
        }

    }
}
