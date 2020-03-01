package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.config.redis.JedisPoolUtil;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import com.ydxsj.ydsoldnote.mapper.SellReceiptsMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class SellReceiptsJedisUtil {

    private static JedisPoolUtil jedisPoolUtil = new JedisPoolUtil("49.234.210.89", 6379, "rk123321");
    private static Jedis jedis = jedisPoolUtil.borrowJedis();
    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-dd-MM");
    private static final String SELL_RECEIPTS_ID = "SELL_RECEIPTS_ID:";
    private static final String SELL_RECEIPTS_STATUS = "SELL_RECEIPTS_STATUS:";
    private static final String SELL_RECEIPTS_PROVINCE = "SELL_RECEIPTS_PROVINCE:";
    private static final String SELL_RECEIPTS_CITY = "SELL_RECEIPTS_CITY:";
    private static final String SELL_RECEIPTS_CREATE_USER_ID = "SELL_RECEIPTS_CREATE_USER_ID:";
    private static final String SELL_RECEIPTS_INSTALL_TP_ID = "SELL_RECEIPTS_INSTALL_TP_ID:";
    private static final String SELL_RECEIPTS_GATHERING_MSG_ID = "SELL_RECEIPTS_GATHERING_MSG_ID:";
    private static final String SELL_RECEIPTS_YD_GATHERING = "SELL_RECEIPTS_YD_GATHERING";
    private static final String SELL_RECEIPTS_TP_GATHERING = "SELL_RECEIPTS_TP_GATHERING";
    private static final String SELL_RECEIPTS_GATHERING_USER_ID = "SELL_RECEIPTS_GATHERING_USER_ID:";
    private static final String SELL_RECEIPTS_CLIENT_NAME_LIKE = "SELL_RECEIPTS_CLIENT_NAME_LIKE:";
    private static final String SELL_RECEIPTS_CLIENT_CAR_NUM = "SELL_RECEIPTS_CLIENT_CAR_NUM:";
    private static final String SELL_RECEIPTS_CLIENT_CAR_BRAND = "SELL_RECEIPTS_CLIENT_CAR_BRAND:";
    private static final String SELL_RECEIPTS_EQUIPMENT_ID = "SELL_RECEIPTS_EQUIPMENT_ID:";
    private static final String SELL_RECEIPTS_EQUIPMENT_BRAND = "SELL_RECEIPTS_EQUIPMENT_BRAND:";
    private static final String SELL_RECEIPTS_EQUIPMENT_SIZE = "SELL_RECEIPTS_EQUIPMENT_SIZE:";
    private static final String SELL_RECEIPTS_SELL_TYPE_ID = "SELL_RECEIPTS_SELL_TYPE_ID:";
    private static final String SELL_RECEIPTS_ADDITION_TYPE_ID = "SELL_RECEIPTS_ADDITION_TYPE_ID:";
    private static final String SELL_RECEIPTS_CHANNEL_ID = "SELL_RECEIPTS_CHANNEL_ID:";
    private static final String SELL_RECEIPTS_CREATE_TIME_DATE = "SELL_RECEIPTS_CREATE_TIME_DATE:";
    private static final String SELL_RECEIPTS_GATHERING_TIME_DATE = "SELL_RECEIPTS_GATHERING_TIME_DATE:";
    private static final String SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE = "SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE:";
    private static final String SELL_RECEIPTS_SCRAP_TIME_DATE = "SELL_RECEIPTS_SCRAP_TIME_DATE:";
    private static final String SELL_RECEIPTS_REFUND_TIME_DATE = "SELL_RECEIPTS_REFUND_TIME_DATE:";

    private static SellReceiptsJedisUtil sellReceiptsJedisUtil;
    @Autowired
    private SellReceiptsMapper sellReceiptsMapper;
    @Autowired
    private DataManagementMapper dataManagementMapper;

    @PostConstruct
    public void init() {
        sellReceiptsJedisUtil = this;
        sellReceiptsJedisUtil.sellReceiptsMapper = this.sellReceiptsMapper;
        sellReceiptsJedisUtil.dataManagementMapper = this.dataManagementMapper;
//        initialization();
    }

    // 初始化信息
    public static void initialization() {
        jedis.select(3);
        // id - 单据
        List<CarReceipts> list = sellReceiptsJedisUtil.sellReceiptsMapper.getCarReceipts();
        for (CarReceipts carReceipts : list) {
            carReceipts.setGatheringMsg(sellReceiptsJedisUtil.sellReceiptsMapper.getGatheringMsgById(carReceipts.getGatheringMsgId()));
            jedis.set(SELL_RECEIPTS_ID + carReceipts.getId() + "", JSON.toJSONString(carReceipts));
            // 状态 - id
            if (carReceipts.getReceiptsStatus() == -1) {
                jedis.sadd(SELL_RECEIPTS_STATUS + -1, carReceipts.getId() + "");
            } else if (carReceipts.getReceiptsStatus() == 0) {
                jedis.sadd(SELL_RECEIPTS_STATUS + 0, carReceipts.getId() + "");
            } else if (carReceipts.getReceiptsStatus() == 1) {
                jedis.sadd(SELL_RECEIPTS_STATUS + 1, carReceipts.getId() + "");
            } else if (carReceipts.getReceiptsStatus() == 2) {
                jedis.sadd(SELL_RECEIPTS_STATUS + 2, carReceipts.getId() + "");
            } else if (carReceipts.getReceiptsStatus() == 3) {
                jedis.sadd(SELL_RECEIPTS_STATUS + 3, carReceipts.getId() + "");
            } else if (carReceipts.getReceiptsStatus() == 4) {
                jedis.sadd(SELL_RECEIPTS_STATUS + 4, carReceipts.getId() + "");
            }
            // 省份 - id
            jedis.sadd(SELL_RECEIPTS_PROVINCE + carReceipts.getProvince(), carReceipts.getId() + "");
            // 城市 - id
            jedis.sadd(SELL_RECEIPTS_CITY + carReceipts.getCity(), carReceipts.getId() + "");
            // 创建人id - id
            jedis.sadd(SELL_RECEIPTS_CREATE_USER_ID + carReceipts.getUserId(), carReceipts.getId() + "");
            // 安装平台id - id
            jedis.sadd(SELL_RECEIPTS_INSTALL_TP_ID + carReceipts.getThirdPartyTerraceId(), carReceipts.getId() + "");
            // 收款信息id - 收款信息
            jedis.set(SELL_RECEIPTS_GATHERING_MSG_ID + carReceipts.getGatheringMsgId(), JSON.toJSONString(carReceipts.getGatheringMsg()));
            if (carReceipts.getGatheringMsg().getYdGathering() == 1) {
                // YD收款 - id
                jedis.sadd(SELL_RECEIPTS_YD_GATHERING, carReceipts.getId() + "");
            } else if (carReceipts.getGatheringMsg().getThirdPartyGathering() == 1) {
                // PT收款 - id
                jedis.sadd(SELL_RECEIPTS_TP_GATHERING, carReceipts.getId() + "");
            }
            // 收款人id - id
            jedis.sadd(SELL_RECEIPTS_GATHERING_USER_ID + carReceipts.getGatheringMsg().getGatheringUserId(), carReceipts.getId() + "");
            // 客户姓名-拆分 - id
            char[] chars = carReceipts.getClientName().trim().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                jedis.sadd(SELL_RECEIPTS_CLIENT_NAME_LIKE + chars[i], carReceipts.getId() + "");
            }
            // 车辆品牌 - id
            jedis.sadd(SELL_RECEIPTS_CLIENT_CAR_BRAND + carReceipts.getCarBrand(), carReceipts.getId() + "");
            // 车牌号码 - id
            jedis.sadd(SELL_RECEIPTS_CLIENT_CAR_NUM + carReceipts.getClientCarNum(), carReceipts.getId() + "");
            if (!StringUtils.isEmpty(carReceipts.getEquipmentBrand())) {
                // 设备id - id
                Integer i = sellReceiptsJedisUtil.dataManagementMapper.getEquipmentMsgByMsg(carReceipts.getEquipmentBrand(), carReceipts.getEquipmentTypeNum(), carReceipts.getSize());
                jedis.sadd(SELL_RECEIPTS_EQUIPMENT_ID + i, carReceipts.getId() + "");
                // 设备品牌 - id
                jedis.sadd(SELL_RECEIPTS_EQUIPMENT_BRAND + carReceipts.getEquipmentBrand(), carReceipts.getId() + "");
                // 设备尺寸 - id
                jedis.sadd(SELL_RECEIPTS_EQUIPMENT_SIZE + carReceipts.getSize(), carReceipts.getId() + "");
            }
            // 销售类型id - id
            SellType sellType = sellReceiptsJedisUtil.dataManagementMapper.getSellTypeByName(carReceipts.getSellTypeName());
            jedis.sadd(SELL_RECEIPTS_SELL_TYPE_ID + sellType.getId(), carReceipts.getId() + "");
            // 附加业务id - id
            List<String> list1 = Arrays.asList(carReceipts.getAdditionType().split("-"));
            for (String str : list1) {
                Addition addition = sellReceiptsJedisUtil.dataManagementMapper.getAdditionsByName(str);
                jedis.sadd(SELL_RECEIPTS_ADDITION_TYPE_ID + addition.getId(), carReceipts.getId() + "");
            }
            // 渠道id - id > 需要添加渠道信息
            jedis.sadd(SELL_RECEIPTS_CHANNEL_ID + carReceipts.getChannelId(), carReceipts.getId() + "");
            // 创建时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCreateTime())) {
                jedis.sadd(SELL_RECEIPTS_CREATE_TIME_DATE + sdf.format(new Date(Long.parseLong(carReceipts.getCreateTime()))), carReceipts.getId() + "");
            }
            // 收款时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCollectionTime())){
                jedis.sadd(SELL_RECEIPTS_GATHERING_TIME_DATE+sdf.format(new Date(Long.parseLong(carReceipts.getCollectionTime()))),carReceipts.getId()+"");
            }
            // 安装时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getThirdPartyCheckTime())){
                jedis.sadd(SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE+sdf.format(new Date(Long.parseLong(carReceipts.getThirdPartyCheckTime()))),carReceipts.getId()+"");
            }
            // 作废时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCancellationTime())){
                jedis.sadd(SELL_RECEIPTS_SCRAP_TIME_DATE+sdf.format(new Date(Long.parseLong(carReceipts.getCancellationTime()))),carReceipts.getId()+"");
            }
            // 退款时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getRefundTime())){
                jedis.sadd(SELL_RECEIPTS_REFUND_TIME_DATE+sdf.format(new Date(Long.parseLong(carReceipts.getRefundTime()))),carReceipts.getId()+"");
            }
        }
    }

}