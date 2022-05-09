package com.banana.oauth2.server.config;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    //注入redis连接工厂
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    //RedisTokenStore用于将token存储至redis
    @Bean
    public RedisTokenStore redisTokenStore(){
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        //设置redis key的层级前缀
        redisTokenStore.setPrefix("TOKEN:");
        return redisTokenStore;
    }

    //初始化密码的编码器
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new PasswordEncoder() {
            //加密
            @Override
            public String encode(CharSequence charSequence) {
                return DigestUtil.md5Hex(charSequence.toString());
            }
            //校验
            @Override
            public boolean matches(CharSequence charSequence, String encoderPassword) {
                return DigestUtil.md5Hex(charSequence.toString()).equals(encoderPassword);
            }
        };
    }
    //初始化认证管理对象
    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    //放行和认证规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/oauth/**", "/actuator/**").permitAll()
                .and()
                .authorizeRequests()
                .anyRequest().authenticated();
    }
}
