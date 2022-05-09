package com.banana.diners.controller;

import com.banana.commons.model.domain.ResultInfo;
import com.banana.commons.model.dto.DinersDTO;
import com.banana.commons.utils.ResultInfoUtil;
import com.banana.diners.service.DinersService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Api(tags = "食客相关接口")
public class DinersController {
    @Resource
    private DinersService dinersService;

    @Resource
    private HttpServletRequest httpServletRequest;

    @GetMapping("signin")
    public ResultInfo signIn(String account, String password){
        return dinersService.signIn(account, password, httpServletRequest.getServletPath());
    }

    @GetMapping("checkPhone")
    public ResultInfo checkPhone(String phone){
        dinersService.phoneIsRegistered(phone);
        return ResultInfoUtil.buildSuccess(httpServletRequest.getServletPath(), "校验成功");
    }

    @PostMapping("register")
    public ResultInfo register(@RequestBody DinersDTO dinersDTO){
        return dinersService.register(dinersDTO, httpServletRequest.getServletPath());
    }
}
