package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.City;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;
import com.ydxsj.ydsoldnote.config.shiro.TokenGenerator;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import com.ydxsj.ydsoldnote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private CityMapper cityMapper;

    //12小时后过期
    private final static int EXPIRE = 3600 * 12 * 1000;

    public User getUserByJobName(String jobName){
       User user = userMapper.selectUserByJobName(jobName);
       return user;
    }

    @Override
    public User getUserByToken(String token) {
        System.err.println("token"+token);
        Integer userId = userTokenMapper.getUserIdByToken(token);
        User user = userMapper.selectUserById(userId);
        return user;
    }

    @Override
    public List<Province> getCitys(User user) {
        // 获取用户省份代码
        List<String> provinceIds = Arrays.asList(user.getBeProvince().split("-"));
        // 根据省份id获取名称
        List<Province> provinces = cityMapper.getProvinceByIds(provinceIds);
        // 根据省份id获取市对象
        for (Province province : provinces ){
            List<City> cities = cityMapper.getCitysByProvinceId(province.getId());
            province.setCities(cities);
        }
        return provinces;
    }

    @Override
    public List<User> getUserByRole(String role) {
        List<User> users = userMapper.selectUserByRole(role);
        return users;
    }


    @Override
    public List<String> getUserInfoByToken(String token) {
        //获取用户
        User user = userMapper.selectUserById(userTokenMapper.getUserIdByToken(token));
        List<String> roles = Arrays.asList(user.getRoleNum().split(","));
        return roles;
    }

    @Override
    public User getUserById(Integer userId) {
        User user = userMapper.selectUserById(userId);
        return user;
    }

    @Override
    public UserToken saveToken(Integer userId) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        //生成一个token
        userToken.setToken(TokenGenerator.generateValue());

        Long nowTime = System.currentTimeMillis();
        //过期时间
        userToken.setExpireTime(nowTime + EXPIRE);
        //更新时间
        userToken.setUpdateTime(nowTime);

//       查询token表中是否有该id的token信息
        UserToken userToken1 = userTokenMapper.selectTokenById(userId);
//       没有
        if (userToken1 == null){
            // 将token信息插入数据库
            userTokenMapper.insertToken(userToken);
            return userToken;
        }
//        有的话就是更新
        userTokenMapper.updateUserToken(userToken);
        return userToken;

    }

    @Override
    public UserToken queryByToken(String token) {
        if (token == null || "".equals(token)) {
            return null;
        }
        UserToken userToken = userTokenMapper.selectTokenByToken(token);
        return userToken;
    }
}
