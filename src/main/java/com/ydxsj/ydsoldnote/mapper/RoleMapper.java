package com.ydxsj.ydsoldnote.mapper;


import com.ydxsj.ydsoldnote.bean.role.RolePermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper {

    /**
     * 根据角色编码查询角色的权限编码
     * @param roleNum
     * @return
     */
    List<RolePermission> getPermissionByUserRole(String roleNum);
}
