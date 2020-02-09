package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DataManagementServiceImpl implements DataManagementService {

    @Autowired
    private DataManagementMapper dataManagementMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private UserUtil userUtil;




    @Override
    public List<CarType> getCarType() {
        List<CarType> carTypes = dataManagementMapper.getCarType();
        for (CarType carType : carTypes){
            carType.setSubsidiarys(carType.getSubsidiary().split(","));
        }

        return carTypes;
    }

    @Override
    public List<SellType> getSellTypes() {
        return dataManagementMapper.getSellTypes();
    }

    @Override
    public List<Addition> getAdditions() {
        return dataManagementMapper.getAdditions();
    }

    @Override
    public List<Channel> getChannelMsgs(String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        //获取用户信息
        User user = userMapper.selectUserById(userTokenMapper.getUserIdByToken(token));
        //根据用户所在的省份权限查出该省份的渠道
        List<String> provinceIds = Arrays.asList(user.getBeProvince().split("-"));
        List<String> provinces = cityMapper.getProvinceById(provinceIds);
        List<Channel> channels = dataManagementMapper.getChannelByProvince(provinces);
        for (Channel channel : channels){
            channel.setUser(userMapper.selectUserById(channel.getCreateId()));
        }
        return channels;
    }

    @Override
    public List<Province> getProvinces(String token) {
        //获取用户信息
        return userUtil.getProvincesByToken(token);
    }

    @Override
    public List<City> getCitysByProvinces(List<Province> provinces) {
        List<String> provinceIds = new ArrayList<>();
        for (Province province : provinces){
            provinceIds.add(String.valueOf(province.getId()));
        }
        return cityMapper.getCitysByProvinceIds(provinceIds);
    }

    @Override
    public Channel addChannelMsg(String token, Map channelMsg) {
        //获取添加用户
        User user = userUtil.getUserByToken(token);
        Channel channel = new Channel();
        channel.setProvince((String) channelMsg.get("province"));
        channel.setCity((String) channelMsg.get("city"));
        channel.setChannelName((String) channelMsg.get("channelName"));
        channel.setSite((String) channelMsg.get("site"));
        channel.setStatus(1);
        channel.setCreateId(user.getId());
        channel.setUser(user);
        Integer row = dataManagementMapper.addChannel(channel);
        return channel;
    }

    @Override
    public Channel updateChannelMsg(String token, Map channelMsg) {
        User user = userUtil.getUserByToken(token);
        System.err.println(channelMsg);
        Integer id = (Integer) channelMsg.get("id");
        Channel channel = dataManagementMapper.getChannelById(id);
        if (user == null || channel == null || user.getId() != channel.getCreateId()){
            return null;
        } else {
            Channel channel1 = new Channel();
            channel1.setId((Integer) channelMsg.get("id"));
            channel1.setProvince((String) channelMsg.get("province"));
            channel1.setCity((String) channelMsg.get("city"));
            channel1.setChannelName((String) channelMsg.get("channelName"));
            channel1.setSite((String) channelMsg.get("site"));

            Integer row = dataManagementMapper.updateChannel(channel1);
        }

        return null;
    }

    @Override
    public List<EquipmentMsg> getEquipmentMsg() {
        return dataManagementMapper.getEquipmentMsg();
    }
}
