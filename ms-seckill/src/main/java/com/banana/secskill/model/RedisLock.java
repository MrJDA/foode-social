package com.banana.secskill.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.UUID;

@Getter
@Setter
public class RedisLock {

    private RedisTemplate redisTemplate;
    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Object> unlockScript;

    public RedisLock(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 加载释放锁的脚本
        this.lockScript = new DefaultRedisScript<>();
        this.lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lock.lua")));
        this.lockScript.setResultType(Long.class);
        // 加载释放锁的脚本
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
    }

    public String tryLock(String lockName, long releaseTime) {
        // 存入的线程信息的前缀，防止与其它JVM中线程信息冲突
        String key = UUID.randomUUID().toString();

        // 执行脚本
        Long result = (Long)redisTemplate.execute(
                lockScript,
                Collections.singletonList(lockName),
                key + Thread.currentThread().getId(), releaseTime);

        // 判断结果
        if(result != null && result.intValue() == 1) {
            return key;
        }else {
            return null;
        }
    }

    public void unlock(String lockName, String key) {
        // 执行脚本
        redisTemplate.execute(
                unlockScript,
                Collections.singletonList(lockName),
                key + Thread.currentThread().getId(), null);
    }
}