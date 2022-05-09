package com.banana.commons.model.domain;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class SignInIdentity implements UserDetails {
    private Integer id;
    private String username;
    private String nickname;
    private String password;
    private String phone;
    private String email;
    private String avatarUrl;
    private String roles;
    private int isValid;
    private List<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (StrUtil.isNotBlank(this.roles)) {
            //获取数据库角色信息
            Lists.newArrayList();
            List<GrantedAuthority> list = new ArrayList<>();
            for (String role : this.roles.split(",")) {
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
                list.add(simpleGrantedAuthority);
            }
            this.authorities = list;
        } else {
            this.authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
        }
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isValid != 0;
    }
}
