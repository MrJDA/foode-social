package com.banana.commons.model.pojo;

import com.banana.commons.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Diners extends BaseModel {
    private Integer id;
    private String username;
    private String nickname;
    private String password;
    private String phone;
    private String email;
    private String avatarUrl;
    private String roles;
}
