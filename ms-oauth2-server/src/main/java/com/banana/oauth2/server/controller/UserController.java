package com.banana.oauth2.server.controller;

import cn.hutool.core.bean.BeanUtil;
import com.banana.commons.model.domain.ResultInfo;
import com.banana.commons.model.domain.SignInIdentity;
import com.banana.commons.model.vo.SignInDinerInfo;
import com.banana.commons.utils.ResultInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
public class UserController {

    @Resource
    private RedisTokenStore redisTokenStore;
    @Resource
    private HttpServletRequest httpServletRequest;

    @GetMapping("user/me")
    public ResultInfo getCurrentUser(Authentication authentication){
        SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
        SignInDinerInfo signInDinerInfo = new SignInDinerInfo();
        BeanUtil.copyProperties(signInIdentity, signInDinerInfo);
        return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), signInDinerInfo);
    }

    @GetMapping("user/logout")
    public ResultInfo userLogout(String access_token, String authorization){
        if(StringUtils.isBlank(access_token)){
            access_token = authorization;
        }
        if(StringUtils.isBlank(access_token)){
            return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), "退出成功");
        }
        if(access_token.toLowerCase().contains("bearer ".toLowerCase())){
            access_token = access_token.toLowerCase().replace("bearer ", "");
        }
        OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
        if(Objects.nonNull(oAuth2AccessToken)){
            redisTokenStore.removeAccessToken(access_token);
            OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(refreshToken);
        }
        return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), "退出成功");
    }
}
