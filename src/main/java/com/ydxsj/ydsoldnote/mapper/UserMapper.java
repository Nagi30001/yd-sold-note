package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.role.Role;
import com.ydxsj.ydsoldnote.bean.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 根据用户省份
     * @param provinces
     * @param type
     * @return
     */
    List<User> getUsersByProvince(@Param("provinces") List<String> provinces,@Param("type") String type);

    /**
     * 根据类型获取权限列表
     * @param type
     * @return
     */
    List<Role> getRoles(@Param("type") String type);

    /**
     * 检查工号是否存在
     * @param jobNum
     * @return
     */
    Integer selectUserByJobNum(@Param("jobNum") int jobNum);


    /**
     * 添加用户
     * @param user
     * @return
     */
    Integer insertUser(@Param("user") User user);


    /**
     * 更新用户信息
     * @param user
     * @return
     */
    Integer updateUser(@Param("user")User user);


    /**
     * 根据用户名获取用户信息
     * @param sellName
     * @return
     */
    List<User> getUserByLikeUserName(String sellName);

    /**
     * 根据省份查询对应的收货平台用户
     * @param provinces
     * @return
     */
    List<User> getPlatformsByProvince(@Param("provinces")List<Province> provinces);
}
