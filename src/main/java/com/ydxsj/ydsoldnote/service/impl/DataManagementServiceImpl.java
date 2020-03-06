package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.UserService;
import com.ydxsj.ydsoldnote.service.UserUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.CityJedisUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.DMJedisUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.IccidJedisUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.UserJedisUtil;
import com.ydxsj.ydsoldnote.util.PublicUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleUnresolved;
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
    @Autowired
    private UserService userService;

    private final static String PLATFORM_ABBREVIATION = "PT";
    private final static String YOUDAO_ABBREVIATION = "YD";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public List<CarType> getCarType() throws RuntimeException {
        List<CarType> carTypes = DMJedisUtil.getCarBrandAndSubBrand();
        if (CollectionUtils.isEmpty(carTypes)) {
            // no cache
            carTypes = dataManagementMapper.getCarType();
            for (CarType carType : carTypes) {
                carType.setSubsidiarys(carType.getSubsidiary().split(","));
            }
            // cache
            DMJedisUtil.initializationCarBrandAndCarSubBrand(carTypes);
        }
        return carTypes;

    }

    @Override
    public List<SellType> getSellTypes() throws RuntimeException {
        List<SellType> sellTypes = DMJedisUtil.getSellType();
        if (CollectionUtils.isEmpty(sellTypes)) {
            // no cache
            sellTypes = dataManagementMapper.getSellTypes();
            // cache
            DMJedisUtil.initializationSellType(sellTypes);
        }
        return sellTypes;
    }

    @Override
    public List<Addition> getAdditions() throws RuntimeException {
        List<Addition> addition = DMJedisUtil.getAddition();
        if (CollectionUtils.isEmpty(addition)) {
            // no cache
            addition = dataManagementMapper.getAdditions();
            // cache
            DMJedisUtil.initializationAddition(addition);
        }
        return addition;
    }

    @Override
    public List<Channel> getChannelMsgs(String token) throws RuntimeException {
        // 获取省份信息
        List<Province> provinces = getProvinces(token);
        //根据用户所在的省份权限查出该省份的渠道
        List<Channel> channels = DMJedisUtil.getChannelByProvinces(provinces);
        if (CollectionUtils.isEmpty(channels)) {
            // no cache
            channels = dataManagementMapper.getChannelByProvinces(provinces);
            // channel cache
            List<Channel> channelList = dataManagementMapper.getChannel();
            DMJedisUtil.initializationChannel(channelList);
        }
        for (Channel channel : channels) {
            channel.setUser(UserJedisUtil.getUserById(channel.getCreateId()));
        }
        return channels;
    }

    @Override
    public List<Province> getProvinces(String token) throws RuntimeException {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        User user = UserJedisUtil.getUserByToken(token);
        Set<Integer> ids = CityJedisUtil.getUserProvinceIds(user);
        List<Province> provinces = CityJedisUtil.getProvinceByIds(ids);
        if (CollectionUtils.isEmpty(provinces)) {
            // no cache
            provinces = cityMapper.getProvinceByIds(ids);
            // 省份缓存初始化
            CityJedisUtil.initialization();
        }
        return provinces;

    }

    @Override
    public List<City> getCitysByProvinces(List<Province> provinces) throws RuntimeException {

        List<City> cities = CityJedisUtil.getCitiesByProvinces(provinces);
        if (CollectionUtils.isEmpty(cities)) {
            // no cache
            // cache
            CityJedisUtil.initialization();
            cities = CityJedisUtil.getCitiesByProvinces(provinces);
        }
        return cities;
    }

    @Override
    public Channel addChannelMsg(String token, Map channelMsg) throws RuntimeException {
        return addOrUpdateChanel(token, channelMsg, true);
    }

    @Override
    public Channel updateChannelMsg(String token, Map channelMsg) throws RuntimeException {
        return addOrUpdateChanel(token, channelMsg, false);
    }

    /**
     * 添加/更新渠道信息
     *
     * @param token
     * @param channelMsg
     * @param type
     * @return
     */
    @Transactional
    public Channel addOrUpdateChanel(String token, Map channelMsg, Boolean type) throws RuntimeException {
        //获取添加用户
        User user = UserJedisUtil.getUserByToken(token);
        Channel channel = new Channel();
        channel.setProvince((String) channelMsg.get("province"));
        channel.setCity((String) channelMsg.get("city"));
        channel.setChannelName((String) channelMsg.get("channelName"));
        channel.setSite((String) channelMsg.get("site"));
        if (type) {
            // add
            channel.setStatus(1);
            channel.setCreateId(user.getId());
            channel.setUser(user);
            Integer row = dataManagementMapper.addChannel(channel);
            if (!row.equals(1)) {
                throw new RuntimeException("添加渠道信息错误！");
            }
            // add cache
            DMJedisUtil.addChannel(channel);

        } else {
            // update
            channel.setId((Integer) channelMsg.get("id"));
            Channel oldChannel = DMJedisUtil.getChannelById(channel.getId());
            if (oldChannel == null) {
                // no cache
                // cache
                DMJedisUtil.initializationChannel(dataManagementMapper.getChannel());
            }
            Integer row = dataManagementMapper.updateChannel(channel);
            if (!row.equals(1)) {
                throw new RuntimeException("更新渠道信息错误！");
            }
            // update cache
            channel = dataManagementMapper.getChannelById(channel.getId());
            DMJedisUtil.updateChannel(oldChannel, channel);
        }
        return channel;
    }

    @Override
    public List<EquipmentMsg> getEquipmentMsg() throws RuntimeException {
        List<EquipmentMsg> allEquipmentMsg = DMJedisUtil.getAllEquipmentMsg();
        if (CollectionUtils.isEmpty(allEquipmentMsg)) {
            // no cache
            allEquipmentMsg = dataManagementMapper.getEquipmentMsg();
            // cache
            DMJedisUtil.initializationEquipmentMsg(allEquipmentMsg);
        }
        return allEquipmentMsg;
    }

    @Override
    public List<InventoryMsg> getInventoryMsgByTPId(User user, String type) throws RuntimeException {
        List<InventoryMsg> inventoryMsg = new ArrayList<>();
        if (PLATFORM_ABBREVIATION.equals(type)) {
            inventoryMsg = DMJedisUtil.getInventoryMsgByTPId(user.getId());
            if (CollectionUtils.isEmpty(inventoryMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationInventoryMsg(dataManagementMapper.getInventoryMsg());
                inventoryMsg = DMJedisUtil.getInventoryMsgByTPId(user.getId());
            }
        } else if (YOUDAO_ABBREVIATION.equals(type)) {
            // 获取该用户区域权限内的用户信息
            List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
            List<User> userList = UserJedisUtil.getUserByProvinces(provinces);
            // 根据ID集合查询所有该ID所属的库存信息
            inventoryMsg = DMJedisUtil.getInventoryMsgByTPIds(userList);
            if (CollectionUtils.isEmpty(inventoryMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationInventoryMsg(dataManagementMapper.getInventoryMsg());
                inventoryMsg = DMJedisUtil.getInventoryMsgByTPIds(userList);
            }
        } else {
            return null;
        }
        // 补充用户信息及设备型号信息
        for (InventoryMsg inventoryMsg1 : inventoryMsg) {
            inventoryMsg1.setUser(UserJedisUtil.getUserById(inventoryMsg1.getThirdPartyTerraceId()));
            EquipmentMsg equipmentMsg = DMJedisUtil.getEquipmentMsgById(inventoryMsg1.getEquipmentMsgId());
            if (equipmentMsg == null) {
                // no cache
                // cache
                DMJedisUtil.initializationEquipmentMsg(dataManagementMapper.getEquipmentMsg());
                equipmentMsg = DMJedisUtil.getEquipmentMsgById(inventoryMsg1.getEquipmentMsgId());
            }
            inventoryMsg1.setEquipmentMsg(equipmentMsg);
        }
        return inventoryMsg;

    }

    @Override
    public List<TransferMsg> getTransferMsgById(User user, String type) throws RuntimeException {
        List<TransferMsg> transferMsg;
        if (PLATFORM_ABBREVIATION.equals(type)) {
            // 安装平台
            // 发起人&收货人
            transferMsg = DMJedisUtil.getPTAndTransferMsg(user);
            if (CollectionUtils.isEmpty(transferMsg)) {
                DMJedisUtil.initializationTransferMsg(dataManagementMapper.getTransferMsg());
                transferMsg = DMJedisUtil.getPTAndTransferMsg(user);
            }
        } else if (YOUDAO_ABBREVIATION.equals(type)) {
            // 优道用户
            // 获取这些全部用户id集合
            List<String> ids = userUtil.getIds(user);
            List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
            List<User> userList = UserJedisUtil.getUserByProvinces(provinces);
            transferMsg = DMJedisUtil.getTransferMsgByUsers(userList);
            if (CollectionUtils.isEmpty(transferMsg)) {
                DMJedisUtil.initializationTransferMsg(dataManagementMapper.getTransferMsg());
                transferMsg = DMJedisUtil.getTransferMsgByUsers(userList);
            }
        } else {
            return new ArrayList<>();
        }
        // 补充信息，换格式
        for (TransferMsg transferMsg1 : transferMsg) {
            transferMsg1.setRequestUser(UserJedisUtil.getUserById(transferMsg1.getRequestUserId()));
            transferMsg1.setConsigneeUser(UserJedisUtil.getUserById(transferMsg1.getConsigneeUserId()));
            transferMsg1.setEquipmentMsg(getEquipmentMsgById(transferMsg1.getEquipmentMsgId()));
            transferMsg1.setRequestTime(PublicUtil.timestampToString(transferMsg1.getRequestTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            transferMsg1.setArriveTime(PublicUtil.timestampToString(transferMsg1.getArriveTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            transferMsg1.setScrapTime(PublicUtil.timestampToString(transferMsg1.getScrapTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        }
        return transferMsg;
    }

    @Override
    public List<ChangeMsg> getChangeMsgByTPId(User user, String type) throws RuntimeException {
        List<ChangeMsg> changeMsg;
        if (PLATFORM_ABBREVIATION.equals(type)) {
            changeMsg = DMJedisUtil.getChannelMsgByUser(user, true);
            if (CollectionUtils.isEmpty(changeMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationChannel(dataManagementMapper.getChannel());
                changeMsg = DMJedisUtil.getChannelMsgByUser(user, true);
            }
        } else if (YOUDAO_ABBREVIATION.equals(type)) {
            List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
            List<User> userList = UserJedisUtil.getUserByProvinces(provinces);
            changeMsg = DMJedisUtil.getChannelMsgByUsers(userList);
            if (CollectionUtils.isEmpty(changeMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationChannel(dataManagementMapper.getChannel());
                changeMsg = DMJedisUtil.getChannelMsgByUsers(userList);
            }
        } else {
            return new ArrayList<>();
        }
        for (ChangeMsg changeMsg1 : changeMsg) {
            changeMsg1.setRequestUser(UserJedisUtil.getUserById(changeMsg1.getThirdPartyTerraceId()));
            changeMsg1.setCheckUser(UserJedisUtil.getUserById(changeMsg1.getCheckUserId()));
            changeMsg1.setEquipmentMsg(getEquipmentMsgById(changeMsg1.getEquipmentMsgId()));
            changeMsg1.setRequestTime(PublicUtil.timestampToString(changeMsg1.getRequestTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            changeMsg1.setPassTime(PublicUtil.timestampToString(changeMsg1.getPassTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            changeMsg1.setCancellationTime(PublicUtil.timestampToString(changeMsg1.getCancellationTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        }
        return changeMsg;
    }

    @Override
    public List<MaintainMsg> getMaintainMsg(User user, String type) throws RuntimeException {
        List<MaintainMsg> maintainMsg;
        if (YOUDAO_ABBREVIATION.equals(type)) {
            List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
            List<User> userList = UserJedisUtil.getUserByProvinces(provinces);
            maintainMsg = DMJedisUtil.getMaintainMgsByUsers(userList);
            if (CollectionUtils.isEmpty(maintainMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationMaintainMsg(dataManagementMapper.getMaintainMsg());
                maintainMsg = DMJedisUtil.getMaintainMgsByUsers(userList);
            }
        } else {
            return null;
        }
        for (MaintainMsg maintainMsg1 : maintainMsg) {
            maintainMsg1.setRequestUser(UserJedisUtil.getUserById(maintainMsg1.getRequestUserId()));
            maintainMsg1.setConsigneeUser(UserJedisUtil.getUserById(maintainMsg1.getConsigneeUserId()));
            maintainMsg1.setEquipmentMsg(getEquipmentMsgById(maintainMsg1.getEquipmentMsgId()));
            maintainMsg1.setRequestTime(PublicUtil.timestampToString(maintainMsg1.getRequestTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            maintainMsg1.setArriveTime(PublicUtil.timestampToString(maintainMsg1.getArriveTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            maintainMsg1.setScrapTime(PublicUtil.timestampToString(maintainMsg1.getScrapTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        }
        return maintainMsg;
    }

    @Override
    public List<PurchaseMsg> getPurchaseMsg(User user, String type) throws RuntimeException {
        List<PurchaseMsg> purchaseMsg = new ArrayList<>();
        if (YOUDAO_ABBREVIATION.equals(type)) {
            List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
            List<User> users = UserJedisUtil.getUserByProvinces(provinces);
            purchaseMsg = DMJedisUtil.getPurchaseMsgByUsers(users);
            if (CollectionUtils.isEmpty(purchaseMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationPurchaseMsg(dataManagementMapper.getPurchaseMsg());
                purchaseMsg = DMJedisUtil.getPurchaseMsgByUsers(users);
            }
        } else if (PLATFORM_ABBREVIATION.equals(type)) {
            purchaseMsg = DMJedisUtil.getPurchaseMsgByUser(user);
            if (CollectionUtils.isEmpty(purchaseMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationPurchaseMsg(dataManagementMapper.getPurchaseMsg());
                purchaseMsg = DMJedisUtil.getPurchaseMsgByUser(user);
            }
        } else {
            return null;
        }
        // 对象排序
        Collections.sort(purchaseMsg, new Comparator<PurchaseMsg>() {
            @Override
            public int compare(PurchaseMsg o1, PurchaseMsg o2) {
                // 倒序
                return o2.getPurchaseTime().compareTo(o1.getPurchaseTime());
            }
            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });
        for (PurchaseMsg purchaseMs1 : purchaseMsg) {
            purchaseMs1.setPurchaseUser(UserJedisUtil.getUserById(purchaseMs1.getPurchaseUserId()));
            purchaseMs1.setConsigneeUser(UserJedisUtil.getUserById(purchaseMs1.getConsigneeUserId()));
            purchaseMs1.setEquipmentMsg(getEquipmentMsgById(purchaseMs1.getEquipmentMsgId()));
            purchaseMs1.setPurchaseTime(PublicUtil.timestampToString(purchaseMs1.getPurchaseTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            purchaseMs1.setArriveTime(PublicUtil.timestampToString(purchaseMs1.getArriveTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            purchaseMs1.setScrapTime(PublicUtil.timestampToString(purchaseMs1.getScrapTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        }
        return purchaseMsg;
    }

    @Override
    public List<ScrapMsg> getScrapMsg(User user, String type) throws RuntimeException {
        List<ScrapMsg> scrapMsg;
        if (YOUDAO_ABBREVIATION.equals(type)) {
            List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
            List<User> users = UserJedisUtil.getUserByProvinces(provinces);
            scrapMsg = DMJedisUtil.getScrapMsgByUsers(users);
            if (CollectionUtils.isEmpty(scrapMsg)) {
                // no cache
                // cache
                DMJedisUtil.initializationScrapMsg(dataManagementMapper.getScrapMsg());
                scrapMsg = DMJedisUtil.getScrapMsgByUsers(users);
            }
        } else {
            return null;
        }
        for (ScrapMsg scrapMsg1 : scrapMsg) {
            scrapMsg1.setRequestUser(UserJedisUtil.getUserById(scrapMsg1.getRequestUserId()));
            scrapMsg1.setEquipmentMsg(getEquipmentMsgById(scrapMsg1.getEquipmentMsgId()));
            scrapMsg1.setCreateTime(PublicUtil.timestampToString(scrapMsg1.getCreateTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        }
        return scrapMsg;
    }

    @Override
    public EquipmentMsg addEquipment(Map map) throws RuntimeException {
        String brand = String.valueOf(map.get("brand"));
        String type = String.valueOf(map.get("type"));
        String size = String.valueOf(map.get("size"));
        if (StringUtils.isEmpty(brand) || StringUtils.isEmpty(type) || StringUtils.isEmpty(size)) {
            throw new RuntimeException("#addEquipment");
        } else {
            EquipmentMsg equipmentMsg = new EquipmentMsg();
            equipmentMsg.setEquipmentBrand(brand);
            equipmentMsg.setEquipmentTypeNum(type);
            equipmentMsg.setSize(size + '"');
            equipmentMsg.setStatus(1);
            Integer row = dataManagementMapper.insertEquipmentMsg(equipmentMsg);
            if (row == 0) {
                throw new RuntimeException("#addEquipment");
            } else {
                DMJedisUtil.addEquipmentMsg(equipmentMsg);
                return equipmentMsg;
            }

        }
    }

    @Override
    public List<CarType> getCarTypeMsg() throws RuntimeException {
        List<CarType> carTypeList = DMJedisUtil.getAllCarType();
        if (CollectionUtils.isEmpty(carTypeList)) {
            // no cache
            // cache
            DMJedisUtil.initializationCarType(dataManagementMapper.getAllCarType());
            carTypeList = DMJedisUtil.getAllCarType();
        }
        return carTypeList;
    }

    @Override
    public CarType addCarType(Map map) throws RuntimeException {
        String brand = String.valueOf(map.get("brand"));
        String subsidiary = String.valueOf(map.get("subsidiary"));
        CarType carType = new CarType();
        carType.setBrand(brand);
        carType.setSubsidiary(subsidiary);
        carType.setStatus(1);
        Integer row = dataManagementMapper.insertCarType(carType);
        if (row == 0) {
            return null;
        } else {
            DMJedisUtil.addCarType(carType);
            return carType;
        }
    }

    @Transactional
    @Override
    public PurchaseMsg addPurchaseMsg(Map map) throws RuntimeException {
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
        if (!row.equals(1)) {
            throw new RuntimeException("新增采购信息失败！");
        } else {
            purchaseMsg.setConsigneeUser(UserJedisUtil.getUserById(purchaseMsg.getConsigneeUserId()));
            purchaseMsg.setPurchaseUser(UserJedisUtil.getUserById(purchaseMsg.getPurchaseUserId()));
            purchaseMsg.setEquipmentMsg(getEquipmentMsgById(purchaseMsg.getEquipmentMsgId()));
            // 添加库存
            // 获取该用户库存
            boolean index = false;
            InventoryMsg inventoryMsg1 = null;
            List<InventoryMsg> InventoryMsgs = DMJedisUtil.getInventoryMsgByTPId(Integer.valueOf(consigneeUserId));
            if (CollectionUtils.isEmpty(InventoryMsgs)) {
                // no cache
                // update cache
                DMJedisUtil.initializationInventoryMsg(dataManagementMapper.getInventoryMsg());
                InventoryMsgs = DMJedisUtil.getInventoryMsgByTPId(Integer.valueOf(consigneeUserId));
            }
            // 判断是否有该设备库存
            for (InventoryMsg inventoryMsg : InventoryMsgs) {
                if (inventoryMsg.getEquipmentMsgId() == purchaseMsg.getEquipmentMsgId()) {

                    index = true;
                    inventoryMsg1 = inventoryMsg;
                    break;
                }
            }
            if (index && inventoryMsg1 != null) {
                // 有该库存信息，更新库存
                InventoryMsg inventoryMsg = new InventoryMsg();
                inventoryMsg.setId(inventoryMsg1.getId());
                inventoryMsg.setInPurchase(count);
                Integer line = dataManagementMapper.updateInventoryMsg(inventoryMsg);
                if (line != 1) {
                    // 库存更新失败，回调
                    throw new RuntimeException("更新库存信息失败！");
                }
                // update cache
                inventoryMsg = dataManagementMapper.getInventoryMsgById(inventoryMsg1.getId());
                DMJedisUtil.updateInventoryMsg(inventoryMsg);
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
                if (line != 1) {
                    throw new RuntimeException("添加库存失败！");
                }
                // add cache
                DMJedisUtil.addInventoryMsg(inventoryMsg);
            }
            purchaseMsg.setPurchaseTime(PublicUtil.timestampToString(purchaseMsg.getPurchaseTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            // add PurchaseMsg cache
            purchaseMsg = dataManagementMapper.getPurchaseMsgById(purchaseMsg.getId());
            DMJedisUtil.addPurchaseMsg(purchaseMsg);
            return purchaseMsg;
        }
    }

    @Override
    @Transactional
    public boolean scrapPurchaseMsg(Map map) throws RuntimeException {
        String userId = String.valueOf(map.get("userId"));
        String purchaseMsgId = String.valueOf(map.get("purchaseMsgId"));
        // 获取采购单据信息
        PurchaseMsg purchaseMsg = DMJedisUtil.getPurchaseMsgById(purchaseMsgId);
        if (purchaseMsg == null) {
            // no cache
            // update cache
            DMJedisUtil.initializationPurchaseMsg(dataManagementMapper.getPurchaseMsg());
            purchaseMsg = DMJedisUtil.getPurchaseMsgById(purchaseMsgId);
        }
        // 单据null 或者 创建人不是该用户 则失败
        if (purchaseMsg == null || purchaseMsg.getPurchaseUserId() != Integer.valueOf(userId)) {
            throw new RuntimeException("错误！#scrapPurchaseMsg");
        } else {
            PurchaseMsg purchaseMsg1 = new PurchaseMsg();
            purchaseMsg1.setStatus(0);
            purchaseMsg1.setId(Integer.valueOf(purchaseMsgId));
            purchaseMsg1.setScrapTime(String.valueOf(System.currentTimeMillis()));
            // 修改状态
            Integer row = dataManagementMapper.updatePurchaseMsgStatus(purchaseMsg1);
            if (!row.equals(1)) {
                throw new RuntimeException("更新采购单据状态失败");
            }
            // 减少库存的采购中数量
            purchaseMsg1 = dataManagementMapper.getPurchaseMsgById(Integer.valueOf(purchaseMsgId));
            // update PurchaseMsg cache
            DMJedisUtil.updatePurchaseMsg(purchaseMsg1);
            // 更新库存
            // 获取该库存
            InventoryMsg inventoryMsg= dataManagementMapper.getInventoryMsgByEquipmentMsgIdAndUserId(purchaseMsg1.getEquipmentMsgId(), purchaseMsg1.getConsigneeUserId());
            if (inventoryMsg == null){
                throw new RuntimeException("");
            }
            InventoryMsg inventoryMsg1 = new InventoryMsg();
            inventoryMsg1.setId(inventoryMsg.getId());
            inventoryMsg1.setInPurchase(-purchaseMsg1.getCount());
            dataManagementMapper.updateInventoryMsg(inventoryMsg1);
            DMJedisUtil.updateInventoryMsg(dataManagementMapper.getInventoryMsgById(inventoryMsg.getId()));
            return true;
        }
    }

    @Override
    public boolean receivePurchaseMsg(Map map) throws RuntimeException {
        String userId = String.valueOf(map.get("userId"));
        String purchaseMsgId = String.valueOf(map.get("purchaseMsgId"));
        // 获取单据信息
        PurchaseMsg purchaseMsg = DMJedisUtil.getPurchaseMsgById(purchaseMsgId);
        if (purchaseMsg == null) {
            // no cache
            // update cache
            DMJedisUtil.initializationPurchaseMsg(dataManagementMapper.getPurchaseMsg());
            purchaseMsg = DMJedisUtil.getPurchaseMsgById(purchaseMsgId);
        }
        if (purchaseMsg == null || purchaseMsg.getConsigneeUserId() != Integer.valueOf(userId) || StringUtils.isEmpty(userId)) {
            throw new RuntimeException("错误！#receivePurchaseMsg");
        } else {
            // 收货操作
            // 采购单据状态更改/补相应时间
            PurchaseMsg purchaseMsg1 = new PurchaseMsg();
            purchaseMsg1.setStatus(2);
            purchaseMsg1.setArriveTime(String.valueOf(System.currentTimeMillis()));
            purchaseMsg1.setId(Integer.valueOf(purchaseMsgId));
            Integer row = dataManagementMapper.updatePurchaseMsgStatus(purchaseMsg1);
            if (!row.equals(1)) {
                throw new RuntimeException("采购单据状态更改失败！");
            }
            purchaseMsg1 = dataManagementMapper.getPurchaseMsgById(Integer.valueOf(purchaseMsgId));
            // 收货人 正常库存增加，采购中库存减少
            InventoryMsg inventoryMsg1 = dataManagementMapper.getInventoryMsgByEquipmentMsgIdAndUserId(purchaseMsg1.getEquipmentMsgId(), purchaseMsg1.getConsigneeUserId());
            InventoryMsg inventoryMsg = new InventoryMsg();
            inventoryMsg.setId(inventoryMsg1.getId());
            inventoryMsg.setThirdPartyTerraceId(Integer.valueOf(userId));
            inventoryMsg.setEquipmentMsgId(purchaseMsg.getEquipmentMsgId());
            inventoryMsg.setAwaitInstall(purchaseMsg.getCount());
            inventoryMsg.setInPurchase(-purchaseMsg.getCount());
            Integer line = dataManagementMapper.updateInventoryMsg(inventoryMsg);
            if (!line.equals(1)) {
                throw new RuntimeException("库存更新失败！");
            }
            // update PurchaseMsg cache
            purchaseMsg1 = dataManagementMapper.getPurchaseMsgById(Integer.valueOf(purchaseMsgId));
            DMJedisUtil.updatePurchaseMsg(purchaseMsg1);
            // update InventoryMsg cache
            inventoryMsg = dataManagementMapper.getInventoryMsgById(inventoryMsg1.getId());
            DMJedisUtil.updateInventoryMsg(inventoryMsg);
            return true;

        }
    }

    @Override
    public CheckIccidResult checkIccid(String iccid) throws RuntimeException {
        CheckIccidResult checkIccidResult = new CheckIccidResult();
        boolean b = IccidJedisUtil.checkIccid(iccid);
        if (!b) {
            checkIccidResult.setResult(false);
            checkIccidResult.setMessage("Iccid有误或已被占用,请检查Iccid！");
        } else {
            checkIccidResult.setResult(true);
        }
        return checkIccidResult;
    }

    @Override
    public List<Iccid> getIccidsByStatus(int status) throws RuntimeException {
        List<Iccid> iccids = IccidJedisUtil.getIccidsByStatus(status);
        return iccids;
    }

    @Transactional
    @Override
    public List<Channel> getChangeByUser(User user) throws RuntimeException {
        // 获取区域城市集合
        List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
        List<Channel> channels = DMJedisUtil.getChannelByProvinces(provinces);
        if (CollectionUtils.isEmpty(channels)) {
            // no cache
            // update cache
            DMJedisUtil.initializationChannel(dataManagementMapper.getChannel());
            channels = DMJedisUtil.getChannelByProvinces(provinces);
        }
        return channels;
    }


    /**
     * 根据id获取对应的设备信息
     *
     * @param id
     * @return
     */
    public EquipmentMsg getEquipmentMsgById(Integer id) {
        EquipmentMsg equipmentMsg = DMJedisUtil.getEquipmentMsgById(id);
        if (equipmentMsg == null) {
            // no cache
            // cache
            DMJedisUtil.initializationEquipmentMsg(dataManagementMapper.getEquipmentMsg());
            equipmentMsg = DMJedisUtil.getEquipmentMsgById(id);
        }
        return equipmentMsg;
    }

}
