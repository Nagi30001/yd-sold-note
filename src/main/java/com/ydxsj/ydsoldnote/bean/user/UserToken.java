package com.ydxsj.ydsoldnote.bean.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToken {

    private Integer userId;
    private String token;
    private Long expireTime;
    private Long updateTime;
}
