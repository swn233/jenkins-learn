package com.example.backend.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate template;

    //发送验证码的冷却时间限制
    //return：是否可以发送
    public boolean limitOnceCheck(String key,int blockTime){
        if(Boolean.TRUE.equals(template.hasKey(key))){
            return false;
        } else {
          template.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
