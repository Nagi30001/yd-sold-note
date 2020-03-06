package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.GatheringMsg;
import com.ydxsj.ydsoldnote.bean.ImageUrl;
import com.ydxsj.ydsoldnote.bean.QueryCRMsg;
import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.data.equipment.EquipmentMsg;
import com.ydxsj.ydsoldnote.bean.data.equipment.InventoryMsg;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.*;
import com.ydxsj.ydsoldnote.service.SellReceiptsService;
import com.ydxsj.ydsoldnote.service.UserUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.*;
import com.ydxsj.ydsoldnote.util.PublicUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.management.relation.RoleUnresolved;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SellReceiptsServiceImpl implements SellReceiptsService {


    @Autowired
    private SellReceiptsMapper sellReceiptsMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DataManagementMapper dataManagementMapper;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private UserUtil userUtil;

    // 图片存储路径
    @Value("${uploadDir}")
    private String uploadDir;

    @Override
    public List<CarReceipts> getCarReceipts(User user, Map map) throws RuntimeException {
        Integer page = Integer.valueOf(String.valueOf(map.get("page")));
        Integer count = Integer.valueOf(String.valueOf(map.get("count")));
        // 获取用户省份
        List<Province> provinces = CityJedisUtil.getBeProvincesByUser(user);
        // 获取
        // 使用省份名称获取对应的单据信息（时间倒序）
        // 本月与省份并集的数据
        Set<String> sinter = SellReceiptsJedisUtil.getSellReceiptsByProvince(new Date(), provinces);
        if (CollectionUtils.isEmpty(sinter)) {
            // 没有数据
            return new ArrayList<>();
        }
        List<CarReceipts> carReceiptsList = getCarReceiptsByPaging(page,count,sinter);
        // 补全信息
        carReceiptsList = getCarreceipts(carReceiptsList);
        return carReceiptsList;
    }

    @Override
    @Transactional
    public CarReceipts addSellReceipts(Map map) throws RuntimeException {
        Map data = (Map) map.get("temp");
        String token = (String) map.get("token");
        double money = 0.0;
        double cashPledge = 0.0;

        CarReceipts carReceipts = new CarReceipts();
        // 根据token获取用户信息
        Integer userId = UserJedisUtil.getUserIdByToken(token);
        // 获取平台信息
        User tpUser = UserJedisUtil.getUserById((Integer) data.get("thirdParty"));
        // 添加单据信息
        carReceipts.setCreateTime(String.valueOf(System.currentTimeMillis()));
        carReceipts.setUserId(userId);
        List<String> city = (List<String>) data.get("city");
        carReceipts.setProvince(city.get(0));
        carReceipts.setCity(city.get(1));
        carReceipts.setReceiptsStatus(1);
        carReceipts.setClientName((String.valueOf(data.get("clientName"))).trim());
        carReceipts.setClientPhone(data.get("clientPhone").toString());
        List<String> carBrand = (List<String>) data.get("carType");
        carReceipts.setCarBrand(carBrand.get(0) + "/" + carBrand.get(1));
        carReceipts.setClientCarNum((String) data.get("carNum"));
        carReceipts.setThirdPartyTerraceId((Integer) data.get("thirdParty"));
        carReceipts.setCount(1);
        carReceipts.setPredictInstallTime(PublicUtil.stringTotimestamp(String.valueOf(data.get("installTime")), PublicUtil.SDF_YYYY_MM_DD_HH_MM));
        // 查询销售类型
        SellType sellType = DMJedisUtil.getSellTypeById(String.valueOf(data.get("sellType")));
        List<Integer> additions = (List<Integer>) data.get("additionType");
        money += sellType.getMoney();
        cashPledge += sellType.getCashPledge();

        List<Addition> additions1 = new ArrayList<>();
        for(Integer add : additions) {
            additions1.add(DMJedisUtil.getAdditionById(String.valueOf(add)));
        }
        List<String> additions2 = new ArrayList<>();
        for (Addition addition : additions1) {
            additions2.add(addition.getAdditionName());
        }
        String additionType = StringUtils.join(additions2, "-");
        carReceipts.setAdditions(additions1);
        carReceipts.setAdditionType(additionType);
        carReceipts.setSellTypeName(sellType.getSellType());
        carReceipts.setMoney(money);
        carReceipts.setCashPledge(cashPledge);
        GatheringMsg gatheringMsg = new GatheringMsg();
        // 判断收款方
        if (data.get("gatheringType").toString().equals("YD")) {
            carReceipts.setGatheringType("优道科技");
            gatheringMsg.setYdGathering(1);
            gatheringMsg.setThirdPartyGathering(0);
            gatheringMsg.setGatheringStatus(0);
            gatheringMsg.setGatheringUserId(userId);
        } else {
            carReceipts.setGatheringType(tpUser.getUserName());
            gatheringMsg.setYdGathering(0);
            gatheringMsg.setThirdPartyGathering(1);
            gatheringMsg.setGatheringStatus(0);
            gatheringMsg.setGatheringUserId((Integer) data.get("thirdParty"));
        }
        // 插入收款信息
        Integer integer = sellReceiptsMapper.insertGatheringMsg(gatheringMsg);
        if (!integer.equals(1)) {
            throw new RuntimeException("");
        }
        carReceipts.setGatheringMsg(gatheringMsg);
        carReceipts.setGatheringMsgId(gatheringMsg.getId());

        // 插入报表数据
        Integer carReceiptsId = sellReceiptsMapper.insertCarReceiots(carReceipts);
        if (!carReceiptsId.equals(1)) {
            throw new RuntimeException("");
        }
        carReceipts.setUser(UserJedisUtil.getUserById(userId));
        carReceipts.setTpUser(tpUser);
        carReceipts.setTimeMsgs(getTimeMsg(carReceipts));
        // cache
        SellReceiptsJedisUtil.addSellReceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByGathering(User user) throws RuntimeException {
        //获取全部收款中单据数据
        List<CarReceipts> carReceipts = SellReceiptsJedisUtil.getReceiptsByStatusAndGatheringUser(2, user);
        // 到店确认的
        carReceipts = (List<CarReceipts>) CollectionUtils.union(carReceipts,SellReceiptsJedisUtil.getReceiptsByStatusAndGatheringUser(1, user));
        // 补充收款收款信息
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByReach(User user) throws RuntimeException {
        //获取该平台待到店确认的单据
        List<CarReceipts> carReceipts = SellReceiptsJedisUtil.getCarReceiptsByStatusOfTH(1, user);
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByInstall(User user) throws RuntimeException {
        // 获取该平台安装待确认的单据
        List<CarReceipts> carReceipts = SellReceiptsJedisUtil.getCarReceiptsByStatusOfTH(3, user);
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Transactional
    @Override
    public Integer checkReceipts(String token, String type, Integer id, String checkTime, Map map) throws RuntimeException {
        Map data = (Map) map.get("data");
        // 获取操作人用户信息
        User user = UserJedisUtil.getUserById(UserJedisUtil.getUserIdByToken(token));
        //获取该id单据
        CarReceipts carReceipts = SellReceiptsJedisUtil.getSellReceiptsById(String.valueOf(id));
        if (user == null || carReceipts == null || checkTime.isEmpty()) {
            throw new RuntimeException("");
        }
        List<CarReceipts> carReceiptss = new ArrayList<>();
        carReceiptss.add(carReceipts);
        // 补全信息
        carReceipts = getCarreceipts(carReceiptss).get(0);
        // 到店确认
        if (type.equals("reachCheck") && carReceipts.getReceiptsStatus().equals(1) && carReceipts.getReceiptsReachCheck() == null) {
            // 打桩
            carReceipts.setReceiptsReachCheck(checkTime);
            carReceipts.setReceiptsStatus(2);
            Integer row = sellReceiptsMapper.updateCheckTime(carReceipts, "reachCheck");
            if (!row.equals(1)) {
                throw new RuntimeException();
            } else {
                // update carReceipts cache
                carReceipts = sellReceiptsMapper.getCarReceiptsById(carReceipts.getId());
                carReceipts.setGatheringMsg(SellReceiptsJedisUtil.getGatheringMsgById(carReceipts.getGatheringMsgId()));
                SellReceiptsJedisUtil.updateSellReceipts(carReceipts, "reachCheck");
                return row;
            }
            // 收款确认
        } else if (type.equals("gatheringCheck") && StringUtils.isEmpty(carReceipts.getGatheringMsg().getGatheringCheckTime())) {
            carReceipts.getGatheringMsg().setGatheringCheckTime(checkTime);
            carReceipts.getGatheringMsg().setText((String) data.get("text"));
            carReceipts.setReceiptsStatus(3);
            // 更新收款信息
            Integer row = sellReceiptsMapper.updateGatherimgMsg(carReceipts.getGatheringMsg());
            if (!row.equals(1)) {
                throw new RuntimeException("");

            } else {
                carReceipts.setCollectionTime(checkTime);
                // 更新收款信息
                Integer integer = sellReceiptsMapper.updateCheckTime(carReceipts, type);
                if (!integer.equals(1)) {
                    throw new RuntimeException("");
                } else {
                    //update cache
                    carReceipts = sellReceiptsMapper.getCarReceiptsById(carReceipts.getId());
                    carReceipts.setGatheringMsg(sellReceiptsMapper.getGatheringMsgById(carReceipts.getGatheringMsgId()));
                    carReceipts.setGatheringMsg(sellReceiptsMapper.getGatheringMsgById(carReceipts.getGatheringMsgId()));
                    SellReceiptsJedisUtil.updateSellReceipts(carReceipts, "gatheringCheck");
                }
                return integer;
            }
            // 安装确认
        } else if (type.equals("installCheck") && carReceipts.getReceiptsStatus() == 3 && StringUtils.isEmpty(carReceipts.getGatheringMsg().getGatheringCheckTime())) {
            carReceipts.setThirdPartyCheckTime(checkTime); //废弃
            return null;
        } else {
            throw new RuntimeException("");
        }
    }

    @Override
    @Transactional
    public boolean uploadFile(Map map, MultipartFile file) throws RuntimeException {
        Random random = new Random();
        //获取上传文件操作类型
        String type = (String) map.get("fileType");
        String userId = String.valueOf(map.get("fileUserId"));
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        String faset = fileName.substring(0, fileName.lastIndexOf(".")) + random.nextInt(10000);
        fileName = faset + suffixName;
        // 文件上传后的路径
        String filePath = uploadDir + userId + "/";
        // 解决中文问题，liunx下中文路径，图片显示问题
        // fileName = UUID.randomUUID() + suffixName;
        File dest = new File(filePath + fileName);
        ImageUrl imageUrl = new ImageUrl();
        //判断是否收款上传图片
        if (type.equals("gatheringCheck")) {
            CarReceipts carReceipts = SellReceiptsJedisUtil.getSellReceiptsById((String) map.get("fileId"));
            if (carReceipts != null) {
                imageUrl.setMsgId(Integer.parseInt((String) map.get("fileId")));
                imageUrl.setUrl("http://49.234.210.89/images/" + userId + "/" + fileName);

            } else {
                return false;
            }
            Integer row = sellReceiptsMapper.insertImageUrl(imageUrl);
            if (!row.equals(1)){
                throw new RuntimeException("");
            }
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
                System.err.println("上传成功后的文件路径未：" + filePath + fileName);
                return true;
            } catch (Exception e) {
                throw new RuntimeException("");
            }
        }
        throw new RuntimeException("");
    }

    @Override
    public List<CarReceipts> getMyReceipts(String token) throws RuntimeException{
        User user = UserJedisUtil.getUserByToken(token);
        List<CarReceipts> carReceipts;
        // 平台用户
        if ("R1004".equals(user.getRoleNum())) {
            carReceipts = SellReceiptsJedisUtil.getSellReceiptsByInstallUser(new Date(),user);
        } else {
            carReceipts = SellReceiptsJedisUtil.getSellReceiptsByCreateUser(new Date(),user);
        }
        // 补全信息
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    @Transactional
    public boolean cancellationCarReceiptsById(String token, Integer id) throws RuntimeException {
        User user = UserJedisUtil.getUserByToken(token);
        // id 为null 不能作废 或者操作用户为null 不能作废
        if (id == null || user == null) {
            throw new RuntimeException("");
        }
        CarReceipts carReceipts = SellReceiptsJedisUtil.getSellReceiptsById(String.valueOf(id));
        // 单据已报废或已完成,或者单据的创建人用户ID不匹配 不能作废
        if (carReceipts == null || carReceipts.getReceiptsStatus() == 0 || carReceipts.getReceiptsStatus() == 4 || carReceipts.getUserId() != user.getId()) {
            throw new RuntimeException("");
        }
        // 修改该单据状态，更新单据信息
        // 更新状态
        carReceipts.setReceiptsStatus(0);
        // 更新作废时间
        carReceipts.setCancellationTime(String.valueOf(System.currentTimeMillis()));
        // 更新到数据库
        Integer row = sellReceiptsMapper.updateCarReceiptsStatus(carReceipts);
        // 不为0更新成功
        if (!row.equals(1)) {
            throw new RuntimeException("");
        }
        // update cache
        SellReceiptsJedisUtil.updateSellReceipts(carReceipts,"scrap");
        return true;
    }

    @Override
    public List<CarReceipts> searchQueryDate(Map map) throws RuntimeException {
        QueryCRMsg queryCRMsg = new QueryCRMsg();
        // 判断是大厅还是我的(DT/YD/PT)
        // 获取数据
        String type = String.valueOf(map.get("type"));
        String userId = String.valueOf(map.get("userId"));
        String sellName = String.valueOf(map.get("sellName"));
        String TPId = String.valueOf(map.get("TPId"));
        String clientName = String.valueOf(map.get("clientName"));
        String clientCarNum = String.valueOf(map.get("clientCarNum"));
        String startingDate = String.valueOf(map.get("startingDate"));
        String endDate = String.valueOf(map.get("endDay"));
        String status = String.valueOf(map.get("status"));
        Integer page = Integer.valueOf(String.valueOf(map.get("page")));
        Integer count = Integer.valueOf(String.valueOf(map.get("count")));
        // 查询时间类型
        String checkTimeType = String.valueOf(map.get("checkTimeType"));
        // 销售类型
        String sellType = String.valueOf(map.get("sellType"));
        // 附加业务
        List<Integer> additionType = (List<Integer>) map.get("additionType");
        // 渠道
        String channel = String.valueOf(map.get("channel"));

        // 查询信息封装
        queryCRMsg.setUserId(Integer.valueOf(userId));
        queryCRMsg.setType(type);
        queryCRMsg.setStartingDate(startingDate);
        queryCRMsg.setEndDate(endDate);
        queryCRMsg.setPage(Integer.valueOf(page));
        queryCRMsg.setCount(Integer.valueOf(count));
        queryCRMsg.setCheckTimeType(checkTimeType);
        if (!CollectionUtils.isEmpty(additionType)){
            queryCRMsg.setAdditionType(additionType);
        }
        if (!StringUtils.isEmpty(clientName)){
            queryCRMsg.setClientName(clientName);
        }
        if (!StringUtils.isEmpty(clientCarNum)){
            queryCRMsg.setClientCarNum(clientCarNum);
        }
        if (!StringUtils.isEmpty(sellName)){
            queryCRMsg.setSellName(sellName);
        }
        if (!StringUtils.isEmpty(sellType)) {
            queryCRMsg.setSellType(sellType);
        }
        if (!StringUtils.isEmpty(TPId)) {
            queryCRMsg.setTPId(Integer.valueOf(TPId));
        }
        if (!StringUtils.isEmpty(channel)) {
            queryCRMsg.setChannel(channel);
        }
        if (!StringUtils.isEmpty(status)) {
            queryCRMsg.setStatus(Integer.valueOf(status));
        }
        if (!StringUtils.isEmpty(type)) {
            // 获取角色省份
            User user ;
            List<Province> provinces;
            List<User> userList = new ArrayList<>();
            // 查询时间类型
            String checkTimeTypeq = null;
            // 创建人：id or 安装人：id
            String relatingType = null;
            if (queryCRMsg.getCheckTimeType().equals("创建时间")) {
                checkTimeTypeq = SellReceiptsJedisUtil.SELL_RECEIPTS_CREATE_TIME_DATE;
            } else if (queryCRMsg.getCheckTimeType().equals("收款时间")){
                checkTimeTypeq = SellReceiptsJedisUtil.SELL_RECEIPTS_GATHERING_TIME_DATE;
            } else if (queryCRMsg.getCheckTimeType().equals("安装时间")) {
                checkTimeTypeq = SellReceiptsJedisUtil.SELL_RECEIPTS_EQUIPMENT_INSTALL_TIME_DATE;
            }
            if (queryCRMsg.getType().equals("DT")) {
                user = UserJedisUtil.getUserById(Integer.valueOf(userId));
                provinces = CityJedisUtil.getBeProvincesByUser(user);
                if (!StringUtils.isEmpty(sellName)){
                    queryCRMsg.setSellName(sellName);
                    userList = UserJedisUtil.getUserByNameLike(sellName,provinces);
                } else {
                    userList = UserJedisUtil.getUserByProvinces(provinces);
                }
                queryCRMsg.setUsers(userList);
                relatingType = SellReceiptsJedisUtil.SELL_RECEIPTS_CREATE_USER_ID;
                queryCRMsg.setUsers(userList);
            } else if (queryCRMsg.getType().equals("YD")) {
                user = UserJedisUtil.getUserById(Integer.valueOf(userId));
                userList.add(user);
                queryCRMsg.setUsers(userList);
                relatingType = SellReceiptsJedisUtil.SELL_RECEIPTS_CREATE_USER_ID;
            } else if (queryCRMsg.getType().equals("PT")) {
                user = UserJedisUtil.getUserById(Integer.valueOf(userId));
                userList.add(user);
                queryCRMsg.setUsers(userList);
                relatingType = SellReceiptsJedisUtil.SELL_RECEIPTS_INSTALL_TP_ID;
            } else {
                throw new RuntimeException("");
            }
            List<String> timeStrings = new ArrayList<>();
            // 根据时间范围获取单据并集
            List<String> days = PublicUtil.getDays(startingDate, endDate);
            for (String d : days) {
                timeStrings = (List<String>) CollectionUtils.union(timeStrings, SellReceiptsJedisUtil.getCarReceiptsByTimeScope(d, checkTimeTypeq));
            }
            List<String> strings = new ArrayList<>();

            for (User u : userList) {
                // 根据用户信息获取每个用户+条件单据交集
                List<String> stringSet = new ArrayList<>();
                // 创建或安装人
                stringSet =  SellReceiptsJedisUtil.getSellReceiptsByCreateOrInstallUser(u,relatingType);
                // 销售类型
                if (!StringUtils.isEmpty(queryCRMsg.getSellType())) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByTAndUser(queryCRMsg.getSellType(), u, relatingType, SellReceiptsJedisUtil.SELL_RECEIPTS_SELL_TYPE_ID));
                }
                // 平台id
                if (queryCRMsg.getTPId() != null) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByTAndUser(String.valueOf(queryCRMsg.getTPId()), u, relatingType, SellReceiptsJedisUtil.SELL_RECEIPTS_INSTALL_TP_ID));
                }
                // 客户姓名
                if (!StringUtils.isEmpty(queryCRMsg.getClientName())) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByClientNameLike(queryCRMsg.getClientName(), u, relatingType, SellReceiptsJedisUtil.SELL_RECEIPTS_CLIENT_NAME_LIKE));
                }
                // 客户车牌
                if (!StringUtils.isEmpty(queryCRMsg.getClientCarNum())) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByTAndUser(queryCRMsg.getClientCarNum(), u, relatingType, SellReceiptsJedisUtil.SELL_RECEIPTS_CLIENT_CAR_NUM));
                }
                // 渠道信息
                if (!StringUtils.isEmpty(queryCRMsg.getChannel())) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByTAndUser(queryCRMsg.getChannel(), u, relatingType, SellReceiptsJedisUtil.SELL_RECEIPTS_CHANNEL_ID));
                }
                // 状态
                if (queryCRMsg.getStatus() != null) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByTAndUser(String.valueOf(queryCRMsg.getStatus()), u, relatingType, SellReceiptsJedisUtil.SELL_RECEIPTS_STATUS));
                }
                // 附加业务
                if (!CollectionUtils.isEmpty(queryCRMsg.getAdditionType())) {
                    stringSet = (List<String>) CollectionUtils.intersection(stringSet, SellReceiptsJedisUtil.getSellReceiptsByAdditionAndUser(queryCRMsg.getAdditionType(), u, relatingType));
                }
                // 将每个用户+条件的交集,并集到新的集合中
                strings = (List<String>) CollectionUtils.union(strings, stringSet);
            }
            // 将用户+条件 or 时间范围 交集
            strings = (List<String>) CollectionUtils.intersection(strings, timeStrings);

            // 排序 分页
            List<CarReceipts> carReceipts = getCarReceiptsByPaging(page,count, new HashSet<>(strings));
            // 补充信息
            carReceipts = getCarreceipts(carReceipts);

            return carReceipts;




        }
        return new ArrayList<>();
    }

    /**
     * 获取排序后
     * @param page
     * @param count
     * @param strings
     * @return
     */
    public List<CarReceipts> getCarReceiptsByPaging(Integer page,Integer count,Set<String> strings){
        //排序
        strings = new TreeSet<>(strings);
        List<String> list = new ArrayList<>(strings);
        List<CarReceipts> carReceipts = new ArrayList<>();
        int datePage = page * count;
        int dateCount = (page + 1) * count;
        int size = list.size();
        if (dateCount > size) {
            dateCount = size;
        }
        for (int i = datePage; i < dateCount; i++) {
            carReceipts.add(SellReceiptsJedisUtil.getSellReceiptsById(list.get(i)));
        }
        return carReceipts;
    }

    @Transactional
    @Override
    public void pushInstallMsg(Map map) {
        String userId = String.valueOf(map.get("userId"));
        String iccid = String.valueOf(map.get("iccid"));
        String equipment = String.valueOf(map.get("equipment"));
        String carNum = String.valueOf(map.get("carNum"));
        String carReceiptsId = String.valueOf(map.get("carReceiptsId"));
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(iccid) || StringUtils.isEmpty(equipment) || StringUtils.isEmpty(carNum) || StringUtils.isEmpty(carReceiptsId)) {
            throw new RuntimeException("数据请求错误!#1");
        }
        // 获取单据
        CarReceipts carReceipts = SellReceiptsJedisUtil.getSellReceiptsById(carReceiptsId);
        boolean row = IccidJedisUtil.checkIccid(iccid);
        EquipmentMsg equipmentMsg = DMJedisUtil.getEquipmentMsgById(Integer.valueOf(equipment));
        // 判断
        if (carReceipts == null || carReceipts.getReceiptsStatus().equals(4) || !userId.equals(String.valueOf(carReceipts.getThirdPartyTerraceId())) ||
                !carNum.equals(carReceipts.getClientCarNum()) || !row || equipmentMsg == null) {
            throw new RuntimeException("数据请求错误!#2");
        } else {
            // 更新数据
            CarReceipts carReceipts1 = new CarReceipts();
            carReceipts1.setId(Integer.valueOf(carReceiptsId));
            carReceipts1.setReceiptsStatus(4);
            // 获取iccid信息
            Iccid iccid1 = IccidJedisUtil.getIccidByTop19(iccid);
            // 更新iccid 状态
            Iccid iccid2 = new Iccid();
            iccid2.setIccid(iccid1.getIccid());
            iccid2.setStatus(0);
            Integer row1 = dataManagementMapper.updateIccid(iccid2);
            carReceipts1.setIccid(iccid1.getIccid());
            // 获取库存信息
            //
            InventoryMsg inventoryMsg = dataManagementMapper.getInventoryMsgByEquipmentMsgIdAndUserId(equipmentMsg.getId(), Integer.valueOf(userId));
            // 判断库存是否大于0
            if (inventoryMsg.getAwaitInstall()<1){
                throw  new RuntimeException("库存小于1，请检查库存！");
            }
            //更新库存信息
            InventoryMsg inventoryMsg1 = new InventoryMsg();
            inventoryMsg1.setId(inventoryMsg.getId());
            inventoryMsg1.setAwaitInstall(-1);
            Integer integer = dataManagementMapper.updateInventoryMsg(inventoryMsg1);
            if (!integer.equals(1)){
                throw new RuntimeException("更新库存失败！");
            }

            // 添加设备信息
            carReceipts1.setEquipmentBrand(equipmentMsg.getEquipmentBrand());
            carReceipts1.setEquipmentTypeNum(equipmentMsg.getEquipmentTypeNum());
            carReceipts1.setSize(equipmentMsg.getSize());
            carReceipts1.setThirdPartyCheckTime(String.valueOf(System.currentTimeMillis()));
            Integer line = sellReceiptsMapper.updateCarReceipts(carReceipts1);
            if (row1 != 1 || line != 1) {
                throw new RuntimeException("数据请求错误!#3");
            }
            // 更新库存信息
            inventoryMsg1 = dataManagementMapper.getInventoryMsgById(inventoryMsg1.getId());
            DMJedisUtil.updateInventoryMsg(inventoryMsg1);
            // 更新缓存 iccid
            iccid1 = dataManagementMapper.getIccid(iccid+"_");

            IccidJedisUtil.updateIccid(iccid1);
            // 更新单据信息缓存
            carReceipts = sellReceiptsMapper.getCarReceiptsById(Integer.valueOf(carReceiptsId));
            SellReceiptsJedisUtil.updateSellReceipts(carReceipts,"installCheck");
        }


    }

    /**
     * 根据单据的状态 添加时间状态等
     *
     * @param carReceipts
     * @return
     */
    public List<TimeMsg> getTimeMsg(CarReceipts carReceipts) {
        List<TimeMsg> timeMsgs = new ArrayList<>();
        // 变更创建时间格式
        TimeMsg timeMsg = new TimeMsg();
        timeMsg.setTypeName("创建时间");
        timeMsg.setTypeTime(PublicUtil.timestampToString(carReceipts.getCreateTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        timeMsg.setColor("#0bbd87");
        timeMsgs.add(timeMsg);

        // 变更预计到店时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("预计到店时间");
        timeMsg.setTypeTime(PublicUtil.timestampToString(carReceipts.getPredictInstallTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
        timeMsg.setColor("#0bbd87");
        timeMsgs.add(timeMsg);

        // 变更到店时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("到店确认");
        if (!StringUtils.isEmpty(carReceipts.getReceiptsReachCheck())) {
            timeMsg.setTypeTime(PublicUtil.timestampToString(carReceipts.getReceiptsReachCheck(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        } else {
            timeMsg.setTypeTime("...");
            timeMsg.setType("primary");
            timeMsg.setIcon("el-icon-more");
            timeMsgs.add(timeMsg);
        }

        // 变更收款确认时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("收款确认");
        if (!StringUtils.isEmpty(carReceipts.getGatheringMsg().getGatheringCheckTime())) {
            timeMsg.setTypeTime(PublicUtil.timestampToString(carReceipts.getGatheringMsg().getGatheringCheckTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        } else {
            timeMsg.setTypeTime("...");
            timeMsg.setType("primary");
            timeMsg.setIcon("el-icon-more");
            timeMsgs.add(timeMsg);
        }

        // 变更安装完成时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("安装确认");
        if (!StringUtils.isEmpty(carReceipts.getThirdPartyCheckTime())) {
            timeMsg.setTypeTime(PublicUtil.timestampToString(carReceipts.getThirdPartyCheckTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        } else {
            timeMsg.setTypeTime("...");
            timeMsg.setType("primary");
            timeMsg.setIcon("el-icon-more");
            timeMsgs.add(timeMsg);
        }

        // 变更作废时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("作废时间");
        if (carReceipts.getReceiptsStatus() == 0 && carReceipts.getCancellationTime() != "") {
            timeMsg.setTypeTime(PublicUtil.timestampToString(carReceipts.getCancellationTime(), PublicUtil.SDF_YYYY_MM_DD_HH_MM_SS));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        }

        return timeMsgs;
    }

    /**
     * 查询补全信息// 到店待确认，安装待确认
     *
     * @return
     */
    public List<CarReceipts> getCarreceipts(List<CarReceipts> carReceipts) {
        List<CarReceipts> carReceiptss = new ArrayList<>();
        if (carReceipts != null) {
            for (CarReceipts carReceipts1 : carReceipts) {
                carReceipts1.setUser(UserJedisUtil.getUserById(carReceipts1.getUserId()));
                carReceipts1.setTpUser(UserJedisUtil.getUserById(carReceipts1.getThirdPartyTerraceId()));
                // 获取每个单据的收款信息
                GatheringMsg gatheringMsg = SellReceiptsJedisUtil.getGatheringMsgById(carReceipts1.getGatheringMsgId());
                // 补全收款图片地址
                List<ImageUrl> imageUrls = SellReceiptsJedisUtil.getSellReceiptsImageUrlById(carReceipts1.getId());
                if (!CollectionUtils.isEmpty(imageUrls)) {
                    List<String> urls = new ArrayList<>();
                    for (ImageUrl imageUrl : imageUrls) {
                        urls.add(imageUrl.getUrl());
                    }
                    carReceipts1.setImageUrls(urls);
                }
                // 判断 收款人id是该用户 且 销售单据的收款信息id 是收款信息id
                if (carReceipts1.getGatheringMsgId() == gatheringMsg.getId()) {
                    // 将收款信息添加到单据中
                    carReceipts1.setGatheringMsg(gatheringMsg);
                    // 修改时间格式
                    carReceipts1.setTimeMsgs(getTimeMsg(carReceipts1));
                    // 添加单据到新的集合中
                    carReceiptss.add(carReceipts1);
                }
            }
            return carReceiptss;
        }
        return null;
    }

}
