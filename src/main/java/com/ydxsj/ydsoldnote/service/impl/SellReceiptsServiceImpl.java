package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.GatheringMsg;
import com.ydxsj.ydsoldnote.bean.ImageUrl;
import com.ydxsj.ydsoldnote.bean.QueryCRMsg;
import com.ydxsj.ydsoldnote.bean.data.Iccid;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.data.TimeMsg;
import com.ydxsj.ydsoldnote.bean.data.equipment.EquipmentMsg;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.*;
import com.ydxsj.ydsoldnote.service.SellReceiptsService;
import com.ydxsj.ydsoldnote.service.UserUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public List<CarReceipts> getCarReceipts(User user,Map map) {
        String page = String.valueOf(map.get("page"));
        String count = String.valueOf(map.get("count"));
        // 获取用户省份
        List<String> provincesId = Arrays.asList((user.getBeProvince().split("-")));
        System.err.println("provincesId:" + provincesId.toString());
        //使用省份获取省份名称
        List<String> provinces = cityMapper.getProvinceById(provincesId);
        // 获取
        System.err.println("provinces:" + provinces.toString());
        // 使用省份名称获取对应的单据信息（时间倒序）
        List<CarReceipts> carReceiptss = sellReceiptsMapper.getCarReceiptsByProvince(provinces,Integer.valueOf(page),Integer.valueOf(count));

        // 补全信息
        carReceiptss = getCarreceipts(carReceiptss);
//        for (CarReceipts carReceipts : carReceiptss) {
//            // 收款信息
//            carReceipts.setGatheringMsg(sellReceiptsMapper.getGatheringMsgById(carReceipts.getGatheringMsgId()));
//            // 状态模块
//            carReceipts.setTimeMsgs(getTimeMsg(carReceipts));
//            // 平台信息
//            carReceipts.setTpUser(userMapper.selectUserById(carReceipts.getThirdPartyTerraceId()));
//            // 创建人信息
//            carReceipts.setUser(userMapper.selectUserById(carReceipts.getUserId()));
//        }
        return carReceiptss;
    }

    @Override
    public CarReceipts addSellReceipts(Map map) {
        Map data = (Map) map.get("temp");
        String token = (String) map.get("token");
        System.err.println(map);
        double money = 0.0;
        double cashPledge = 0.0;

        CarReceipts carReceipts = new CarReceipts();
        // 根据token获取用户信息
        Integer userId = userTokenMapper.getUserIdByToken(token);
        System.err.println("userId" + userId);
        // 获取平台信息
        User tpUser = userMapper.selectUserById((Integer) data.get("thirdParty"));
        // 添加单据信息
        carReceipts.setCreateTime(String.valueOf(System.currentTimeMillis()));
        carReceipts.setUserId(userId);
        List<String> city = (List<String>) data.get("city");
        carReceipts.setProvince(city.get(0));
        carReceipts.setCity(city.get(1));
        carReceipts.setReceiptsStatus(1);
        carReceipts.setClientName((String) data.get("clientName"));
        carReceipts.setClientPhone(data.get("clientPhone").toString());
        List<String> carBrand = (List<String>) data.get("carType");
        carReceipts.setCarBrand(carBrand.get(0) + "/" + carBrand.get(1));
        carReceipts.setClientCarNum((String) data.get("carNum"));
        carReceipts.setThirdPartyTerraceId((Integer) data.get("thirdParty"));
        carReceipts.setCount(1);
        carReceipts.setPredictInstallTime(String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse((String) data.get("installTime"), new ParsePosition(0)).getTime()));
        // 查询销售类型
        SellType sellType = dataManagementMapper.getSellTypeById((Integer) data.get("sellType"));
        money += sellType.getMoney();
        cashPledge += sellType.getCashPledge();
        List<String> additions = (List<String>) data.get("additionType");
        String additionType = StringUtils.join(additions,"-");
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
        carReceipts.setGatheringMsg(gatheringMsg);
        System.err.println("integer=" + integer);
        carReceipts.setGatheringMsgId(gatheringMsg.getId());
        System.err.println("gatheringMsg=" + gatheringMsg);

        // 插入报表数据
        Integer carReceiptsId = sellReceiptsMapper.insertCarReceiots(carReceipts);
        if (carReceiptsId.equals("0") || carReceiptsId.equals(0)) {
            return null;
        }
        carReceipts.setUser(userMapper.selectUserById(userId));
        carReceipts.setTpUser(tpUser);
        carReceipts.setTimeMsgs(getTimeMsg(carReceipts));
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByGathering(User user) {
        // 新集合
        List<CarReceipts> carReceiptss = new ArrayList<>();
        //获取全部收款中单据数据
        List<CarReceipts> carReceipts = sellReceiptsMapper.getCarReceiptsByStatus(2,user.getId());
        System.err.println(carReceipts);
        // 补充收款收款信息
        for (CarReceipts carReceipts1 : carReceipts) {
            carReceipts1.setUser(userMapper.selectUserById(carReceipts1.getUserId()));
            carReceipts1.setTpUser(userMapper.selectUserById(carReceipts1.getThirdPartyTerraceId()));
            // 获取每个单据的收款信息
            GatheringMsg gatheringMsg = sellReceiptsMapper.getGatheringMsgById(carReceipts1.getGatheringMsgId());
            // 判断 收款人id是该用户 且 销售单据的收款信息id 是收款信息id
            if (gatheringMsg.getGatheringUserId() == user.getId() && carReceipts1.getGatheringMsgId() == gatheringMsg.getId()) {
                // 将收款信息添加到单据中
                carReceipts1.setGatheringMsg(gatheringMsg);
                // 修改时间格式
                carReceipts1.setTimeMsgs(getTimeMsg(carReceipts1));
                System.err.println();
                // 添加单据到新的集合中
                carReceiptss.add(carReceipts1);
            }
        }
        return carReceiptss;
    }

    @Override
    public List<CarReceipts> getReceiptsByReach(User user) {
        //获取该平台待到店确认的单据
        List<CarReceipts> carReceipts = sellReceiptsMapper.getCarReceiptsByStatusOfTH(1, user.getId());
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByInstall(User user) {
        // 获取该平台安装待确认的单据
        List<CarReceipts> carReceipts = sellReceiptsMapper.getCarReceiptsByStatusOfTH(3, user.getId());
        System.err.println(user);

        carReceipts = getCarreceipts(carReceipts);
        System.err.println(carReceipts);
        return carReceipts;
    }

    @Transactional
    @Override
    public Integer checkReceipts(String token, String type, Integer id, String checkTime, Map map) {
        Map data = (Map) map.get("data");
        // 获取操作人用户信息
        User user = userMapper.selectUserById(userTokenMapper.getUserIdByToken(token));
        //获取该id单据
        CarReceipts carReceipts = sellReceiptsMapper.getCarReceiptsById(id);
        if (user == null || carReceipts == null || checkTime.isEmpty()) {
            throw new RuntimeException();
        }
        List<CarReceipts> carReceiptss = new ArrayList<>();
        carReceiptss.add(carReceipts);
        // 补全信息
        carReceipts = getCarreceipts(carReceiptss).get(0);
        // 到店确认
        System.err.println(carReceipts);
        if (type.equals("reachCheck") && carReceipts.getReceiptsStatus().equals(1) && carReceipts.getReceiptsReachCheck() == null) {
            // 打桩
            System.err.println("true");
            carReceipts.setReceiptsReachCheck(checkTime);
            carReceipts.setReceiptsStatus(2);
            Integer row = sellReceiptsMapper.updateCheckTime(carReceipts, "reachCheck");
            System.err.println("type"+type);
            System.err.println(carReceipts);
            if (row.equals(0) || row.equals("0")) {
                throw new RuntimeException();
            } else {
                return row;
            }
            // 收款确认
        } else if (type.equals("gatheringCheck") && StringUtils.isEmpty(carReceipts.getGatheringMsg().getGatheringCheckTime())) {
            carReceipts.getGatheringMsg().setGatheringCheckTime(checkTime);
            carReceipts.getGatheringMsg().setText((String) data.get("text"));
            carReceipts.setReceiptsStatus(3);
            Integer row = sellReceiptsMapper.updateGatherimgMsg(carReceipts.getGatheringMsg());
            if (row > 0){
                carReceipts.setCollectionTime(checkTime);
                Integer integer = sellReceiptsMapper.updateCheckTime(carReceipts, type);
                return integer;
            }
            throw new RuntimeException();
            // 安装确认
        } else if (type.equals("installCheck") && carReceipts.getReceiptsStatus() == 3 && StringUtils.isEmpty(carReceipts.getGatheringMsg().getGatheringCheckTime())) {
            carReceipts.setThirdPartyCheckTime(checkTime);

            return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean uploadFile(Map map, MultipartFile file) {
        Random random = new Random();
        System.err.println(map);
        System.err.println(file);
        //获取上传文件操作类型
        String type = (String) map.get("fileType");
        String userId = String.valueOf(map.get("fileUserId"));
        // 获取文件名
        String fileName = file.getOriginalFilename();
        System.err.println("上传的文件名为：" + fileName);
        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        System.err.println("上传的后缀名为：" + suffixName);
        String faset = fileName.substring(0,fileName.lastIndexOf("."))+random.nextInt(10000);

        System.err.println("文件名：" + faset);
        fileName = faset + suffixName;
        // 文件上传后的路径
        String filePath = uploadDir+userId+"/";
        // 解决中文问题，liunx下中文路径，图片显示问题
        // fileName = UUID.randomUUID() + suffixName;
        File dest = new File(filePath + fileName);
        ImageUrl imageUrl = new ImageUrl();
        //判断是否收款上传图片
        if (type.equals("gatheringCheck")) {
            CarReceipts carReceipts = sellReceiptsMapper.getCarReceiptsById(Integer.parseInt((String) map.get("fileId")));
            if (carReceipts != null){

                imageUrl.setMsgId(Integer.parseInt((String) map.get("fileId")));
                imageUrl.setUrl("http://49.234.210.89/images/"+userId+"/"+fileName);

            } else {
                return false;
            }
            Integer row = sellReceiptsMapper.insertImageUrl(imageUrl);
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
                System.err.println("上传成功后的文件路径未：" + filePath + fileName);
                if (row > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public List<CarReceipts> getMyReceipts(String token) {
        User user = userUtil.getUserByToken(token);
        List<CarReceipts> carReceipts = new ArrayList<>();
        System.err.println(user);
        // 平台用户
        if ("R1004".equals(user.getRoleNum())){
            carReceipts = sellReceiptsMapper.getCarReceiptsByTPId(user.getId());
        } else {
            carReceipts = sellReceiptsMapper.getCarReceiptsByCreateId(user.getId());
        }
        if (carReceipts == null){
            return null;
        }
        // 补全信息
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public boolean cancellationCarReceiptsById(String token,Integer id) {
        User user = userUtil.getUserByToken(token);
        // id 为null 不能作废 或者操作用户为null 不能作废
        if (id == null || user == null){
            return false;
        }
        CarReceipts carReceipts = sellReceiptsMapper.getCarReceiptsById(id);
        // 单据已报废或已完成,或者单据的创建人用户ID不匹配 不能作废
        if (carReceipts.getReceiptsStatus() == 0 || carReceipts.getReceiptsStatus() == 4 || carReceipts.getUserId() != user.getId()){
            return false;
        }
        // 修改该单据状态，更新单据信息
        // 更新状态
        carReceipts.setReceiptsStatus(0);
        // 更新作废时间
        carReceipts.setCancellationTime(String.valueOf(System.currentTimeMillis()));
        // 更新到数据库
        Integer row = sellReceiptsMapper.updateCarReceiptsStatus(carReceipts);
        // 不为0更新成功
        if (row != 0){
            return true;
        }
        return false;
    }

    @Override
    public List<CarReceipts> searchQueryDate(Map map) throws ParseException {
        QueryCRMsg queryCRMsg = new QueryCRMsg();
        System.err.println(map);
        List<CarReceipts> carReceipts = new ArrayList<>();
        // 判断是大厅还是我的(DT/YD/PT)
        String type = String.valueOf(map.get("type"));
        String userId = String.valueOf(map.get("userId"));
        // 获取数据
        String sellName = String.valueOf(map.get("sellName"));
        String TPId = String.valueOf(map.get("TPId"));
        String clientName = String.valueOf(map.get("clientName"));
        String clientCarNum = String.valueOf(map.get("clientCarNum"));
        String startingDate = String.valueOf(map.get("startingDate"));
        String endDate = String.valueOf(map.get("endDay"));
        String status = String.valueOf(map.get("status"));
        String page = String.valueOf(map.get("page"));
        String count = String.valueOf(map.get("count"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 查询时间类型
        String checkTimeType = String.valueOf(map.get("checkTimeType"));
        // 销售类型
        String sellType = String.valueOf(map.get("sellType"));
        // 附加业务
        List<String> additionType = (List<String>) map.get("additionType");
        // 渠道
        String channel = String.valueOf(map.get("channel"));
        // 获取角色省份
        List<String> province = userUtil.getProvinceByUser(userMapper.selectUserById(Integer.valueOf(userId)));

        if (!StringUtils.isEmpty(startingDate)){
            Date date = sdf.parse(startingDate);
            startingDate = String.valueOf(date.getTime());
        }
        if (!StringUtils.isEmpty(endDate)){
            Date date = sdf.parse(endDate);
            endDate = String.valueOf(date.getTime());
        }
        if (!StringUtils.isEmpty(sellName)){
            sellName = "%"+sellName+"%";
        } else {
            sellName = "%%";
        }
        if (!StringUtils.isEmpty(clientName)){
            clientName = "%"+clientName+"%";
        } else {
            clientName = "%%";
        }
        if (!StringUtils.isEmpty(clientCarNum)){
            clientCarNum = "%"+clientCarNum+"%";
        } else {
            clientCarNum = "%%";
        }

        List<User> users = userMapper.getUserByLikeUserName(sellName);
        // 查询信息封装
        queryCRMsg.setUserId(Integer.valueOf(userId));
        queryCRMsg.setType(type);
        queryCRMsg.setStartingDate(startingDate);
        queryCRMsg.setEndDate(endDate);
        queryCRMsg.setSellName(sellName);
        queryCRMsg.setPage(Integer.valueOf(page));
        queryCRMsg.setCount(Integer.valueOf(count));

        if (!StringUtils.isEmpty(sellType)){
            queryCRMsg.setSellType(sellType);
        }
        if (!StringUtils.isEmpty(TPId)){
            queryCRMsg.setTPId(Integer.valueOf(TPId));
        }
        queryCRMsg.setClientName(clientName);
        queryCRMsg.setClientCarNum(clientCarNum);
        queryCRMsg.setCheckTimeType(checkTimeType);


        if (!StringUtils.isEmpty(channel)){
            queryCRMsg.setChannel(channel);
        }
        if (!StringUtils.isEmpty(status)){
            queryCRMsg.setStatus(Integer.valueOf(status));
        }


        List<CarReceipts> carReceiptsList = new ArrayList<>();
        if (!StringUtils.isEmpty(type)){

            if ("DT".equals(type)){
                // 大厅查询
                queryCRMsg.setUsers(users);
                queryCRMsg.setProvinces(province);
                List<CarReceipts> carReceipts1 = sellReceiptsMapper.searchCarReceipts(queryCRMsg);
                if (!CollectionUtils.isEmpty(additionType)){
                    for (CarReceipts carReceipts2 : carReceipts1){
                        List<String> list = Arrays.asList(carReceipts2.getAdditionType().split("-"));
                        // 附加业务是否包含查询的附加业务
                        if (CollectionUtils.isSubCollection(additionType,list)){
                            carReceiptsList.add(carReceipts2);
                        }
                    }
                    // 补全信息
                    carReceiptsList = getCarreceipts(carReceiptsList);
                    return carReceiptsList;
                }
                // 补全信息
                carReceipts1 = getCarreceipts(carReceipts1);
                return carReceipts1;
            } else if ("PT".equals(type) || "YD".equals(type)){
                // 我的
                List<CarReceipts> carReceipts1 = sellReceiptsMapper.searchCarReceipts(queryCRMsg);

                if (!CollectionUtils.isEmpty(additionType)){
                    for (CarReceipts carReceipts2 : carReceipts1){
                        List<String> list = Arrays.asList(carReceipts2.getAdditionType().split("-"));
                        // 附加业务是否包含查询的附加业务
                        if (CollectionUtils.isSubCollection(additionType,list)){
                            carReceiptsList.add(carReceipts2);
                        }
                    }
                    // 补全信息
                    carReceiptsList = getCarreceipts(carReceiptsList);
                    return carReceiptsList;
                }
                // 补全信息
                carReceipts1 = getCarreceipts(carReceipts1);
                return carReceipts1;
            } else {
                return null;
            }


        } else {
            return null;
        }






    }

    @Transactional
    @Override
    public void pushInstallMsg(Map map) {
        String userId = String.valueOf(map.get("userId"));
        String iccid = String.valueOf(map.get("iccid"));
        String equipment = String.valueOf(map.get("equipment"));
        String carNum = String.valueOf(map.get("carNum"));
        String carReceiptsId = String.valueOf(map.get("carReceiptsId"));
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(iccid) || StringUtils.isEmpty(equipment) || StringUtils.isEmpty(carNum) || StringUtils.isEmpty(carReceiptsId)){
            throw new RuntimeException("数据请求错误!#1");
        }
        // 获取单据
        CarReceipts carReceipts = sellReceiptsMapper.getCarReceiptsById(Integer.valueOf(carReceiptsId));
        Integer row = dataManagementMapper.getIccid(iccid+"_");
        EquipmentMsg equipmentMsg = dataManagementMapper.getEquipmentMsgById(Integer.valueOf(equipment));
        System.err.println(map);
        System.err.println(carReceipts == null);
        System.err.println(carReceipts.getReceiptsStatus().equals(4));
        System.err.println(carReceipts.getThirdPartyTerraceId());
        System.err.println(!userId.equals(String.valueOf(carReceipts.getThirdPartyTerraceId())));
        System.err.println(!carNum.equals(carReceipts.getClientCarNum()));
        System.err.println(row != 1 );
        System.err.println(equipmentMsg == null);
        // 判断
        if (carReceipts == null || carReceipts.getReceiptsStatus().equals(4) || !userId.equals(String.valueOf(carReceipts.getThirdPartyTerraceId())) ||
                !carNum.equals(carReceipts.getClientCarNum()) || row != 1 || equipmentMsg == null){
            throw new RuntimeException("数据请求错误!#2");
        } else {
            // 更新数据
            CarReceipts carReceipts1 = new CarReceipts();
            carReceipts1.setId(Integer.valueOf(carReceiptsId));
            carReceipts1.setReceiptsStatus(4);
            // 获取iccid信息
            String iccid1 = dataManagementMapper.getIccidByiccid(iccid+"_");
            // 更新iccid 状态
            Iccid iccid2 = new Iccid();
            iccid2.setIccid(iccid1);
            iccid2.setStatus(0);
            Integer row1 = dataManagementMapper.updateIccid(iccid2);
            carReceipts1.setIccid(iccid1);
            // 添加设备信息
            carReceipts1.setEquipmentBrand(equipmentMsg.getEquipmentBrand());
            carReceipts1.setEquipmentTypeNum(equipmentMsg.getEquipmentTypeNum());
            carReceipts1.setSize(equipmentMsg.getSize());
            carReceipts1.setThirdPartyCheckTime(String.valueOf(System.currentTimeMillis()));
            Integer line = sellReceiptsMapper.updateCarReceipts(carReceipts1);

            if (row1 != 1 || line != 1){
                throw new RuntimeException("数据请求错误!#3");
            }
        }



    }

    /**
     * 根据单据的状态 添加时间状态等
     *
     * @param carReceipts
     * @return
     */
    public List<TimeMsg> getTimeMsg(CarReceipts carReceipts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TimeMsg> timeMsgs = new ArrayList<>();

        // 变更创建时间格式
        TimeMsg timeMsg = new TimeMsg();
        timeMsg.setTypeName("创建时间");
        timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getCreateTime()))));
        timeMsg.setColor("#0bbd87");
        timeMsgs.add(timeMsg);

        // 变更预计到店时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("预计到店时间");
        timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getPredictInstallTime()))));
        timeMsg.setColor("#0bbd87");
        timeMsgs.add(timeMsg);

        // 变更到店时间格式
        timeMsg = new TimeMsg();
        timeMsg.setTypeName("到店确认");
        System.err.println(carReceipts);
        if (!StringUtils.isEmpty(carReceipts.getReceiptsReachCheck()) ) {
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getReceiptsReachCheck()))));
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
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getGatheringMsg().getGatheringCheckTime()))));
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
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getThirdPartyCheckTime()))));
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
        if (carReceipts.getReceiptsStatus() == 0 &&  carReceipts.getCancellationTime() != "") {
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getCancellationTime()))));
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
                carReceipts1.setUser(userMapper.selectUserById(carReceipts1.getUserId()));
                carReceipts1.setTpUser(userMapper.selectUserById(carReceipts1.getThirdPartyTerraceId()));
                // 获取每个单据的收款信息
                GatheringMsg gatheringMsg = sellReceiptsMapper.getGatheringMsgById(carReceipts1.getGatheringMsgId());
                // 补全收款图片地址
                List<ImageUrl> imageUrls = sellReceiptsMapper.getImageUrls(carReceipts1.getId());
                if (imageUrls != null){
                    List<String> urls = new ArrayList<>();
                    for (ImageUrl imageUrl: imageUrls){
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
