package com.ydxsj.ydsoldnote.bean.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission {

    private Integer id;
    private Integer roleId;
    private String permissionId;
}
