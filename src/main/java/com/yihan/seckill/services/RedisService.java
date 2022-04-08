package com.yihan.seckill.services;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;

@Service
public class RedisService {

    @Resource
    private JedisPool jedisPool;

    public RedisService setValue(String key, Long value){
        Jedis client = jedisPool.getResource();
        client.set(key, value.toString());
        client.close();
        return this;
    }


    public RedisService setValue(String key, String value){
        Jedis client = jedisPool.getResource();
        client.set(key, value);
        client.close();
        return this;
    }


    public String getValue(String key){
        Jedis client = jedisPool.getResource();
        String value = client.get(key);
        client.close();
        return value;
    }

    public boolean stockDeductValidation(String key){
        try(Jedis client = jedisPool.getResource()){
            // Do not know why this lua error
            String script = "if redis.call('exists', KEYS[1]) == 1 then\n" +
                    "    local stock = tonumber(redis.call('get', KEYS[1]))\n" +
                    "    if (stock <= 0) then\n" +
                    "        return -1\n" +
                    "    end; \n" +
                    "\n" +
                    "    redis.call('decr', KEYS[1]);\n" +
                    "    return stock - 1;\n" +
                    "end;\n" +
                    "\n" +
                    "return -1;";
//            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
//                    "                 local stock = tonumber(redis.call('get', KEYS[1]))\n" +
//                    "                 if( stock <=0 ) then\n" +
//                    "                    return -1\n" +
//                    "                 end;\n" +
//                    "                 redis.call('decr',KEYS[1]);\n" +
//                    "                 return stock - 1;\n" +
//                    "             end;\n" +
//                    "             return -1;";
            Long stock = (Long) client.eval(script, Collections.singletonList(key), Collections.emptyList());

            if(stock < 0){
                System.out.println("库存不足");
                return false;
            }

            System.out.println("恭喜抢购成功");
            return true;
        }catch (Throwable throwable){
            System.out.println("库存扣减失败" + throwable.toString());
            return false;
        }
    }

    /**
     * 获取分布式锁
     * @param lockKey
     * @param requestId
     * @param expireTime
     * @return
     */
    public boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime){
        Jedis jedisClient = jedisPool.getResource();
        String result = jedisClient.set(lockKey, requestId, "NX", "PX", expireTime);
        if("OK".equals(result)){
            return true;
        }

        return false;
    }

    public boolean releaseDistributedLock(String lockKey, String requestId){
        Jedis jedisClient = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) jedisClient.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if(result == 1L){
            return true;
        }else{
            return false;
        }
    }
}
