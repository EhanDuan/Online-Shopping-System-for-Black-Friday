package com.yihan.seckill.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.yihan.seckill.db.dao.OrderDao;
import com.yihan.seckill.db.dao.SeckillActivityDao;
import com.yihan.seckill.db.dao.SeckillCommodityDao;
import com.yihan.seckill.db.po.SeckillActivity;
import com.yihan.seckill.db.po.SeckillCommodity;
import com.yihan.seckill.db.po.SeckillOrder;
import com.yihan.seckill.services.RedisService;
import com.yihan.seckill.services.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
//@RequestMapping("seckillActivity")
public class SeckillActivityController {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private RedisService redisService;



    @RequestMapping("addSeckillActivity")
    public String addSeckillActivity(){
        return "add_activity";
    }


    @RequestMapping("addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
            ) throws ParseException {

        startTime = startTime.substring(0, 10) + startTime.substring(11);
        endTime = endTime.substring(0, 10) + endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");

        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));

        seckillActivityDao.inertSeckillActivity(seckillActivity);

        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap){
        try(Entry entry = SphU.entry("seckills")){
            List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
            resultMap.put("seckillActivities", seckillActivities);
            return "seckill_activity_list";
        }catch(BlockException e){
            log.error("查询秒杀活动的刘表被限流： " + e.toString());
            return "wait";
        }
    }

    /**
     * 定义限流规则
     * 1、创建存放限流规则的集合
     * 2、创建限流规则
     * 3、将限流规则放到集合中
     * 4、加载限流规则
     * @PostConstruct 当前类的构造函数执行完之后执行
     */
    @PostConstruct
    public void seckillsFlowRule(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setRefResource("seckills");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(2);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }


    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(@PathVariable("seckillActivityId") long seckillActivityId, Map<String, Object> resultMap){
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());

        return "seckill_item";
    }

    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(
            @PathVariable long userId,
            @PathVariable long seckillActivityId
    ){
        boolean stockValidateResult = false;

        ModelAndView modelAndView = new ModelAndView();

        try{
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if(stockValidateResult){
                SeckillOrder order = seckillActivityService.createOrder(seckillActivityId, userId);
                modelAndView.addObject("resultInfo", "秒杀成功，订单创建中，订单ID: " + order.getOrderNo());
                modelAndView.addObject("orderNo", order.getOrderNo());
            }else{
                modelAndView.addObject("resultInfo", "对不起，库存不足");
            }

        }catch(Exception exception){
            log.error("秒杀活动异常：" , exception.toString());
            modelAndView.addObject("resultInfo", "秒杀失败");
            exception.printStackTrace();
        }

        modelAndView.setViewName("seckill_result");

        return modelAndView;
    }


    @RequestMapping("seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable long orderNo){
        log.info("订单查询， 订单号：" + orderNo);

        SeckillOrder order = orderDao.queryOrder(orderNo + "");
        ModelAndView modelAndView = new ModelAndView();

        if(order != null){
            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        }else{
            modelAndView.setViewName("order_wait");
        }

        return modelAndView;
    }

    @RequestMapping("/seckill/parOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception{
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }


    public void pushSeckillInfoToRedis(long seckillActivityId){
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:" + seckillActivityId, JSON.toJSONString(seckillActivity));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(), JSON.toJSONString(seckillCommodity));
    }

    @RequestMapping("/seckill/getSystemTime")
    @ResponseBody
    public String getSystemTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        return date;
    }


}
