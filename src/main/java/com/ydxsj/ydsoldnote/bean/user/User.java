package com.ydxsj.ydsoldnote.bean.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private Integer jobNum;
    private String jobPassword;
    private String userName;
    private String phone;
    private String province;
    private String city;
    private String roleNum;
    private Long createTime;
    private String createUser;
    private String address;
    private Integer status;
    private String beProvince;
}
