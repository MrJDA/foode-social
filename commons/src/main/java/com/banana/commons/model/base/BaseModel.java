package com.banana.commons.model.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class BaseModel implements Serializable {
    private Integer id;
    private Date createDate;
    private Date updateDate;
    private int isValid;
}
