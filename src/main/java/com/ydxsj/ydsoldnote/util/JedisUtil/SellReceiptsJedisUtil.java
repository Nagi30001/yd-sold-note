package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.GatheringMsg;
import com.ydxsj.ydsoldnote.bean.ImageUrl;
import com.ydxsj.ydsoldnote.bean.QueryCRMsg;
import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import com.ydxsj.ydsoldnote.mapper.SellReceiptsMapper;
import com.ydxsj.ydsoldnote.util.PublicUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.IMP_LIMIT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class SellReceiptsJedisUtil {


    public static final String SELL_RECEIPTS_ID = "SELL_RECEIPTS_ID:";
    public static final String SELL_RECEIPTS_STATUS = "SELL_RECEIPTS_STATUS:";
    public static final String SELL_RECEIPTS_PROVINCE = "SELL_RECEIPTS_PROVINCE:";
    public static final String SELL_RECEIPTS_CITY = "SELL_RECEIPTS_CITY:";
    public static final String SELL_RECEIPTS_CREATE_USER_ID = "SELL_RECEIPTS_CREATE_USER_ID:";
    public static final String SELL_RECEIPTS_INSTALL_TP_ID = "SELL_RECEIPTS_INSTALL_TP_ID:";
    public static final String SELL_RECEIPTS_GATHERING_MSG_ID = "SELL_RECEIPTS_GATHERING_MSG_ID:";
    public static final String SELL_RECEIPTS_YD_GATHERING = "SELL_RECEIPTS_YD_GATHERING";
    public static final String SELL_RECEIPTS_TP_GATHERING = "SELL_RECEIPTS_TP_GATHERING";
    public static final String SELL_RECEIPTS_GATHERING_USER_ID = "SELL_RECEIPTS_GATHERING_USER_ID:";
    public static final String SELL_RECEIPTS_CLIENT_NAME_LIKE = "SELL_RECEIPTS_CLIENT_NAME_LIKE:";
    public static final String SELL_RECEIPTS_CLIENT_CAR_NUM = "SELL_RECEIPTS_CLIENT_CAR_NUM:";
    public static final String SELL_RECEIPTS_CLIENT_CAR_BRAND = "SELL_RECEIPTS_CLIENT_CAR_BRAND:";
    public static final String SELL_RECEIPTS_EQUIPMENT_ID = "SELL_RECEIPTS_EQUIPMENT_ID:";
    public static final String SELL_RECEIPTS_EQUIPMENT_BRAND = "SELL_RECEIPTS_EQUIPMENT_BRAND:";
    public static final String SELL_RECEIPTS_EQUIPMENT_SIZE = "SELL_RECEIPTS_EQUIPMENT_SIZE:";
    public static final String SELL_RECEIPTS_SELL_TYPE_ID = "SELL_RECEIPTS_SELL_TYPE_ID:";
    public static final String SELL_RECEIPTS_ADDITION_TYPE_ID = "SELL_RECEIPTS_ADDITION_TYPE_ID:";
    public static final String SELL_RECEIPTS_CHANNEL_ID = "SELL_RECEIPTS_CHANNEL_ID:";
    public static final String SELL_RECEIPTS_CREATE_TIME_DATE = "SELL_RECEIPTS_CREATE_TIME_DATE:";
    public static final String SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH = "SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH:";
    public static final String SELL_RECEIPTS_GATHERING_TIME_DATE = "SELL_RECEIPTS_GATHERING_TIME_DATE:";
    public static final String SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE = "SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE:";
    public static final String SELL_RECEIPTS_SCRAP_TIME_DATE = "SELL_RECEIPTS_SCRAP_TIME_DATE:";
    public static final String SELL_RECEIPTS_REFUND_TIME_DATE = "SELL_RECEIPTS_REFUND_TIME_DATE:";
    public static final String SELL_RECEIPTS_IMAGE_URL_ID = "SELL_RECEIPTS_IMAGE_URL_ID:";
    public static final String SELL_RECEIPTS_ID_IMAGE_URL = "SELL_RECEIPTS_ID_IMAGE_URL:";

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
        initialization();
    }

    // 初始化信息
    public static void initialization() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // id - 单据
            List<CarReceipts> list = sellReceiptsJedisUtil.sellReceiptsMapper.getCarReceipts();
            for (CarReceipts carReceipts : list) {
                carReceipts.setGatheringMsg(sellReceiptsJedisUtil.sellReceiptsMapper.getGatheringMsgById(carReceipts.getGatheringMsgId()));
                addSellReceipts(carReceipts);
            }
            // 收款图片地址
            List<ImageUrl> imageUrls = sellReceiptsJedisUtil.sellReceiptsMapper.getAllImageUrls();
            for (ImageUrl imageUrl : imageUrls) {
                addSellReceiptsImageUrl(imageUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }


    public static void addSellReceiptsImageUrl(ImageUrl imageUrl) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            jedis.set(SELL_RECEIPTS_IMAGE_URL_ID + imageUrl.getId(), JSON.toJSONString(imageUrl));
            jedis.sadd(SELL_RECEIPTS_ID_IMAGE_URL + imageUrl.getMsgId(), imageUrl.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsImageUrl");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<ImageUrl> getSellReceiptsImageUrlById(Integer id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            List<ImageUrl> imageUrls = new ArrayList<>();
            Set<String> smembers = jedis.smembers(SELL_RECEIPTS_ID_IMAGE_URL + id);
            for (String s : smembers) {
                imageUrls.add(JSON.parseObject(jedis.get(SELL_RECEIPTS_IMAGE_URL_ID + s), ImageUrl.class));
            }
            return imageUrls;
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsImageUrl");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceipts(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // id - 单据
            addSellReceiptsIdData(carReceipts);
            // 状态 - id
            addSellReceiptsStatus(carReceipts);
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
            if (carReceipts.getChannelId() != null) {
                jedis.sadd(SELL_RECEIPTS_CHANNEL_ID + carReceipts.getChannelId(), carReceipts.getId() + "");
            }
            // 创建时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCreateTime())) {
                jedis.sadd(SELL_RECEIPTS_CREATE_TIME_DATE + PublicUtil.timestampToString(carReceipts.getCreateTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
                // 月份
                jedis.sadd(SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH + PublicUtil.timestampToString(carReceipts.getCreateTime(), PublicUtil.SDF_YYYY_MM), carReceipts.getId() + "");
            }
            // 收款时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCollectionTime())) {
                jedis.sadd(SELL_RECEIPTS_GATHERING_TIME_DATE + PublicUtil.timestampToString(carReceipts.getCollectionTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
            // 安装时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getThirdPartyCheckTime())) {
                jedis.sadd(SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE + PublicUtil.timestampToString(carReceipts.getThirdPartyCheckTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
            // 作废时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCancellationTime())) {
                jedis.sadd(SELL_RECEIPTS_SCRAP_TIME_DATE + PublicUtil.timestampToString(carReceipts.getCancellationTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
            // 退款时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getRefundTime())) {
                jedis.sadd(SELL_RECEIPTS_REFUND_TIME_DATE + PublicUtil.timestampToString(carReceipts.getRefundTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsIdData(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            jedis.set(SELL_RECEIPTS_ID + carReceipts.getId() + "", JSON.toJSONString(carReceipts));
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsStatus(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
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
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void deleteSellReceiptsStatus(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // 状态 - id
            jedis.srem(SELL_RECEIPTS_STATUS + -1, carReceipts.getId() + "");
            jedis.srem(SELL_RECEIPTS_STATUS + 0, carReceipts.getId() + "");
            jedis.srem(SELL_RECEIPTS_STATUS + 1, carReceipts.getId() + "");
            jedis.srem(SELL_RECEIPTS_STATUS + 2, carReceipts.getId() + "");
            jedis.srem(SELL_RECEIPTS_STATUS + 3, carReceipts.getId() + "");
            jedis.srem(SELL_RECEIPTS_STATUS + 4, carReceipts.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void updateSellReceiptsStatus(CarReceipts oldCarReceipts, CarReceipts newCarReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            deleteSellReceiptsStatus(oldCarReceipts);
            addSellReceiptsStatus(newCarReceipts);
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsCreateTime(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // 创建时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCreateTime())) {
                jedis.sadd(SELL_RECEIPTS_CREATE_TIME_DATE + PublicUtil.timestampToString(carReceipts.getCreateTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
                // 月份
                jedis.sadd(SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH + PublicUtil.timestampToString(carReceipts.getCreateTime(), PublicUtil.SDF_YYYY_MM), carReceipts.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsCollectionTime(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // 收款时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCollectionTime())) {
                jedis.sadd(SELL_RECEIPTS_GATHERING_TIME_DATE + PublicUtil.timestampToString(carReceipts.getCollectionTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsThirdPartyCheckTime(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // 安装时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getThirdPartyCheckTime())) {
                jedis.sadd(SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE + PublicUtil.timestampToString(carReceipts.getThirdPartyCheckTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsCancellationTime(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // 作废时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getCancellationTime())) {
                jedis.sadd(SELL_RECEIPTS_SCRAP_TIME_DATE + PublicUtil.timestampToString(carReceipts.getCancellationTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addSellReceiptsRefundTime(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            // 退款时间-日期 - id
            if (!StringUtils.isEmpty(carReceipts.getRefundTime())) {
                jedis.sadd(SELL_RECEIPTS_REFUND_TIME_DATE + PublicUtil.timestampToString(carReceipts.getRefundTime(), PublicUtil.SDF_YYYY_MM_DD), carReceipts.getId() + "");
            }
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     *  获取 date 月的单据信息
     * @param date
     * @param provinces
     * @return
     */
    public static Set<String> getSellReceiptsByProvince(Date date, List<Province> provinces) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            List<String> sinter = new ArrayList<>();
            for (Province province : provinces) {
                Set<String> sinter1 = jedis.sinter(SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH + PublicUtil.timestampToString(String.valueOf(date.getTime()), PublicUtil.SDF_YYYY_MM), SELL_RECEIPTS_PROVINCE + province.getProvince());
                if (sinter1.size() != 0){
                    sinter = (List<String>) CollectionUtils.union(sinter,sinter1);
                }
            }
            return new HashSet<>(sinter);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static CarReceipts getSellReceiptsById(String id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            return JSON.parseObject(jedis.get(SELL_RECEIPTS_ID + id), CarReceipts.class);
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }


    public static GatheringMsg getGatheringMsgById(Integer gatheringMsgId) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            return JSON.parseObject(jedis.get(SELL_RECEIPTS_GATHERING_MSG_ID + gatheringMsgId), GatheringMsg.class);
        } catch (Exception e) {
            throw new RuntimeException("#addSellReceiptsIdData");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<CarReceipts> getReceiptsByStatusAndGatheringUser(int i, User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            Set<String> sinter = jedis.sinter(SELL_RECEIPTS_STATUS + i, SELL_RECEIPTS_GATHERING_USER_ID + user.getId());
            List<CarReceipts> carReceiptsList = new ArrayList<>();
            for (String s : sinter) {
                carReceiptsList.add(getSellReceiptsById(s));
            }
            return carReceiptsList;
        } catch (Exception e) {
            throw new RuntimeException("#getReceiptsByStatusAndGatheringUser");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<CarReceipts> getCarReceiptsByStatusOfTH(int i, User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            Set<String> sinter = jedis.sinter(SELL_RECEIPTS_STATUS + i, SELL_RECEIPTS_INSTALL_TP_ID + user.getId());
            List<CarReceipts> carReceiptsList = new ArrayList<>();
            for (String s : sinter) {
                carReceiptsList.add(getSellReceiptsById(s));
            }
            return carReceiptsList;
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 单据更新更新缓存
     *
     * @param carReceipts
     * @param checkType
     */
    public static void updateSellReceipts(CarReceipts carReceipts, String checkType) throws RuntimeException {
        if (checkType.equals("reachCheck")) {
            // 到店更新
            addSellReceipts(carReceipts);
            deleteSellReceiptsStatus(carReceipts);
            addSellReceiptsStatus(carReceipts);
        } else if (checkType.equals("gatheringCheck")) {
            // 收款更新
            deleteSellReceiptsStatus(carReceipts);
            addSellReceiptsById(carReceipts);
            addSellReceiptsStatus(carReceipts);
            addSellReceiptsCollectionTime(carReceipts);
        } else if (checkType.equals("installCheck")) {
            // 安装更新
            addSellReceiptsById(carReceipts);
            deleteSellReceiptsStatus(carReceipts);
            addSellReceiptsStatus(carReceipts);
            addSellReceiptsThirdPartyCheckTime(carReceipts);
        } else if (checkType.equals("scrap")) {
            // 报废更新
            addSellReceiptsById(carReceipts);
            deleteSellReceiptsStatus(carReceipts);
            addSellReceiptsStatus(carReceipts);
            addSellReceiptsCancellationTime(carReceipts);
        } else if (checkType.equals("refund")) {
            // 退款更新
            addSellReceiptsById(carReceipts);
            deleteSellReceiptsStatus(carReceipts);
            addSellReceiptsStatus(carReceipts);
            addSellReceiptsRefundTime(carReceipts);
        }
    }



    /**
     * 本月在该用户平台上安装的信息
     *
     * @param date
     * @param user
     * @return
     */
    public static List<CarReceipts> getSellReceiptsByInstallUser(Date date, User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            Set<String> sinter = jedis.sinter(SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH + PublicUtil.timestampToString(String.valueOf(date.getTime()), PublicUtil.SDF_YYYY_MM), SELL_RECEIPTS_INSTALL_TP_ID + user.getId());
            List<CarReceipts> carReceiptsList = new ArrayList<>();
            for (String s : sinter) {
                carReceiptsList.add(getSellReceiptsById(s));
            }
            return carReceiptsList;
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 本月我创建的单据
     *
     * @param date
     * @param user
     * @return
     */
    public static List<CarReceipts> getSellReceiptsByCreateUser(Date date, User user) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            Set<String> sinter = jedis.sinter(SELL_RECEIPTS_CREATE_TIME_DATE_YEAR_MONTH + PublicUtil.timestampToString(String.valueOf(date.getTime()), PublicUtil.SDF_YYYY_MM), SELL_RECEIPTS_CREATE_USER_ID + user.getId());
            List<CarReceipts> carReceiptsList = new ArrayList<>();
            for (String s : sinter) {
                carReceiptsList.add(getSellReceiptsById(s));
            }
            return carReceiptsList;
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 根据查询条件获取指定单据
     *
     * @param s
     * @return
     */
    public static List<CarReceipts> searchCarReceipts(QueryCRMsg s) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            String checkTimeType = null;
            String relatingType = null;
            if (s.getCheckTimeType().equals("创建时间")) {
                checkTimeType = SELL_RECEIPTS_CREATE_TIME_DATE;
            } else if (s.getCheckTimeType().equals("收款时间"))
                if (s.getTPId() != null) {
                    checkTimeType = SELL_RECEIPTS_GATHERING_TIME_DATE;
                } else if (s.getCheckTimeType().equals("安装时间")) {
                    checkTimeType = SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE;
                } else {
                    checkTimeType = SELL_RECEIPTS_CREATE_TIME_DATE;
                }
            if (s.getType().equals("DT") || s.getType().equals("YD")) {
                relatingType = SELL_RECEIPTS_CREATE_USER_ID;
            } else if (s.getType().equals("PT")) {
                relatingType = SELL_RECEIPTS_INSTALL_TP_ID;
            } else {
                throw new RuntimeException("");
            }
            Set<String> set = new HashSet<>();
            for (User user : s.getUsers()) {

            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 根据日期/时间查询类型
     *
     * @param date
     * @param checkTimeType
     * @return
     */
    public static Set<String> getCarReceiptsByTimeScope(String date, String checkTimeType) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            return jedis.smembers(checkTimeType + date);
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static Set<String> getSellReceiptsByTAndUser(String id, User u, String relatingType, String T) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            return jedis.sinter(relatingType + u.getId(), T + id);
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static Set<String> getSellReceiptsByClientNameLike(String clientName, User u, String relatingType, String sellReceiptsClientNameLike) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            List<String> strings = new ArrayList<>();
            String[] chars = clientName.split("|");
            if (chars.length == 1) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(sellReceiptsClientNameLike + chars[0], relatingType + u.getId()));
            } else if (chars.length == 2) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(sellReceiptsClientNameLike + chars[0], sellReceiptsClientNameLike + chars[1], relatingType + u.getId()));
            } else if (chars.length == 3) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(sellReceiptsClientNameLike + chars[0], sellReceiptsClientNameLike + chars[1], sellReceiptsClientNameLike + chars[2], relatingType + u.getId()));
            } else if (chars.length == 4) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(sellReceiptsClientNameLike + chars[0], sellReceiptsClientNameLike + chars[1], sellReceiptsClientNameLike + chars[2], sellReceiptsClientNameLike + chars[3], relatingType + u.getId()));
            } else if (chars.length == 5) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(sellReceiptsClientNameLike + chars[0], sellReceiptsClientNameLike + chars[1], sellReceiptsClientNameLike + chars[2], sellReceiptsClientNameLike + chars[3], sellReceiptsClientNameLike + chars[4], relatingType + u.getId()));
            } else if (chars.length == 6) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(sellReceiptsClientNameLike + chars[0], sellReceiptsClientNameLike + chars[1], sellReceiptsClientNameLike + chars[2], sellReceiptsClientNameLike + chars[3], sellReceiptsClientNameLike + chars[4], sellReceiptsClientNameLike + chars[5], relatingType + u.getId()));
            }
            Set<String> set = new HashSet<>(strings);
            return set;
        } catch (Exception e) {
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<String> getSellReceiptsByAdditionAndUser(List<Integer> additionType, User u, String relatingType) {



        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            List<String> strings = new ArrayList<>();
            if (additionType.size() == 1) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(0), relatingType + u.getId()));
            } else if (additionType.size() == 2) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(0), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(1), relatingType + u.getId()));
            } else if (additionType.size() == 3) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(0), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(1), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(2), relatingType + u.getId()));
            } else if (additionType.size() == 4) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(0), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(1), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(2), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(3), relatingType + u.getId()));
            } else if (additionType.size() == 5) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(0), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(1), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(2), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(3), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(4), relatingType + u.getId()));
            } else if (additionType.size() == 6) {
                strings = (List<String>) CollectionUtils.union(strings, jedis.sinter(SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(0), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(1), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(2), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(3), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(4), SELL_RECEIPTS_ADDITION_TYPE_ID + additionType.get(5), relatingType + u.getId()));
            }
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static List<String> getSellReceiptsByCreateOrInstallUser(User u, String relatingType) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            Set<String> smembers = jedis.smembers(relatingType + u.getId());
            if (CollectionUtils.isEmpty(smembers)){
                return new ArrayList<>();
            }
            List<String> strings = new ArrayList<>();
            for (String s : smembers){
                strings.add(s);
            }
            return strings;
        } catch (Exception e) {
//            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
            e.printStackTrace();
            return null;
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    private static void addSellReceiptsById(CarReceipts carReceipts) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(3);
            jedis.set(SELL_RECEIPTS_ID+carReceipts.getId(),JSON.toJSONString(carReceipts));
            if (carReceipts.getGatheringMsg() != null){
                jedis.set(SELL_RECEIPTS_GATHERING_MSG_ID+carReceipts.getGatheringMsg().getId(),JSON.toJSONString(carReceipts.getGatheringMsg()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("#getCarReceiptsByStatusOfTH");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }


}
