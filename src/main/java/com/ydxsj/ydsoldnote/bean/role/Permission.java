package com.ydxsj.ydsoldnote.bean.role;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {

    private Integer id;
    private String permissionNum;
    private String permissionModule;
    private String permissionList;
    private String permissionFunction;


}
