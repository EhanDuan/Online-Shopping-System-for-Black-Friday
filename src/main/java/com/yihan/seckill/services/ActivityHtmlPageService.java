package com.yihan.seckill.services;

import com.yihan.seckill.db.dao.SeckillActivityDao;
import com.yihan.seckill.db.dao.SeckillCommodityDao;
import com.yihan.seckill.db.po.SeckillActivity;
import com.yihan.seckill.db.po.SeckillCommodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ActivityHtmlPageService {

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    SeckillCommodityDao seckillCommodityDao;

    /**
     * 创建html页面
     */
    public void createActivityHtml(long seckillActivityId){
        PrintWriter writer = null;





        try{
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
            SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
            //获取页面数据
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("seckillActivity", seckillActivity);
            resultMap.put("seckillCommodity", seckillCommodity);
            resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
            resultMap.put("oldPrice", seckillActivity.getOldPrice());
            resultMap.put("commodityId", seckillActivity.getCommodityId());
            resultMap.put("commodityName", seckillCommodity.getCommodityName());
            resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());

            Context context = new Context();
            context.setVariables(resultMap);

            File file = new File("/Users/duanyihan/IdeaProjects/seckill/src/main/resources/templates" + " seckill_detail_" + seckillActivityId + ".html");
            writer = new PrintWriter(file);
        }catch (Exception e){
            log.error(e.toString());
            log.error("页面静态化出错:" + seckillActivityId);
        }finally {
            if(writer != null){
                writer.close();
            }
        }
    }
}
