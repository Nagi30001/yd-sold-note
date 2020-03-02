package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DMJedisUtil {

    private static final String DATA_CHANNEL_ID = "DATA_CHANNEL_ID:";
    private static final String DATA_CHANNEL_PROVINCE = "DATA_CHANNEL_PROVINCE:";
    private static final String DATA_CHANNEL_CITY = "DATA_CHANNEL_CITY:";
    private static final String DATA_CHANNEL_CREATE_USER_ID = "DATA_CHANNEL_CREATE_USER_ID:";
    private static final String DATA_CHANNEL_STATUS_ACTIVE = "DATA_CHANNEL_STATUS_ACTIVE";
    private static final String DATA_CHANNEL_STATUS_INACTIVE = "DATA_CHANNEL_STATUS_INACTIVE";
    private static final String DATA_CAR_TYPE_ID = "DATA_CAR_TYPE_ID:";
    private static final String DATA_CAR_TYPE = "DATA_CAR_TYPE";
    private static final String DATA_CAR_BRAND = "DATA_CAR_BRAND";
    private static final String DATA_CAR_SUB_BRAND = "DATA_CAR_SUB_BRAND:";
    private static final String DATA_SELL_TYPE_ID = "DATA_SELL_TYPE_ID:";
    private static final String DATA_SELL_TYPE = "DATA_SELL_TYPE";
    private static final String DATA_ADDITION_TYPE_ID = "DATA_ADDITION_TYPE_ID:";
    private static final String DATA_ADDITION_TYPE = "DATA_ADDITION_TYPE";
    private static final String DATA_EQUIPMENT_MSG_ID = "DATA_EQUIPMENT_MSG_ID:";
    private static final String DATA_EQUIPMENT_MSG = "DATA_EQUIPMENT_MSG";
    private static final String DATA_INVENTORY_ID = "DATA_INVENTORY_ID:";
    private static final String DATA_INVENTORY_TP_ID = "DATA_INVENTORY_TP_ID:";
    private static final String DATA_PURCHASE_MSG_ID = "DATA_PURCHASE_MSG_ID:";
    private static final String DATA_PURCHASE_USER_ID = "DATA_PURCHASE_USER_ID:";
    private static final String DATA_PURCHASE_CONSIGNEE_ID = "DATA_PURCHASE_CONSIGNEE_ID:";
    private static final String DATA_SCRAP_MSG_ID = "DATA_SCRAP_MSG_ID:";
    private static final String DATA_SCRAP_USER_ID = "DATA_SCRAP_USER_ID:";
    private static final String DATA_EQUIPMENT_CHANGE_MSG_ID = "DATA_EQUIPMENT_CHANGE_MSG_ID:";
    private static final String DATA_EQUIPMENT_CHANGE_USER_ID = "DATA_EQUIPMENT_CHANGE_USER_ID:";
    private static final String DATA_EQUIPMENT_CHANGE_CHECK_USER_ID = "DATA_EQUIPMENT_CHANGE_CHECK_USER_ID:";
    private static final String DATA_EQUIPMENT_MAINTAIN_MSG_ID = "DATA_EQUIPMENT_MAINTAIN_MSG_ID:";
    private static final String DATA_EQUIPMENT_MAINTAIN_USER_ID = "DATA_EQUIPMENT_MAINTAIN_USER_ID:";
    private static final String DATA_EQUIPMENT_MAINTAIN_CONSIGNEE_USER_ID = "DATA_EQUIPMENT_MAINTAIN_CONSIGNEE_USER_ID:";
    private static final String DATA_EQUIPMENT_TRANSFER_MSG_ID = "DATA_EQUIPMENT_TRANSFER_MSG_ID:";
    private static final String DATA_EQUIPMENT_TRANSFER_REQUEST_USER_ID = "DATA_EQUIPMENT_TRANSFER_REQUEST_USER_ID:";
    private static final String DATA_EQUIPMENT_TRANSFER_CONSIGNEE_USER_ID = "DATA_EQUIPMENT_TRANSFER_CONSIGNEE_USER_ID:";
    private static DMJedisUtil dmJedisUtil;
    @Autowired
    private DataManagementMapper dataManagementMapper;


    @PostConstruct
    public void init() {
        dmJedisUtil = this;
        dmJedisUtil.dataManagementMapper = this.dataManagementMapper;
//        initialization();
    }

    //  初始化信息
    public static void initialization() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 渠道信息初始化
            List<Channel> channelList = dmJedisUtil.dataManagementMapper.getChannel();
            initializationChannel(channelList);
            // 车型信息初始化
            List<CarType> carTypes = dmJedisUtil.dataManagementMapper.getAllCarType();
            initializationCarType(carTypes);
            // 车辆品牌信息初始化
            List<CarType> carTypess = dmJedisUtil.dataManagementMapper.getCarType();
            initializationCarBrandAndCarSubBrand(carTypess);
            // 销售类型信息初始化
            List<SellType> sellTypes = dmJedisUtil.dataManagementMapper.getSellTypes();
            initializationSellType(sellTypes);
            // 附加业务类型信息初始化
            List<Addition> additions = dmJedisUtil.dataManagementMapper.getAdditions();
            initializationAddition(additions);
            // 设备型号信息初始化
            List<EquipmentMsg> equipmentMsgs = dmJedisUtil.dataManagementMapper.getEquipmentMsg();
            initializationEquipmentMsg(equipmentMsgs);
            // 设备库存初始化
            List<InventoryMsg> inventoryMsgs = dmJedisUtil.dataManagementMapper.getInventoryMsg();
            initializationInventoryMsg(inventoryMsgs);
            // 采购信息初始化
            List<PurchaseMsg> purchaseMsgs = dmJedisUtil.dataManagementMapper.getPurchaseMsg();
            initializationPurchaseMsg(purchaseMsgs);
            // 报废信息初始化
            List<ScrapMsg> scrapMsgs = dmJedisUtil.dataManagementMapper.getScrapMsg();
            initializationScrapMsg(scrapMsgs);
            // 更换记录初始化
            List<ChangeMsg> changeMsgs = dmJedisUtil.dataManagementMapper.getChangeMsg();
            initializationChangeMsg(changeMsgs);
            // 维修记录
            List<MaintainMsg> maintainMsgs = dmJedisUtil.dataManagementMapper.getMaintainMsg();
            initializationMaintainMsg(maintainMsgs);
            // 转移记录初始化
            List<TransferMsg> transferMsgs = dmJedisUtil.dataManagementMapper.getTransferMsg();
            initializationTransferMsg(transferMsgs);
        } catch (Exception e) {
            throw new RuntimeException("");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
        jedis.select(4);
    }

    /**
     * 初始化车辆品牌-子品牌信息
     *
     * @param carTypes
     */
    public static void initializationCarBrandAndCarSubBrand(List<CarType> carTypes) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 品牌/子品牌
            for (CarType carType : carTypes) {
                jedis.sadd(DATA_CAR_BRAND, JSON.toJSONString(carType));
                String[] split = carType.getSubsidiary().split(",");
                for (int i = 0; i < split.length; i++) {
                    jedis.sadd(DATA_CAR_SUB_BRAND + carType.getBrand(), split[i]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化车辆品牌信息错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }

    /**
     * 初始化销售类型信息
     *
     * @param sellTypes
     */
    public static void initializationSellType(List<SellType> sellTypes) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            for (SellType sellType : sellTypes) {
                // 销售类型-id - 销售类型信息
                jedis.set(DATA_SELL_TYPE_ID + sellType.getId(), JSON.toJSONString(sellType));
                jedis.sadd(DATA_SELL_TYPE, sellType.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("销售类型信息初始化错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 获得全部销售类型信息
     *
     * @return
     */
    public static List<SellType> getSellType() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers = jedis.smembers(DATA_SELL_TYPE);
            List<SellType> sellTypes = new ArrayList<>();
            for (String s : smembers) {
                sellTypes.add(JSON.parseObject(jedis.get(DATA_SELL_TYPE_ID + s), SellType.class));
            }
            return sellTypes;
        } catch (Exception e) {
            throw new RuntimeException("获取销售类型信息失败！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 初始化附加业务
     *
     * @param additions
     */
    public static void initializationAddition(List<Addition> additions) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            for (Addition addition : additions) {
                // 销售类型-id - 销售类型信息
                jedis.set(DATA_ADDITION_TYPE_ID + addition.getId(), JSON.toJSONString(addition));
                jedis.sadd(DATA_ADDITION_TYPE, addition.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("销售类型信息初始化错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void initializationChannel(List<Channel> channels) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            for (Channel channel : channels) {
                addChannel(channel);
            }
        } catch (Exception e) {
            throw new RuntimeException("渠道信息初始化错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void initializationEquipmentMsg(List<EquipmentMsg> equipmentMsgs) {
        for (EquipmentMsg equipmentMsg : equipmentMsgs) {
            addEquipmentMsg(equipmentMsg);
        }
    }

    public static void initializationInventoryMsg(List<InventoryMsg> inventoryMsgs) {
        for (InventoryMsg inventoryMsg : inventoryMsgs) {
            addInventoryMsg(inventoryMsg);
        }
    }

    public static void initializationTransferMsg(List<TransferMsg> transferMsgs) {
        for (TransferMsg transferMsg : transferMsgs) {
            addTransferMsg(transferMsg);
        }
    }

    public static void initializationChangeMsg(List<ChangeMsg> changeMsgs) {
        for (ChangeMsg changeMsg : changeMsgs) {
            addChannelMsg(changeMsg);
        }
    }

    public static void initializationMaintainMsg(List<MaintainMsg> maintainMsgs) {
        for (MaintainMsg maintainMsg : maintainMsgs) {
            addMaintainMgs(maintainMsg);
        }
    }

    public static void initializationPurchaseMsg(List<PurchaseMsg> purchaseMsgs) {
        for (PurchaseMsg purchaseMsg : purchaseMsgs) {
            addPurchaseMsg(purchaseMsg);
        }
    }

    public static void initializationScrapMsg(List<ScrapMsg> scrapMsgs) {
        for (ScrapMsg scrapMsg : scrapMsgs) {
            addScrapMsg(scrapMsg);
        }
    }

    public static void initializationCarType(List<CarType> carTypes) {
        for (CarType carType : carTypes) {
            addCarType(carType);
        }
    }


    /**
     * 获取全部附加业务信息
     *
     * @return
     */
    public static List<Addition> getAddition() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers = jedis.smembers(DATA_ADDITION_TYPE);
            List<Addition> additions = new ArrayList<>();
            for (String s : smembers) {
                additions.add(JSON.parseObject(jedis.get(DATA_ADDITION_TYPE_ID + s), Addition.class));
            }
            return additions;
        } catch (Exception e) {
            throw new RuntimeException("获取附加业务类型信息失败！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 添加车辆品牌信息
     *
     * @param brand
     */
    public static void addCarBrand(String brand) {
        Jedis jedis = null;
        try {
            jedis.select(4);
            jedis.sadd(DATA_CAR_BRAND, brand);
        } catch (Exception e) {
            throw new RuntimeException("添加车辆品牌错误！");
        }
    }

    /**
     * 删除车辆品牌信息
     *
     * @param brand
     */
    public static void deleteCarBrand(String brand) {
        Jedis jedis = null;
        try {
            jedis.select(4);
            jedis.srem(DATA_CAR_BRAND, brand);
        } catch (Exception e) {
            throw new RuntimeException("删除车辆品牌错误！");
        }
    }

    /**
     * 添加车辆子品牌信息
     *
     * @param brand
     * @param subBrand
     */
    public static void addCarSubBrand(String brand, String subBrand) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            jedis.sadd(DATA_CAR_SUB_BRAND + brand, subBrand);
        } catch (Exception e) {
            throw new RuntimeException("添加子品牌错误");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 删除车辆子品牌信息
     *
     * @param brand
     * @param subBrand
     */
    public static void delectCarSubBrand(String brand, String subBrand) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            jedis.srem(DATA_CAR_SUB_BRAND + brand, subBrand);
        } catch (Exception e) {
            throw new RuntimeException("删除子品牌错误");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 获取车辆品牌&子品牌信息 - 字符串
     *
     * @return
     */
    public static List<CarType> getCarBrandAndSubBrand() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            List<CarType> carTypeList = new ArrayList<>();
            Set<String> catyoe = jedis.smembers(DATA_CAR_BRAND);
            for (String s : catyoe) {
                CarType carType = JSON.parseObject(s, CarType.class);
                carType.setSubsidiarys(carType.getSubsidiary().split(","));
                carTypeList.add(carType);
            }
            return carTypeList;
        } catch (Exception e) {
            throw new RuntimeException("查询品牌信息错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 获取某省的渠道信息
     *
     * @param province
     * @return
     */
    public static List<Channel> getChannelByProvince(Province province) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            List<Channel> channels = new ArrayList<>();
            Set<String> smembers = jedis.smembers(DATA_CHANNEL_PROVINCE + province.getProvince());
            for (String s : smembers) {
                channels.add(JSON.parseObject(jedis.get(DATA_CHANNEL_ID + s), Channel.class));
            }
            return channels;
        } catch (Exception e) {
            throw new RuntimeException("获取渠道信息错误");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 根据省集合查询渠道信息
     *
     * @param provinces
     * @return
     */
    public static List<Channel> getChannelByProvinces(List<Province> provinces) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            List<Channel> channels = new ArrayList<>();
            for (Province province : provinces) {
                channels = (List<Channel>) CollectionUtils.union(channels, getChannelByProvince(province));
            }
            return channels;
        } catch (Exception e) {
            throw new RuntimeException("获取渠道信息集错误");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 根据 id 获取渠道信息
     *
     * @param id
     * @return
     */
    public static Channel getChannelById(Integer id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            return JSON.parseObject(jedis.get(DATA_CHANNEL_ID + id), Channel.class);
        } catch (Exception e) {
            throw new RuntimeException("获取渠道信息集错误");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 添加渠道信息缓存
     *
     * @param channel
     */
    public static void addChannel(Channel channel) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 渠道信息-id - 渠道信息
            jedis.set(DATA_CHANNEL_ID + channel.getId(), JSON.toJSONString(channel));
            // 渠道信息-省份 - id
            jedis.sadd(DATA_CHANNEL_PROVINCE + channel.getProvince(), channel.getId() + "");
            // 渠道信息-城市 - id
            jedis.sadd(DATA_CHANNEL_CITY + channel.getCity(), channel.getId() + "");
            // 渠道信息-创建人id - id
            jedis.sadd(DATA_CHANNEL_CREATE_USER_ID + channel.getCreateId(), channel.getId() + "");
            // 渠道信息-状态 - id
            if (channel.getStatus() == 0) {
                jedis.sadd(DATA_CHANNEL_STATUS_INACTIVE, channel.getId() + "");
            } else if (channel.getStatus() == 1) {
                jedis.sadd(DATA_CHANNEL_STATUS_ACTIVE, channel.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("渠道信息添加错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 删除渠道信息缓存
     *
     * @param channel
     */
    public static void deleteChannel(Channel channel) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 渠道信息-id - 渠道信息
            jedis.del(DATA_CHANNEL_ID + channel.getId(), JSON.toJSONString(channel));
            // 渠道信息-省份 - id
            jedis.srem(DATA_CHANNEL_PROVINCE + channel.getProvince(), channel.getId() + "");
            // 渠道信息-城市 - id
            jedis.srem(DATA_CHANNEL_CITY + channel.getCity(), channel.getId() + "");
            // 渠道信息-创建人id - id
            jedis.srem(DATA_CHANNEL_CREATE_USER_ID + channel.getCreateId(), channel.getId() + "");
            // 渠道信息-状态 - id
            if (channel.getStatus() == 0) {
                jedis.sadd(DATA_CHANNEL_STATUS_INACTIVE, channel.getId() + "");
            } else if (channel.getStatus() == 1) {
                jedis.sadd(DATA_CHANNEL_STATUS_ACTIVE, channel.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("渠道信息删除错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 更新渠道信息缓存
     *
     * @param oldChannel
     * @param newChannel
     */
    public static void updateChannel(Channel oldChannel, Channel newChannel) {
        deleteChannel(oldChannel);
        addChannel(newChannel);
    }


    public static void addEquipmentMsg(EquipmentMsg equipmentMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            jedis.set(DATA_EQUIPMENT_MSG_ID + equipmentMsg.getId(), JSON.toJSONString(equipmentMsg));
            jedis.sadd(DATA_EQUIPMENT_MSG, equipmentMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息添加错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 获取全部设备信息
     *
     * @return
     */
    public static List<EquipmentMsg> getAllEquipmentMsg() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers = jedis.smembers(DATA_EQUIPMENT_MSG);
            List<EquipmentMsg> equipmentMsg = new ArrayList<>();
            for (String s : smembers) {
                equipmentMsg.add(JSON.parseObject(jedis.get(DATA_EQUIPMENT_MSG_ID + s), EquipmentMsg.class));
            }
            return equipmentMsg;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<InventoryMsg> getInventoryMsgByTPId(Integer id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers = jedis.smembers(DATA_INVENTORY_TP_ID + id);
            List<InventoryMsg> inventoryMsgs = new ArrayList<>();
            for (String s : smembers) {
                inventoryMsgs.add(JSON.parseObject(jedis.get(DATA_INVENTORY_ID), InventoryMsg.class));
            }
            return inventoryMsgs;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<InventoryMsg> getInventoryMsgByTPIds(List<User> userList) {
        List<InventoryMsg> inventoryMsgs = new ArrayList<>();
        for (User user : userList) {
            inventoryMsgs = (List<InventoryMsg>) CollectionUtils.union(inventoryMsgs, getInventoryMsgByTPId(user.getId()));
        }
        return inventoryMsgs;
    }

    public static void addInventoryMsg(InventoryMsg inventoryMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 设备库存-id - 设备库存信息
            jedis.set(DATA_INVENTORY_ID + inventoryMsg.getId(), JSON.toJSONString(inventoryMsg));
            // 设备库存-平台id - id
            jedis.sadd(DATA_INVENTORY_TP_ID + inventoryMsg.getThirdPartyTerraceId(), inventoryMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void updateInventoryMsg(InventoryMsg inventoryMsg) {
        addInventoryMsg(inventoryMsg);
    }

    public static EquipmentMsg getEquipmentMsgById(Integer equipmentMsgId) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            return JSON.parseObject(jedis.get(DATA_EQUIPMENT_MSG_ID + equipmentMsgId), EquipmentMsg.class);
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<TransferMsg> getPTAndTransferMsg(User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> sinter = jedis.sunion(DATA_EQUIPMENT_TRANSFER_REQUEST_USER_ID + user.getId(), DATA_EQUIPMENT_TRANSFER_CONSIGNEE_USER_ID + user.getId());
            List<TransferMsg> transferMsgs = new ArrayList<>();
            for (String str : sinter) {
                transferMsgs.add(JSON.parseObject(jedis.get(DATA_EQUIPMENT_TRANSFER_MSG_ID + str), TransferMsg.class));
            }
            return transferMsgs;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addTransferMsg(TransferMsg transferMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 转移记录-id 转移信息
            jedis.set(DATA_EQUIPMENT_TRANSFER_MSG_ID + transferMsg.getId(), JSON.toJSONString(transferMsg));
            // 转移记录-转移人id - id
            jedis.sadd(DATA_EQUIPMENT_TRANSFER_REQUEST_USER_ID + transferMsg.getRequestUserId(), transferMsg.getId() + "");
            // 转移记录-收货人id - id
            jedis.sadd(DATA_EQUIPMENT_TRANSFER_CONSIGNEE_USER_ID + transferMsg.getConsigneeUserId(), transferMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<TransferMsg> getTransferMsgByUsers(List<User> userList) {
        List<TransferMsg> transferMsgs = new ArrayList<>();
        for (User user : userList) {
            transferMsgs = (List<TransferMsg>) CollectionUtils.union(transferMsgs, getPTAndTransferMsg(user));
        }
        return transferMsgs;
    }

    public static List<ChangeMsg> getChannelMsgByUser(User user, boolean type) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers;
            if (type) {
                smembers = jedis.smembers(DATA_EQUIPMENT_CHANGE_USER_ID + user.getId());
            } else {
                smembers = jedis.smembers(DATA_EQUIPMENT_CHANGE_CHECK_USER_ID + user.getId());
            }
            List<ChangeMsg> changeMsgs = new ArrayList<>();
            for (String s : smembers) {
                changeMsgs.add(JSON.parseObject(jedis.get(DATA_EQUIPMENT_CHANGE_MSG_ID + s), ChangeMsg.class));
            }
            return changeMsgs;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addChannelMsg(ChangeMsg changeMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 更换记录-id - 更换信息
            jedis.set(DATA_EQUIPMENT_CHANGE_MSG_ID + changeMsg.getId(), JSON.toJSONString(changeMsg));
            // 更换记录-更换人id - id
            jedis.sadd(DATA_EQUIPMENT_CHANGE_USER_ID + changeMsg.getThirdPartyTerraceId(), changeMsg.getId() + "");
            // 更换记录-确认人id - id
            jedis.sadd(DATA_EQUIPMENT_CHANGE_CHECK_USER_ID + changeMsg.getCheckUserId(), changeMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<ChangeMsg> getChannelMsgByUsers(List<User> userList) {
        List<ChangeMsg> changeMsgs = new ArrayList<>();
        for (User user : userList) {
            changeMsgs = (List<ChangeMsg>) CollectionUtils.union(changeMsgs, getChannelMsgByUser(user, false));
        }
        return changeMsgs;
    }

    public static void addMaintainMgs(MaintainMsg maintainMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 维修记录-id - 维修信息
            jedis.set(DATA_EQUIPMENT_MAINTAIN_MSG_ID + maintainMsg.getId(), JSON.toJSONString(maintainMsg));
            // 维修记录-维修人id - id
            jedis.sadd(DATA_EQUIPMENT_MAINTAIN_USER_ID + maintainMsg.getRequestUserId(), maintainMsg.getId() + "");
            // 维修记录-收货人 - id
            jedis.sadd(DATA_EQUIPMENT_MAINTAIN_CONSIGNEE_USER_ID + maintainMsg.getConsigneeUserId(), maintainMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<MaintainMsg> getMaintainMgsByUser(User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> sunion = jedis.sunion(DATA_EQUIPMENT_MAINTAIN_USER_ID + user.getId(), DATA_EQUIPMENT_MAINTAIN_CONSIGNEE_USER_ID + user.getId());
            List<MaintainMsg> maintainMsg = new ArrayList<>();
            for (String s : sunion) {
                maintainMsg.add(JSON.parseObject(jedis.get(DATA_EQUIPMENT_MAINTAIN_MSG_ID + s), MaintainMsg.class));
            }
            return maintainMsg;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<MaintainMsg> getMaintainMgsByUsers(List<User> userList) {
        List<MaintainMsg> maintainMsgs = new ArrayList<>();
        for (User user : userList) {
            maintainMsgs = (List<MaintainMsg>) CollectionUtils.union(maintainMsgs, getMaintainMgsByUser(user));
        }
        return maintainMsgs;
    }

    /**
     * 添加采购信息
     *
     * @param purchaseMsg
     */
    public static void addPurchaseMsg(PurchaseMsg purchaseMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 采购记录-id - 采购信息
            jedis.set(DATA_PURCHASE_MSG_ID + purchaseMsg.getId(), JSON.toJSONString(purchaseMsg));
            // 采购记录-采购人id - id
            jedis.sadd(DATA_PURCHASE_USER_ID + purchaseMsg.getPurchaseUserId(), purchaseMsg.getId() + "");
            // 采购记录-收货人id - id
            jedis.sadd(DATA_PURCHASE_CONSIGNEE_ID + purchaseMsg.getConsigneeUserId(), purchaseMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<PurchaseMsg> getPurchaseMsgByUser(User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> sunion = jedis.sunion(DATA_PURCHASE_USER_ID + user.getId(), DATA_PURCHASE_CONSIGNEE_ID + user.getId());
            List<PurchaseMsg> purchaseMsg = new ArrayList<>();
            for (String s : sunion) {
                purchaseMsg.add(getPurchaseMsgById(s));
            }
            return purchaseMsg;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static PurchaseMsg getPurchaseMsgById(String purchaseMsgId) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            return JSON.parseObject(jedis.get(DATA_PURCHASE_MSG_ID + purchaseMsgId), PurchaseMsg.class);
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<PurchaseMsg> getPurchaseMsgByUsers(List<User> users) {
        List<PurchaseMsg> purchaseMsgs = new ArrayList<>();
        for (User user : users) {
            purchaseMsgs = (List<PurchaseMsg>) CollectionUtils.union(purchaseMsgs, getPurchaseMsgByUser(user));
        }
        return purchaseMsgs;
    }

    public static void updatePurchaseMsg(PurchaseMsg purchaseMsg1) {
        addPurchaseMsg(purchaseMsg1);
    }

    public static void addScrapMsg(ScrapMsg scrapMsg) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 报废记录-id - 报废信息
            jedis.set(DATA_SCRAP_MSG_ID + scrapMsg.getId(), JSON.toJSONString(scrapMsg));
            // 报废记录-报废人id - id
            jedis.sadd(DATA_SCRAP_USER_ID + scrapMsg.getRequestUserId(), scrapMsg.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<ScrapMsg> getScrapMsgByUser(User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers = jedis.smembers(DATA_SCRAP_USER_ID + user.getId());
            List<ScrapMsg> scrapMsg = new ArrayList<>();
            for (String s : smembers) {
                scrapMsg.add(JSON.parseObject(jedis.get(DATA_SCRAP_MSG_ID + s), ScrapMsg.class));
            }
            return scrapMsg;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<ScrapMsg> getScrapMsgByUsers(List<User> users) {
        List<ScrapMsg> scrapMsgs = new ArrayList<>();
        for (User user : users) {
            scrapMsgs = (List<ScrapMsg>) CollectionUtils.union(scrapMsgs, getPurchaseMsgByUser(user));
        }
        return scrapMsgs;
    }

    public static void addCarType(CarType carType) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            // 车型信息-id - 车型信息
            jedis.set(DATA_CAR_TYPE_ID + carType.getId(), JSON.toJSONString(carType));
            jedis.sadd(DATA_CAR_TYPE, carType.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<CarType> getAllCarType() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            Set<String> smembers = jedis.smembers(DATA_CAR_TYPE);
            List<CarType> carTypes = new ArrayList<>();
            for (String str : smembers) {
                carTypes.add(JSON.parseObject(jedis.get(DATA_CAR_TYPE_ID + str), CarType.class));
            }
            return carTypes;
        } catch (Exception e) {
            throw new RuntimeException("设备信息获取错误！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }
}
