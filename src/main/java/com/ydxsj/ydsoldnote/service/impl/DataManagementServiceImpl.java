package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

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

    private final static String PLATFORM_ABBREVIATION = "PT";
    private final static String YOUDAO_ABBREVIATION = "YD";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



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

    @Override
    public List<InventoryMsg> getInventoryMsgByTPId(User user, String type) {
        if (PLATFORM_ABBREVIATION.equals(type)){
            List<InventoryMsg> inventoryMsg = dataManagementMapper.getInventoryMsgByTPId(user.getId());
            for (InventoryMsg inventoryMsg1 : inventoryMsg){
                inventoryMsg1.setUser(userMapper.selectUserById(inventoryMsg1.getThirdPartyTerraceId()));
                inventoryMsg1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(inventoryMsg1.getEquipmentMsgId()));
            }
            return inventoryMsg;
        } else if (YOUDAO_ABBREVIATION.equals(type)){
            // 获取这些全部用户id集合
            List<String> ids = userUtil.getIds(user);

            // 根据ID集合查询所有该ID所属的库存信息
            List<InventoryMsg> inventoryMsg = dataManagementMapper.getInventoryMsgByListIds(ids);
            // 补充用户信息及设备型号信息
            for (InventoryMsg inventoryMsg1 : inventoryMsg){
                inventoryMsg1.setUser(userMapper.selectUserById(inventoryMsg1.getThirdPartyTerraceId()));
                inventoryMsg1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(inventoryMsg1.getEquipmentMsgId()));
            }
            return inventoryMsg;

        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public List<TransferMsg> getTransferMsgById(User user, String type) {
        List<TransferMsg> transferMsg;
        if (PLATFORM_ABBREVIATION.equals(type)){
            // 安装平台
            // 发起人&收货人
            transferMsg = dataManagementMapper.getTransferMsgByTPId(user.getId());
        } else if (YOUDAO_ABBREVIATION.equals(type)){
            // 优道用户
            // 获取这些全部用户id集合
            List<String> ids = userUtil.getIds(user);
            transferMsg = dataManagementMapper.getTransferMsgByIds(ids);
        } else {
            return  new ArrayList<>();
        }
         // 补充信息，换格式
        for (TransferMsg transferMsg1 : transferMsg){
            transferMsg1.setRequestUser(userMapper.selectUserById(transferMsg1.getRequestUserId()));
            transferMsg1.setConsigneeUser(userMapper.selectUserById(transferMsg1.getConsigneeUserId()));
            transferMsg1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(transferMsg1.getEquipmentMsgId()));
            transferMsg1.setRequestTime(getSdfTime(transferMsg1.getRequestTime()));
            transferMsg1.setArriveTime(getSdfTime(transferMsg1.getArriveTime()));
            transferMsg1.setScrapTime(getSdfTime(transferMsg1.getScrapTime()));
        }
        return transferMsg;
    }

    @Override
    public List<ChangeMsg> getChangeMsgByTPId(User user, String type) {
        List<ChangeMsg> changeMsg;
        if (PLATFORM_ABBREVIATION.equals(type)){
            changeMsg = dataManagementMapper.getChangeMsgById(user.getId());
        } else if(YOUDAO_ABBREVIATION.equals(type)){
            List<String> ids = userUtil.getIds(user);
            changeMsg = dataManagementMapper.getChangeMsgByIds(ids);
        } else {
            return new ArrayList<>();
        }
        for (ChangeMsg changeMsg1 : changeMsg){
            changeMsg1.setRequestUser(userMapper.selectUserById(changeMsg1.getThirdPartyTerraceId()));
            changeMsg1.setCheckUser(userMapper.selectUserById(changeMsg1.getCheckUserId()));
            changeMsg1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(changeMsg1.getEquipmentMsgId()));
            changeMsg1.setRequestTime(getSdfTime(changeMsg1.getRequestTime()));
            changeMsg1.setPassTime(getSdfTime(changeMsg1.getPassTime()));
            changeMsg1.setCancellationTime(getSdfTime(changeMsg1.getCancellationTime()));
        }
        return changeMsg;
    }

    @Override
    public List<MaintainMsg> getMaintainMsg(User user, String type) {
        List<MaintainMsg> maintainMsg;
        if (YOUDAO_ABBREVIATION.equals(type)){
            List<String> ids = userUtil.getIds(user);
            maintainMsg = dataManagementMapper.getMaintainMsgByIds(ids);
        } else {
            return null;
        }
        for (MaintainMsg maintainMsg1 : maintainMsg){
            maintainMsg1.setRequestUser(userMapper.selectUserById(maintainMsg1.getRequestUserId()));
            maintainMsg1.setConsigneeUser(userMapper.selectUserById(maintainMsg1.getConsigneeUserId()));
            maintainMsg1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(maintainMsg1.getEquipmentMsgId()));
            maintainMsg1.setRequestTime(getSdfTime(maintainMsg1.getRequestTime()));
            maintainMsg1.setArriveTime(getSdfTime(maintainMsg1.getArriveTime()));
            maintainMsg1.setScrapTime(getSdfTime(maintainMsg1.getScrapTime()));
        }
        return maintainMsg;
    }

    @Override
    public List<PurchaseMsg> getPurchaseMsg(User user, String type) {
        List<PurchaseMsg> purchaseMsg;
        if (YOUDAO_ABBREVIATION.equals(type)){
            List<String> ids = userUtil.getIds(user);
            purchaseMsg = dataManagementMapper.getPurchaseMsgByIds(ids);
        } else if (PLATFORM_ABBREVIATION.equals(type)){
            List<PurchaseMsg> purchaseMsgs = dataManagementMapper.getPurchaseMsgByConsigneeUserId(user.getId());
            return purchaseMsgs;
        } else {
            return null;
        }
        for (PurchaseMsg purchaseMs1 : purchaseMsg){
            purchaseMs1.setPurchaseUser(userMapper.selectUserById(purchaseMs1.getPurchaseUserId()));
            purchaseMs1.setConsigneeUser(userMapper.selectUserById(purchaseMs1.getConsigneeUserId()));
            purchaseMs1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(purchaseMs1.getEquipmentMsgId()));
            purchaseMs1.setPurchaseTime(getSdfTime(purchaseMs1.getPurchaseTime()));
            purchaseMs1.setArriveTime(getSdfTime(purchaseMs1.getArriveTime()));
            purchaseMs1.setScrapTime(getSdfTime(purchaseMs1.getScrapTime()));
        }
        return purchaseMsg;
    }

    @Override
    public List<ScrapMsg> getScrapMsg(User user, String type) {
        List<ScrapMsg> scrapMsg;
        if (YOUDAO_ABBREVIATION.equals(type)){
            List<String> ids = userUtil.getIds(user);
            scrapMsg = dataManagementMapper.getScrapMsgByIds(ids);
        } else {
            return new ArrayList<>();
        }
        for (ScrapMsg scrapMsg1 : scrapMsg){
            scrapMsg1.setRequestUser(userMapper.selectUserById(scrapMsg1.getRequestUserId()));
            scrapMsg1.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(scrapMsg1.getEquipmentMsgId()));
            scrapMsg1.setCreateTime(getSdfTime(scrapMsg1.getCreateTime()));
        }
        return scrapMsg;
    }

    @Override
    public EquipmentMsg addEquipment(Map map) {
        String brand = String.valueOf(map.get("brand"));
        String type = String.valueOf(map.get("type"));
        String size = String.valueOf(map.get("size"));
        if (StringUtils.isEmpty(brand) || StringUtils.isEmpty(type) || StringUtils.isEmpty(size)){
            return null;
        } else {
            EquipmentMsg equipmentMsg = new EquipmentMsg();
            equipmentMsg.setEquipmentBrand(brand);
            equipmentMsg.setEquipmentTypeNum(type);
            equipmentMsg.setSize(size+'"');
            equipmentMsg.setStatus(1);
            Integer row = dataManagementMapper.insertEquipmentMsg(equipmentMsg);
            if (row == 0){
                return null;
            } else {
                return equipmentMsg;
            }

        }
    }

    @Override
    public List<CarType> getCarTypeMsg() {
        List<CarType> carTypeList =  dataManagementMapper.getCarTypeMsg("off");
        return carTypeList;
    }

    @Override
    public CarType addCarType(Map map) {
        String brand = String.valueOf(map.get("brand"));
        String subsidiary = String.valueOf(map.get("subsidiary"));
        CarType carType = new CarType();
        carType.setBrand(brand);
        carType.setSubsidiary(subsidiary);
        carType.setStatus(1);
        Integer row = dataManagementMapper.insertCarType(carType);
        if (row == 0){
            return null;
        } else {
            return carType;
        }
    }

    @Transactional
    @Override
    public PurchaseMsg addPurchaseMsg(Map map) {
        System.err.println(map);
        String id = String.valueOf(map.get("purchaseUserId"));
        String consigneeUserId = String.valueOf(map.get("consigneeUserId"));
        String brand = String.valueOf(map.get("brand"));
        String type = String.valueOf(map.get("type"));
        String size = String.valueOf(map.get("size"));
        EquipmentMsg equipmentMsg = new EquipmentMsg();
        equipmentMsg.setEquipmentBrand(brand);
        equipmentMsg.setEquipmentTypeNum(type);
        equipmentMsg.setSize(size);
        // 获取设备Id
        Integer equipmentMsgId = dataManagementMapper.getEquipmentMsgId(equipmentMsg);
        Integer count = (Integer) map.get("count");
        PurchaseMsg purchaseMsg = new PurchaseMsg();
        purchaseMsg.setPurchaseUserId(Integer.valueOf(id));
        purchaseMsg.setConsigneeUserId(Integer.valueOf(consigneeUserId));
        purchaseMsg.setEquipmentMsgId(equipmentMsgId);
        purchaseMsg.setCount(count);
        purchaseMsg.setPurchaseTime(String.valueOf(System.currentTimeMillis()));
        purchaseMsg.setStatus(1);
        Integer row = dataManagementMapper.insertPurchaseMsg(purchaseMsg);
        if(!row.equals(1)){
            throw new  RuntimeException("新增采购信息失败！");
        } else {
            purchaseMsg.setConsigneeUser(userMapper.selectUserById(purchaseMsg.getConsigneeUserId()));
            purchaseMsg.setPurchaseUser(userMapper.selectUserById(purchaseMsg.getPurchaseUserId()));
            purchaseMsg.setEquipmentMsg(dataManagementMapper.getEquipmentMsgById(purchaseMsg.getEquipmentMsgId()));
            // 添加库存
            // 获取该用户库存
            boolean index = false;
            InventoryMsg inventoryMsg1 = null;
            List<InventoryMsg> InventoryMsgs = dataManagementMapper.getInventoryMsgByTPId(Integer.valueOf(consigneeUserId));
            // 判断是否有该设备库存
            for (InventoryMsg inventoryMsg : InventoryMsgs){
                if (inventoryMsg.getEquipmentMsgId() == purchaseMsg.getEquipmentMsgId()){
                    index = true;
                    inventoryMsg1 = inventoryMsg;
                    break;
                }
            }
            if (index && inventoryMsg1 != null ){
                // 有该库存信息，更新库存
                InventoryMsg inventoryMsg = new InventoryMsg();
                inventoryMsg.setId(inventoryMsg1.getId());
                inventoryMsg.setInPurchase(count);
                Integer line = dataManagementMapper.updateInventoryMsg(inventoryMsg);
                if(line != 1){
                    // 库存更新失败，回调
                    throw new  RuntimeException("更新库存信息失败！");
                }
            } else {
                // 没有该库存信息，新增一条库存
                InventoryMsg inventoryMsg = new InventoryMsg();
                inventoryMsg.setThirdPartyTerraceId(purchaseMsg.getConsigneeUserId());
                inventoryMsg.setEquipmentMsgId(purchaseMsg.getEquipmentMsgId());
                inventoryMsg.setAwaitReceive(0);
                inventoryMsg.setAwaitInstall(0);
                inventoryMsg.setInPurchase(purchaseMsg.getCount());
                inventoryMsg.setInMaintain(0);
                inventoryMsg.setInInventory(0);
                Integer line = dataManagementMapper.insertInventoryMsg(inventoryMsg);
                if (line != 1){
                    throw new  RuntimeException("添加库存失败！");
                }
            }
            purchaseMsg.setPurchaseTime(sdf.format(new Date(Long.parseLong(purchaseMsg.getPurchaseTime()))));
            return purchaseMsg;
        }
    }

    @Override
    @Transactional
    public boolean scrapPurchaseMsg(Map map) {
        String userId = String.valueOf(map.get("userId"));
        String purchaseMsgId = String.valueOf(map.get("purchaseMsgId"));
        // 获取采购单据信息
        PurchaseMsg purchaseMsg = dataManagementMapper.getPurchaseMsgById(purchaseMsgId);
        // 单据null 或者 创建人不是该用户 则失败
        if (purchaseMsg == null || purchaseMsg.getPurchaseUserId() != Integer.valueOf(userId)){
            return false;
        } else {
            PurchaseMsg purchaseMsg1 = new PurchaseMsg();
            purchaseMsg1.setStatus(0);
            purchaseMsg1.setId(Integer.valueOf(purchaseMsgId));
            purchaseMsg1.setScrapTime(String.valueOf(System.currentTimeMillis()));
            // 修改状态
            Integer row = dataManagementMapper.updatePurchaseMsgStatus(purchaseMsg1);
            System.err.println(row);
            if (!row.equals(1)){
                throw new RuntimeException("更新采购单据状态失败");
            }
            // 减少库存的采购中数量
            return true;
        }
    }

    @Override
    public boolean receivePurchaseMsg(Map map) {
        String userId = String.valueOf(map.get("userId"));
        String purchaseMsgId = String.valueOf(map.get("purchaseMsgId"));
        // 获取单据信息
        PurchaseMsg purchaseMsg = dataManagementMapper.getPurchaseMsgById(purchaseMsgId);
        if (purchaseMsg == null || purchaseMsg.getConsigneeUserId() != Integer.valueOf(userId) || StringUtils.isEmpty(userId)){
            return false;
        } else {
            // 收货操作
            // 采购单据状态更改/补相应时间
            PurchaseMsg purchaseMsg1 = new PurchaseMsg();
            purchaseMsg1.setStatus(2);
            purchaseMsg1.setArriveTime(String.valueOf(System.currentTimeMillis()));
            purchaseMsg1.setId(Integer.valueOf(purchaseMsgId));
            Integer row = dataManagementMapper.updatePurchaseMsgStatus(purchaseMsg1);
            if (!row.equals(1)){
                throw new RuntimeException("采购单据状态更改失败！");
            }
            System.err.println("采购单据状态更新成功");
            // 收货人 正常库存增加，采购中库存减少
            InventoryMsg inventoryMsg = new InventoryMsg();
            inventoryMsg.setThirdPartyTerraceId(Integer.valueOf(userId));
            inventoryMsg.setEquipmentMsgId(purchaseMsg.getEquipmentMsgId());
            inventoryMsg.setAwaitInstall(purchaseMsg.getCount());
            inventoryMsg.setInPurchase(-purchaseMsg.getCount());
            Integer line = dataManagementMapper.updateInventoryMsg(inventoryMsg);

            System.err.println("库存更新成功"+line);
            if (!line.equals(1)){
                throw new RuntimeException("库存更新失败！");
            }
            return true;

        }
    }

    @Override
    public CheckIccidResult checkIccid(String iccid) {
        CheckIccidResult checkIccidResult = new CheckIccidResult();
        iccid = iccid+"_";
        Integer row = dataManagementMapper.getIccid(iccid);
        if (row != 1){
            checkIccidResult.setResult(false);
            checkIccidResult.setMessage("Iccid有误或已被占用,请检查checkIccid！");
        } else {
            checkIccidResult.setResult(true);
        }
        return checkIccidResult;
    }

    @Override
    public List<Iccid> getIccidsByStatus(int status) {
        List<Iccid> iccids = dataManagementMapper.getIccidsByStatus(status);
        return iccids;
    }

    @Transactional
    @Override
    public List<Channel> getChangeByUser(User user) {
        // 获取区域城市集合
        List<String> prvinces = userUtil.getProvinceByUser(user);
        List<Channel> channels = dataManagementMapper.getChannelByProvince(prvinces);
        if (channels == null){
            throw new RuntimeException("请求数据错误!");
        }
        return channels;
    }


    /**
     * 把13位时间戳转换为看得懂的日期时间格式
     * @param timeMilli
     * @return
     */
    public String getSdfTime(String timeMilli){
        if (!StringUtils.isEmpty(timeMilli)){
            return sdf.format(new Date(Long.parseLong(timeMilli)));
        } else {
            return null;
        }
    }
}
