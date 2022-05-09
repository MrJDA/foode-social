package com.banana.oauth2.server.config;

import com.banana.commons.model.domain.SignInIdentity;
import com.banana.oauth2.server.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private ClientOAuth2DataConfiguration clientOAuth2DataConfiguration;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private RedisTokenStore redisTokenStore;

    @Resource
    private UserService userService;

     //配置令牌端点安全约束
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许访问token的公钥，默认情况下 /oauth/token_key是受保护的
        security.tokenKeyAccess("permitAll()")
                //允许检查token的状态, 默认 /oauth/check_token是受保护的
                .checkTokenAccess("permitAll()");
    }

    //客户端配置-授权模型
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(clientOAuth2DataConfiguration.getClientId())
                .secret(passwordEncoder.encode(clientOAuth2DataConfiguration.getSecret()))
                .authorizedGrantTypes(clientOAuth2DataConfiguration.getGrantTypes())
                .accessTokenValiditySeconds(clientOAuth2DataConfiguration.getTokenValidityTime())
                .refreshTokenValiditySeconds(clientOAuth2DataConfiguration.getRefreshTokenValidityTime())
                .scopes(clientOAuth2DataConfiguration.getScopes());
    }

    //配置授权及令牌的访问端点和令牌服务
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //认证器
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userService)
                //token存储方式
                .tokenStore(redisTokenStore)
                //令牌增强对象，增强放回结果
                .tokenEnhancer((accessToken, authentication) -> {
                    //获取登录用户信息
                    SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
                    DefaultOAuth2AccessToken defaultOAuth2AccessToken = (DefaultOAuth2AccessToken) accessToken;
                    LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
                    linkedHashMap.put("nickname", signInIdentity.getNickname());
                    linkedHashMap.put("avatarUrl", signInIdentity.getAvatarUrl());
                    defaultOAuth2AccessToken.setAdditionalInformation(linkedHashMap);
                    return defaultOAuth2AccessToken;
                });
    }
}
