package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.user.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据用户id返回用户信息
     * @param userId
     * @return
     */
    User selectUserById(Integer userId);

    /**
     * 更具账号查询用户
     * @param jobName
     * @return
     */
    User selectUserByJobName(String jobName);


    /**
     * 通过角色获取相应角色的用户信息
     * @param role
     * @return
     */
    List<User> selectUserByRole(String role);
}
