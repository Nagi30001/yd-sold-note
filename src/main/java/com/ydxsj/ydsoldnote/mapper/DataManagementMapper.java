package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.CarType;
import com.ydxsj.ydsoldnote.bean.data.Channel;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataManagementMapper {


    /**
     * 获取全部车型
     * @return
     */
    List<CarType> getCarType();

    /**
     * 获取全部销售类型
     * @return
     */
    List<SellType> getSellTypes();

    /**
     * 获取全部附加业务
     * @return
     */
    List<Addition> getAdditions();


    /**
     * 根据销售id获取销售类型信息
     * @param sellType
     * @return
     */
    SellType getSellTypeById(@Param("sellType") Integer sellType);

    /**
     * 根据附加业务id集合获取附加业务对象信息
     * @param additionIds
     * @return
     */
    List<Addition> getAdditionsById(@Param("additionIds") List<Integer> additionIds);


    /**
     * 获取查询人区域的渠道信息
     * @param provinces
     * @return
     */
    List<Channel> getChannelByProvince(@Param("provinces") List<String> provinces);

    /**
     * 添加渠道信息
     * @param channel
     * @return
     */
    Integer addChannel(@Param("channel") Channel channel);

    /**
     * 根据id获取某一渠道信息
     * @param id
     * @return
     */
    Channel getChannelById(int id);

    /**
     * 更新channel
     * @param channel1
     * @return
     */
    Integer updateChannel(@Param("channel") Channel channel1);


    /**
     * 获取设备信息
     * @return
     */
    List<EquipmentMsg> getEquipmentMsg();

    /**
     * 安装平台获取库存信息
     * @param id
     * @return
     */
    List<InventoryMsg> getInventoryMsgByTPId(@Param("id") Integer id);

    /**
     * 根据型号id获取型号信息
     * @param equipmentMsgId
     * @return
     */
    EquipmentMsg getEquipmentMsgById(@Param("id") Integer equipmentMsgId);

    /**
     * 根据id集合查询库存信息
     * @param ids
     * @return
     */
    List<InventoryMsg> getInventoryMsgByListIds(@Param("ids") List<String> ids);

    /**
     * 根据id获取 该id发起的转移申请&是该id收货的转移申请单
     * @param id
     * @return
     */
    List<TransferMsg> getTransferMsgByTPId(@Param("id") Integer id);


    /**
     * 根据id集合查询转移申请单集合
     * @param ids
     * @return
     */
    List<TransferMsg> getTransferMsgByIds(@Param("ids")List<String> ids);


    /**
     * 根据id获取该平台的更换记录
     * @param id
     * @return
     */
    List<ChangeMsg> getChangeMsgById(@Param("id")Integer id);

    /**
     * 根据ids集合获取更换记录
     * @param ids
     * @return
     */
    List<ChangeMsg> getChangeMsgByIds(@Param("ids")List<String> ids);


    /**
     * 根据ids获取维修信息
     * @param ids
     * @return
     */
    List<MaintainMsg> getMaintainMsgByIds(@Param("ids")List<String> ids);

    /**
     * 根据ids 获取采购信息
     * @param ids
     * @return
     */
    List<PurchaseMsg> getPurchaseMsgByIds(@Param("ids")List<String> ids);


    /**
     * 根据ids 获取报废信息
     * @param ids
     * @return
     */
    List<ScrapMsg> getScrapMsgByIds(@Param("ids")List<String> ids);


    /**
     * 添加设备信息
     * @param equipmentMsg
     * @return
     */
    Integer insertEquipmentMsg(@Param("equipmentMsg") EquipmentMsg equipmentMsg);
}
