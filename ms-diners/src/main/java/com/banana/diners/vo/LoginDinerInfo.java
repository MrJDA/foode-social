package com.banana.diners.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LoginDinerInfo implements Serializable {

    private String nickname;
    private String token;
    private String avatarUrl;

}