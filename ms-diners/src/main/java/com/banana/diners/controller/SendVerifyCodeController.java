package com.banana.diners.controller;

import com.banana.commons.model.domain.ResultInfo;
import com.banana.commons.utils.ResultInfoUtil;
import com.banana.diners.service.SendVerifyCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class SendVerifyCodeController {

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    @Resource
    private HttpServletRequest httpServletRequest;

    @GetMapping("verifycodes")
    public ResultInfo send(String phone){
        sendVerifyCodeService.send(phone);
        return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), "验证码发送成功");
    }
}
