package com.banana.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.banana.commons.constant.ApiConstant;
import com.banana.commons.model.domain.ResultInfo;
import com.banana.commons.model.dto.DinersDTO;
import com.banana.commons.model.pojo.Diners;
import com.banana.commons.utils.AssertUtil;
import com.banana.commons.utils.ResultInfoUtil;
import com.banana.diners.config.Oauth2ClientConfiguration;
import com.banana.diners.domain.OAuthDinerInfo;
import com.banana.diners.mapper.DinnersMapper;
import com.banana.diners.vo.LoginDinerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Objects;

@Service
public class DinersService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;

    @Resource
    private Oauth2ClientConfiguration oauth2ClientConfiguration;

    @Resource
    private DinnersMapper dinnersMapper;

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    public ResultInfo register(DinersDTO dinersDTO, String path){
        //参数校验
        String phone = dinersDTO.getPhone();
        AssertUtil.isNotEmpty(phone, "手机号不为空");
        String verifyCode = dinersDTO.getVerifyCode();
        AssertUtil.isNotEmpty(verifyCode, "验证码不为空");
        String username = dinersDTO.getUsername();
        AssertUtil.isNotEmpty(dinersDTO.getUsername(), "用户名不为空");
        String password = dinersDTO.getPassword();
        AssertUtil.isNotEmpty(password, "密码不为空");
        //验证码校验
        String code = sendVerifyCodeService.getCodeByPhone(phone);
        AssertUtil.isNotEmpty(code, "验证码已过期, 请重新发送");
        AssertUtil.isTrue(!code.equals(verifyCode), "验证码错误");
        //用户是否已经注册
        Diners diners = dinnersMapper.selectByUsername(username.trim());
        AssertUtil.isTrue(Objects.nonNull(diners), "用户名已存在");
        //密码加密
        dinersDTO.setPassword(DigestUtil.md5Hex(password.trim()));
        //注册
        dinnersMapper.save(dinersDTO);
        //自动登录
        return signIn(phone, password, path);
    }

    public void phoneIsRegistered(String phone){
        AssertUtil.isNotEmpty(phone, "手机号不能为空");
        Diners diners = dinnersMapper.selectByPhone(phone);
        AssertUtil.isNotNull(diners, "手机号已注册");
        AssertUtil.isTrue(diners.getIsValid() == 0, "用户已注销");
    }

    public ResultInfo signIn(String account, String password, String path){
        //参数校验
        AssertUtil.isNotEmpty(account, "请输入登录账号");
        AssertUtil.isNotEmpty(password, "请输入登录密码");
        //构建请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        body.setAll(BeanUtil.beanToMap(oauth2ClientConfiguration));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
        //设置Authorization
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(oauth2ClientConfiguration.getClientId(),
                oauth2ClientConfiguration.getSecret()));
        //发送请求
        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(oauthServerName + "/oauth/token", httpEntity, ResultInfo.class);
        //处理返回结果
        AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK, "登录失败");
        ResultInfo resultInfo = result.getBody();
        if(resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        OAuthDinerInfo oAuthDinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap)resultInfo.getData()
                , new OAuthDinerInfo(), false);
        LoginDinerInfo loginDinerInfo = LoginDinerInfo.builder()
                .nickname(oAuthDinerInfo.getNickname())
                .avatarUrl(oAuthDinerInfo.getAvatarUrl())
                .token(oAuthDinerInfo.getAccessToken())
                .build();
        return ResultInfoUtil.buildSuccess(path, loginDinerInfo);
    }
}
