package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.data.CarType;
import com.ydxsj.ydsoldnote.bean.data.Channel;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import com.ydxsj.ydsoldnote.config.redis.JedisPoolUtil;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class DMJedisUtil {

    private static JedisPoolUtil jedisPoolUtil = new JedisPoolUtil("49.234.210.89", 6379, "rk123321");
    private static Jedis jedis = jedisPoolUtil.borrowJedis();

    private static final String DATA_CHANNEL_ID = "DATA_CHANNEL_ID:";
    private static final String DATA_CHANNEL_PROVINCE = "DATA_CHANNEL_PROVINCE:";
    private static final String DATA_CHANNEL_CITY = "DATA_CHANNEL_CITY:";
    private static final String DATA_CHANNEL_CREATE_USER_ID = "DATA_CHANNEL_CREATE_USER_ID:";
    private static final String DATA_CHANNEL_STATUS_ACTIVE = "DATA_CHANNEL_STATUS_ACTIVE";
    private static final String DATA_CHANNEL_STATUS_INACTIVE = "DATA_CHANNEL_STATUS_INACTIVE";
    private static final String DATA_CAR_TYPE_ID = "DATA_CAR_TYPE_ID:";
    private static final String DATA_SELL_TYPE_ID = "DATA_SELL_TYPE_ID:";
    private static final String DATA_EQUIPMENT_MSG_ID = "DATA_EQUIPMENT_MSG_ID:";
    private static final String DATA_INVENTORY_ID = "DATA_INVENTORY_ID:";
    private static final String DATA_INVENTORY_TP_ID = "DATA_INVENTORY_TP_ID:";
    private static final String DATA_PURCHASE_MSG_ID = "DATA_PURCHASE_MSG_ID:";
    private static final String DATA_PURCHASE_USER_ID = "DATA_PURCHASE_USER_ID:";
    private static final String DATA_PURCHASE_CONSIGNEE_ID = "DATA_PURCHASE_CONSIGNEE_ID:";
    private static final String DATA_SCRAP_MSG_ID = "DATA_SCRAP_MSG_ID:";
    private static final String DATA_SCRAP_USER_ID = "DATA_SCRAP_USER_ID:";
    private static final String DATA_EQUIPMENT_CHANGE_MSG_ID = "DATA_EQUIPMENT_CHANGE_MSG_ID:";
    private static final String DATA_EQUIPMENT_CHANGE_USER_ID = "DATA_EQUIPMENT_CHANGE_USER_ID:";
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
        initialization();
    }

    //  初始化信息
    public static void initialization() {
        jedis.select(4);
        List<Channel> channelList = dmJedisUtil.dataManagementMapper.getChannel();
        for (Channel channel : channelList) {
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
        }
        List<CarType> carTypes = dmJedisUtil.dataManagementMapper.getAllCarType();
        for (CarType carType : carTypes) {
            // 车型信息-id - 车型信息
            jedis.set(DATA_CAR_TYPE_ID + carType.getId(), JSON.toJSONString(carType));
        }
        List<SellType> sellTypes = dmJedisUtil.dataManagementMapper.getSellTypes();
        for (SellType sellType : sellTypes) {
            // 销售类型-id - 销售类型信息
            jedis.set(DATA_SELL_TYPE_ID + sellType.getId(), JSON.toJSONString(sellType));
        }
        List<EquipmentMsg> equipmentMsgs = dmJedisUtil.dataManagementMapper.getEquipmentMsg();
        for (EquipmentMsg equipmentMsg : equipmentMsgs) {
            // 设备型号-id - 设备信息
            jedis.set(DATA_EQUIPMENT_MSG_ID + equipmentMsg.getId(), JSON.toJSONString(equipmentMsg));
        }
        List<InventoryMsg> inventoryMsgs = dmJedisUtil.dataManagementMapper.getInventoryMsg();
        for (InventoryMsg inventoryMsg : inventoryMsgs) {
            // 设备库存-id - 设备库存信息
            jedis.set(DATA_INVENTORY_ID + inventoryMsg.getId(), JSON.toJSONString(inventoryMsg));
            // 设备库存-平台id - id
            jedis.sadd(DATA_INVENTORY_TP_ID + inventoryMsg.getThirdPartyTerraceId(), inventoryMsg.getId() + "");
        }
        List<PurchaseMsg> purchaseMsgs = dmJedisUtil.dataManagementMapper.getPurchaseMsg();
        for (PurchaseMsg purchaseMsg : purchaseMsgs) {
            // 采购记录-id - 采购信息
            jedis.set(DATA_PURCHASE_MSG_ID + purchaseMsg.getId(), JSON.toJSONString(purchaseMsg));
            // 采购记录-采购人id - id
            jedis.sadd(DATA_PURCHASE_USER_ID + purchaseMsg.getPurchaseUserId(), purchaseMsg.getId() + "");
            // 采购记录-收货人id - id
            jedis.sadd(DATA_PURCHASE_CONSIGNEE_ID + purchaseMsg.getConsigneeUserId(), purchaseMsg.getId() + "");
        }
        List<ScrapMsg> scrapMsgs = dmJedisUtil.dataManagementMapper.getScrapMsg();
        for (ScrapMsg scrapMsg : scrapMsgs) {
            // 报废记录-id - 报废信息
            jedis.set(DATA_SCRAP_MSG_ID + scrapMsg.getId(), JSON.toJSONString(scrapMsg));
            // 报废记录-报废人id - id
            jedis.sadd(DATA_SCRAP_USER_ID + scrapMsg.getRequestUserId(), scrapMsg.getId() + "");
        }

        List<ChangeMsg> changeMsgs = dmJedisUtil.dataManagementMapper.getChangeMsg();
        for (ChangeMsg changeMsg : changeMsgs) {
            // 更换记录-id - 更换信息
            jedis.set(DATA_EQUIPMENT_CHANGE_MSG_ID + changeMsg.getId(), JSON.toJSONString(changeMsg));
            // 更换记录-更换人id - id
            jedis.set(DATA_EQUIPMENT_CHANGE_USER_ID + changeMsg.getThirdPartyTerraceId(), changeMsg.getId() + "");
        }
        List<MaintainMsg> maintainMsgs = dmJedisUtil.dataManagementMapper.getMaintainMsg();
        for (MaintainMsg maintainMsg : maintainMsgs) {
            // 维修记录-id - 维修信息
            jedis.set(DATA_EQUIPMENT_MAINTAIN_MSG_ID + maintainMsg.getId(), JSON.toJSONString(maintainMsg));
            // 维修记录-维修人id - id
            jedis.sadd(DATA_EQUIPMENT_MAINTAIN_USER_ID + maintainMsg.getRequestUserId(), maintainMsg.getId() + "");
            // 维修记录-收货人 - id
            jedis.sadd(DATA_EQUIPMENT_MAINTAIN_CONSIGNEE_USER_ID + maintainMsg.getConsigneeUserId(), maintainMsg.getId() + "");
        }
        List<TransferMsg> transferMsgs = dmJedisUtil.dataManagementMapper.getTransferMsg();
        for (TransferMsg transferMsg : transferMsgs) {
            // 转移记录-id 转移信息
            jedis.set(DATA_EQUIPMENT_TRANSFER_MSG_ID + transferMsg.getId(), JSON.toJSONString(transferMsg));
            // 转移记录-转移人id - id
            jedis.sadd(DATA_EQUIPMENT_TRANSFER_REQUEST_USER_ID + transferMsg.getRequestUserId(), transferMsg.getId() + "");
            // 转移记录-收货人id - id
            jedis.sadd(DATA_EQUIPMENT_TRANSFER_CONSIGNEE_USER_ID + transferMsg.getConsigneeUserId(), transferMsg.getId() + "");
        }

    }
}
