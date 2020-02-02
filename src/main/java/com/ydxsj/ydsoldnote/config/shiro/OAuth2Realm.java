package com.ydxsj.ydsoldnote.config.shiro;

import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;
import com.ydxsj.ydsoldnote.bean.role.RolePermission;
import com.ydxsj.ydsoldnote.service.RoleService;
import com.ydxsj.ydsoldnote.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权(验证权限时调用, 控制role 和 permissins时使用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        User user = (User) principals.getPrimaryPrincipal();
//        System.err.println(user.toString());
//        Integer userId = user.getId();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

//        获取权限role列表?
        Set<String> permsSet = new HashSet<>();
        Set<String> roles = new HashSet<>();
        List<RolePermission> rolePermissions = roleService.getPermissionByUserRole(user.getRoleNum());
        System.err.println(rolePermissions.toString());
        for (RolePermission rolePermission : rolePermissions){
            permsSet.add(rolePermission.getPermissionId());
        }
        roles.add(user.getRoleNum());
//        // 模拟权限和角色
//
//
//        if (userId == 1) {
//            // 超级管理员-权限
//            permsSet.add("delete");
//            permsSet.add("update");
//            permsSet.add("view");
//
//            roles.add("admin");
//        } else {
//            // 普通管理员-权限
//            permsSet.add("view");
//
//            roles.add("test");
//        }

        info.setStringPermissions(permsSet);
        info.setRoles(roles);

        return info;
    }

    /**
     * 认证(登录时调用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();

        //根据accessToken，查询用户信息
        UserToken userToken = userService.queryByToken(accessToken);
        //token失效
        SimpleDateFormat sm = new SimpleDateFormat("yyyyMMddHHmmss");
        Long expireTime;
        boolean flag = true;
        try {
//            expireTime     = sm.parse(managerToken.getExpireTime());
            expireTime = userToken.getExpireTime();
            flag = userToken == null || expireTime < System.currentTimeMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(flag){
            throw new IncorrectCredentialsException("token失效，请重新登录");
        }

        //查询用户信息
        User user = userService.getUserById(userToken.getUserId());
        //账号锁定
        // if(managerInfo.getStatus() == 0){
        //     throw new LockedAccountException("账号已被锁定,请联系管理员");
        // }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());

        return info;
    }
}

