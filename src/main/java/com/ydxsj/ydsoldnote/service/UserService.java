package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.role.Role;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;

import java.util.List;
import java.util.Map;

public interface UserService {

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    User getUserById(Integer userId);

    /**
     * 根据userId 插入token
     * @param userId
     * @return
     */
    UserToken saveToken(Integer userId);

    /**
     * 根据token查询token数据
     * @param token
     * @return
     */
    UserToken queryByToken(String token);

    /**
     * 根据账户名查询用户
     * @param jobName
     * @return
     */
    User getUserByJobName(String jobName);

    /**
     * 根据token获取用户信息
     * @param token
     * @return
     */
    User getUserByToken(String token);

    /**
     * 根据用户信息，获得已有城市的数据
     * @param
     * @return
     */
    List<Province> getCitys(User user);

    /**
     * 根据角色获取相应的用户信息
     * @param role
     * @return
     */
    List<User> getUserByRole(String role);


    /**
     * 根据token获取用户权限列表
     * @param token
     * @return
     */
    List<String> getUserInfoByToken(String token);

    /**
     * 根据token获取权限内的 type类型用户
     * @param token
     * @param yd
     * @return
     */
    List<User> getUsersByType(String token, String yd);

    /**
     * 判断用户是否超级管理员 是返回所有权限，否返回除超管理员外权限
     * @param user
     * @return
     */
    List<Role> getRolesBy(User user);

    /**
     * 添加用户
     * @param token
     * @param userMap
     * @return
     */
    User addUser(String token, Map userMap);

    /**
     * 检查工号是否被占用
     * @param value
     * @return
     */
    boolean checkJobNum(String value);


    /**
     * 更新用户信息
     * @param token
     * @param userMap
     * @return
     */
    boolean updateUser(String token, Map userMap);
}
