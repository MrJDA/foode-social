package com.banana.oauth2.server.service;

import cn.hutool.core.bean.BeanUtil;
import com.banana.commons.model.domain.SignInIdentity;
import com.banana.commons.model.pojo.Diners;
import com.banana.commons.utils.AssertUtil;
import com.banana.oauth2.server.mapper.DinersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class UserService implements UserDetailsService {

    @Resource
    private DinersMapper dinersMapper;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        AssertUtil.isNotEmpty(account, "请输入用户名");
        Diners diners = dinersMapper.selectByAccountInfo(account);
        if(Objects.isNull(diners)){
            throw new UsernameNotFoundException("用户名或密码错误,请重新输入");
        }
        SignInIdentity signInIdentity = new SignInIdentity();
        BeanUtils.copyProperties(diners, signInIdentity);
        return signInIdentity;
    }
}
