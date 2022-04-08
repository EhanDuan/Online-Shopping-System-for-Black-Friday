package com.yihan.seckill.controller;


import com.yihan.seckill.services.SeckillActivityService;
import com.yihan.seckill.services.SeckillOverSellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class SeckillOverSellController {

    @Resource
    private SeckillOverSellService seckillOverSellService;

    @Resource
    private SeckillActivityService seckillActivityService;

//    @ResponseBody
//    @RequestMapping("seckill/{seckillActivityId}")
//    public String seckill(@PathVariable long seckillActivityId){
//        return seckillOverSellService.processSeckill(seckillActivityId);
//    }

    //  使用lua脚本处理请求
    @RequestMapping("/seckill/{seckillActivityId}")
    @ResponseBody
    public String seckillCommodity(@PathVariable long seckillActivityId){
        boolean stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
        return stockValidateResult ? "恭喜你秒杀成功" : "商品已售完，请下次再来";
    }
}
