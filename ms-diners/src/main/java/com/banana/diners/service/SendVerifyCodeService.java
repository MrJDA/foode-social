package com.banana.diners.service;

import cn.hutool.core.util.RandomUtil;
import com.banana.commons.constant.RedisKeyConstant;
import com.banana.commons.utils.AssertUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class SendVerifyCodeService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public void send(String phone){
        //检查非空
        AssertUtil.isNotEmpty(phone, "手机号码不能为空");
        //查询是否已经生成验证码
        if(!isExpired(phone)){
            return;
        }
        //生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        //调用短信服务发送短信
        //发送成功保存验证码到redis, 并设置失效时间
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        redisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
    }

    private boolean isExpired(String phone) {
       String code = getCodeByPhone(phone);
        return StringUtils.isBlank(code);
    }

    public String getCodeByPhone(String phone){
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        String code = redisTemplate.opsForValue().get(key);
        return code;
    }
}
