package com.ydxsj.ydsoldnote.service;


import com.ydxsj.ydsoldnote.bean.role.RolePermission;

import java.util.List;

public interface RoleService {

    /**
     * 根据用户角色获取用户具体的权限列表
     * @param roleNum
     * @return
     */
    List<RolePermission> getPermissionByUserRole(String roleNum);
}
