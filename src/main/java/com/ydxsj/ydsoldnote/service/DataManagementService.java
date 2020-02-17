package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import com.ydxsj.ydsoldnote.bean.user.User;

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

    /**
     * 根据type获取不同的库存信息
     * @param
     * @return
     */
    List<InventoryMsg> getInventoryMsgByTPId(User user, String type);

    /**
     * 根据type获取平台转移信息
     * @param
     * @return
     */
    List<TransferMsg> getTransferMsgById(User user, String type);


    /**
     * 根据type获取更换设备信息
     * @param user
     * @param type
     * @return
     */
    List<ChangeMsg> getChangeMsgByTPId(User user, String type);

    /**
     * 获取维修信息
     * @param user
     * @param type
     * @return
     */
    List<MaintainMsg> getMaintainMsg(User user, String type);

    /**
     * 获取采购信息
     * @param user
     * @param type
     * @return
     */
    List<PurchaseMsg> getPurchaseMsg(User user, String type);

    /**
     * 获取报废信息
     * @param user
     * @param type
     * @return
     */
    List<ScrapMsg> getScrapMsg(User user, String type);
}
