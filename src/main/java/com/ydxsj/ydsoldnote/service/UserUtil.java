package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class  UserUtil {

    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private  UserTokenMapper userTokenMapper;
    @Autowired
    private  CityMapper cityMapper;


    /**
     * 根据用户获取用户信息
     * @param token
     * @return
     */
    public  User getUserByToken(String token){
        return userMapper.selectUserById(userTokenMapper.getUserIdByToken(token));
    }

    /**
     * 根据用户信息获取省份权限集合
     * @param token
     * @return
     */
    public  List<String> getProvinceByToken(String token){
        User user = getUserByToken(token);
        List<String> provinceIds = Arrays.asList(user.getBeProvince().split("-"));
        List<String> provinces = cityMapper.getProvinceById(provinceIds);
        return provinces;

    }

    /**
     * 根据用户信息获取省份权限集合 对象
     * @param token
     * @return
     */
    public  List<Province> getProvincesByToken(String token){
        User user = getUserByToken(token);
        List<String> provinceIds = Arrays.asList(user.getBeProvince().split("-"));
        List<Province> provinces = cityMapper.getProvinceByIds(provinceIds);

        return provinces;

    }





}
