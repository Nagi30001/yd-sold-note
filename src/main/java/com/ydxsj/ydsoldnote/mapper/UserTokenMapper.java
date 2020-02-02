package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.user.UserToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserTokenMapper {

    /**
     * 插入token数据
     * @param userToken
     */
    void insertToken(@Param("userToken") UserToken userToken);

    /**
     * 根据token查询token对象
     * @param token
     * @return
     */
    UserToken selectTokenByToken(String token);

    /**
     * 根据用户id查询token是否存在
     * @param userId
     * @return
     */
    UserToken selectTokenById(Integer userId);

    /**
     * 更新token更新信息
     * @param userToken
     */
    void updateUserToken(@Param("userToken") UserToken userToken);

    /**
     * 通过token获取用户id
     * @param token
     * @return
     */
    Integer getUserIdByToken(String token);
}
