package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.data.*;
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


    /**
     * 根据参数类型获取相应状态数据
     * @param type off 全部数据 on 启用中数据
     * @return
     */
    List<CarType> getCarTypeMsg(@Param("type") String type);


    /**
     * 添加车辆型号信息
     * @param carType 车辆型号信息
     * @return 返回响应条数
     */
    Integer insertCarType(@Param("carType") CarType carType);

    /**
     * 根据信息获取设备id
     * @param equipmentMsg
     * @return
     */
    Integer getEquipmentMsgId(@Param("equipmentMsg") EquipmentMsg equipmentMsg);

    /**
     * 添加采购信息
     * @param purchaseMsg
     * @return
     */
    Integer insertPurchaseMsg(@Param("purchaseMsg") PurchaseMsg purchaseMsg);

    /**
     *  根据id获取对应的采购单据
     * @param id
     * @return
     */
    PurchaseMsg getPurchaseMsgById(@Param("id") String id);

    /**
     * 更新采购单据状态
     * @param purchaseMsg
     * @return
     */
    Integer updatePurchaseMsgStatus(@Param("purchaseMsg") PurchaseMsg purchaseMsg);

    /**
     * 根据收货人id获取单据
     * @param id
     * @return
     */
    List<PurchaseMsg> getPurchaseMsgByConsigneeUserId(@Param("id") Integer id);

    /**
     * 更新库存信息
     * @param inventoryMsg
     * @return
     */
    Integer updateInventoryMsg(@Param("inventoryMsg") InventoryMsg inventoryMsg);

    /**
     * 插入一条库存信息
     * @param inventoryMsg
     * @return
     */
    Integer insertInventoryMsg(@Param("inventoryMsg") InventoryMsg inventoryMsg);

    /**
     * 查询是否有该Iccid
     * @param iccid
     * @return
     */
    Integer getIccid(@Param("iccid") String iccid);

    /**
     * 根据状态获取对应的iccid数据
     * @param status
     * @return
     */
    List<Iccid> getIccidsByStatus(@Param("status") int status);

    /**
     * 根据字符串获取iccid
     * @param s
     * @return
     */
    String getIccidByiccid(@Param("s") String s);

    /**
     * 更新Iccid状态
     * @param iccid2
     * @return
     */
    Integer updateIccid(@Param("i") Iccid iccid2);

    /**
     * 根据三个信息获取设备id
     * @param equipmentBrand
     * @param equipmentTypeNum
     * @param size
     * @return
     */
    Integer getEquipmentMsgByMsg(@Param("equipmentBrand") String equipmentBrand,@Param("equipmentTypeNum") String equipmentTypeNum,@Param("size") String size);

    /**
     * 根据销售名称获取销售信息
     * @param sellTypeName
     * @return
     */
    SellType getSellTypeByName(@Param("sellTypeName") String sellTypeName);

    /**
     * 根据 name 获取附加业务信息
     * @param str
     * @return
     */
    Addition getAdditionsByName(@Param("str") String str);

    /**
     * 获取全部渠道信息
     * @return
     */
    List<Channel> getChannel();

    /**
     * 获取全部库存信息
     * @return
     */
    List<InventoryMsg> getInventoryMsg();

    /**
     * 获取全部采购信息
     * @return
     */
    List<PurchaseMsg> getPurchaseMsg();

    /**
     * 获取全部报废信息
     * @return
     */
    List<ScrapMsg> getScrapMsg();

    /**
     * 获取全部更换记录
     * @return
     */
    List<ChangeMsg> getChangeMsg();

    /**
     * 获取全部维修记录
     * @return
     */
    List<MaintainMsg> getMaintainMsg();

    /**
     * 获取全部转移记录
     * @return
     */
    List<TransferMsg> getTransferMsg();

    /**
     * 获取全部车型
     * @return
     */
    List<CarType> getAllCarType();

    /**
     * 根据省份信息获取对呀的渠道信息
     * @param provinces
     * @return
     */
    List<Channel> getChannelByProvinces(@Param("provinces") List<Province> provinces);
}
