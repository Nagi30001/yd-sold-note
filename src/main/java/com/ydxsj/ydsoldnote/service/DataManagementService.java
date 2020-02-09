package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.data.*;

import java.util.List;
import java.util.Map;

public interface DataManagementService {

    /**
     * 获取车辆品牌型号信息
     * @return
     */
    List<CarType> getCarType();

    /**
     * 获取销售类型信息集合
     * @return
     */
    List<SellType> getSellTypes();

    /**
     * 获取附加业务
     * @return
     */
    List<Addition> getAdditions();


    /**
     * 获取渠道信息
     * @param token
     * @return
     */
    List<Channel> getChannelMsgs(String token);


    /**
     * 获取该用户权限省
     * @param token
     * @return
     */
    List<Province> getProvinces(String token);

    /**
     * 根据省份信息获取城市信息
     * @param provinces
     * @return
     */
    List<City> getCitysByProvinces(List<Province> provinces);

    /**
     * 添加渠道信息
     * @param token
     * @param channelMsg
     * @return
     */
    Channel addChannelMsg(String token, Map channelMsg);

    /**
     * 更新渠道信息
     * @param token
     * @param channelMsg
     * @return
     */
    Channel updateChannelMsg(String token, Map channelMsg);

    /**
     * 获取设备信息
     * @return
     */
    List<EquipmentMsg> getEquipmentMsg();
}
