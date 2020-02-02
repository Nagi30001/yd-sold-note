package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;

import java.util.List;

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
}
