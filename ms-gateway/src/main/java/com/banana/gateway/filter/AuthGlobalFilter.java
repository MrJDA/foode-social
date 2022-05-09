package com.banana.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.banana.gateway.component.HandleException;
import com.banana.gateway.config.IgnoreUrlsConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private IgnoreUrlsConfig ignoreUrlsConfig;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private HandleException handleException;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //请求是否在白名单，白名单放行
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        String path = exchange.getRequest().getURI().getPath();
        if(ignoreUrlsConfig.getUrls().contains(path)){
            return chain.filter(exchange);
        }
        //不在白名单，验证token是否可用
        String access_token = exchange.getRequest().getQueryParams().getFirst("access_token");
        if(StringUtils.isBlank(access_token)){
            return handleException.writeError(exchange, "请登录");
        }
        String checkTokenUrl = "http://MS-OAUTH2-SERVER/oauth/check_token?token=".concat(access_token);
        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(checkTokenUrl, String.class);
            if(entity.getStatusCode() != HttpStatus.OK){
                return handleException.writeError(exchange, "Token was not recognized, token:".concat(access_token));
            }
            if(StringUtils.isBlank(entity.getBody())){
                return handleException.writeError(exchange, "This token is invalid:".concat(access_token));
            }
        }catch (Exception e){
            return handleException.writeError(exchange, "Token was not recognised, token".concat(access_token));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
