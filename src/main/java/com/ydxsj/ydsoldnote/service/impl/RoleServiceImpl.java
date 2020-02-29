package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.role.RolePermission;
import com.ydxsj.ydsoldnote.mapper.RoleMapper;
import com.ydxsj.ydsoldnote.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<RolePermission> getPermissionByUserRole(String roleNum) {

//        List<RolePermission> rolePermissions = roleMapper.getPermissionByUserRole(roleNum);

        return null;
//        return rolePermissions;
    }
}
