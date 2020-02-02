package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.GatheringMsg;
import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.data.TimeMsg;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.*;
import com.ydxsj.ydsoldnote.service.SellReceiptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public List<CarReceipts> getCarReceipts(User user) {
        // 获取用户省份
        List<String> provincesId = Arrays.asList((user.getBeProvince().split("-")));
        System.err.println("provincesId:"+provincesId.toString());
        //使用省份获取省份名称
        List<String> provinces = cityMapper.getProvinceById(provincesId);
        System.err.println("provinces:"+provinces.toString());
        // 使用省份名称获取对应的
        List<CarReceipts> carReceiptss = sellReceiptsMapper.getCarReceiptsByProvince(provinces);

        // 补全信息
        for (CarReceipts carReceipts : carReceiptss) {
            // 收款信息
            carReceipts.setGatheringMsg(sellReceiptsMapper.getGatheringMsgById(carReceipts.getGatheringMsgId()));
            // 状态模块
            carReceipts.setTimeMsgs(getTimeMsg(carReceipts));
            // 平台信息
            carReceipts.setTpUser(userMapper.selectUserById(carReceipts.getThirdPartyTerraceId()));
            // 创建人信息
            carReceipts.setUser(userMapper.selectUserById(carReceipts.getUserId()));
        }
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
        System.err.println("userId"+userId);
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
        List<String> carBrand = (List<String>)data.get("carType");
        carReceipts.setCarBrand(carBrand.get(0)+"/"+carBrand.get(1));
        carReceipts.setClientCarNum((String) data.get("carNum"));
        carReceipts.setThirdPartyTerraceId((Integer) data.get("thirdParty"));
        carReceipts.setCount(1);
        carReceipts.setPredictInstallTime(String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse((String) data.get("installTime"),new ParsePosition(0)).getTime()));
        SellType sellType = dataManagementMapper.getSellTypeById((Integer)data.get("sellType"));
        money += sellType.getMoney();
        cashPledge += sellType.getCashPledge();
        List<Integer> additionIds = (List<Integer>) data.get("additionType");
        List<Addition> additions = new ArrayList<>();
        if (additionIds.size() > 0){
            additions = dataManagementMapper.getAdditionsById(additionIds);
            String additionType = "";
            for (Addition addition : additions){
                additionType += addition.getAdditionName() + "/";
                money += addition.getMoney();
            }
            carReceipts.setAdditionType(additionType);
        }
        carReceipts.setSellTypeName(sellType.getSellType());
        carReceipts.setMoney(money);
        carReceipts.setCashPledge(cashPledge);
        GatheringMsg gatheringMsg = new GatheringMsg();
        if (data.get("gatheringType").toString().equals("YD")){
            carReceipts.setGatheringType("优道科技");
            gatheringMsg.setYdGathering(1);
            gatheringMsg.setThirdPartyGathering(0);
            gatheringMsg.setGatheringStatus(0);
            gatheringMsg.setGatheringUserId(1);
        } else {
            carReceipts.setGatheringType(tpUser.getUserName());
            gatheringMsg.setYdGathering(0);
            gatheringMsg.setThirdPartyGathering(1);
            gatheringMsg.setGatheringStatus(0);
            gatheringMsg.setGatheringUserId((Integer) data.get("thirdParty"));
        }
        Integer integer = sellReceiptsMapper.insertGatheringMsg(gatheringMsg);
        System.err.println("integer="+integer);
        carReceipts.setGatheringMsgId(gatheringMsg.getId());
        System.err.println("gatheringMsg="+gatheringMsg);

        // 插入报表数据
        Integer carReceiptsId = sellReceiptsMapper.insertCarReceiots(carReceipts);
        if (carReceiptsId.equals("0") || carReceiptsId.equals(0)){
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
        List<CarReceipts> carReceipts = sellReceiptsMapper.getCarReceiptsByStatus(2);
        // 补充收款收款信息
        for (CarReceipts carReceipts1 : carReceipts){
            carReceipts1.setUser(userMapper.selectUserById(carReceipts1.getUserId()));
            carReceipts1.setTpUser(userMapper.selectUserById(carReceipts1.getThirdPartyTerraceId()));
            // 获取每个单据的收款信息
            GatheringMsg gatheringMsg = sellReceiptsMapper.getGatheringMsgById(carReceipts1.getGatheringMsgId());
            // 判断 收款人id是该用户 且 销售单据的收款信息id 是收款信息id
            if (gatheringMsg.getGatheringUserId() == user.getId() && carReceipts1.getGatheringMsgId() == gatheringMsg.getId() ){
                // 将收款信息添加到单据中
                carReceipts1.setGatheringMsg(gatheringMsg);
                // 修改时间格式
                carReceipts1.setTimeMsgs(getTimeMsg(carReceipts1));
                // 添加单据到新的集合中
                carReceiptss.add(carReceipts1);
            }
        }
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByReach(User user) {
        //获取该平台待到店确认的单据
        List<CarReceipts> carReceipts = sellReceiptsMapper.getCarReceiptsByStatusOfTH(1,user.getId());
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public List<CarReceipts> getReceiptsByInstall(User user) {
        // 获取该平台安装待确认的单据
        List<CarReceipts> carReceipts = sellReceiptsMapper.getCarReceiptsByStatusOfTH(3,user.getId());
        carReceipts = getCarreceipts(carReceipts);
        return carReceipts;
    }

    @Override
    public Integer checkReceipts(String token, String type, Integer id) {
        // 获取操作人用户信息
        User user = userMapper.selectUserById(userTokenMapper.getUserIdByToken(token));
        //获取该id单据
        CarReceipts carReceipts = sellReceiptsMapper.getCarReceiptsById(id);
        // 补全信息
        if(user == null || carReceipts == null ){
            return null;
        }
        if (type.equals("reachCheck") && carReceipts.getReceiptsStatus() == 2 && carReceipts.getReceiptsReachCheck().isEmpty()){
            return null;
        } else if (type.equals("gatheringCheck") && carReceipts.getGatheringMsg().getGatheringCheckTime().isEmpty()){
            return null;
        } else if (type.equals("installCheck") && carReceipts.getReceiptsStatus() == 3 && !carReceipts.getGatheringMsg().getGatheringCheckTime().isEmpty()){
            return null;
        } else {
            return null;
        }
    }

    /**
     * 根据单据的状态 添加时间状态等
     * @param carReceipts
     * @return
     */
    public List<TimeMsg> getTimeMsg(CarReceipts carReceipts){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TimeMsg> timeMsgs = new ArrayList<>();

        TimeMsg timeMsg = new TimeMsg();
        timeMsg.setTypeName("创建时间");
        timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getCreateTime()))));
        timeMsg.setColor("#0bbd87");
        timeMsgs.add(timeMsg);

        timeMsg = new TimeMsg();
        timeMsg.setTypeName("预计到店时间");
        timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getPredictInstallTime()))));
        timeMsg.setColor("#0bbd87");
        timeMsgs.add(timeMsg);

        timeMsg = new TimeMsg();
        timeMsg.setTypeName("到店确认");
        if (carReceipts.getReceiptsStatus() > 1){
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getReceiptsReachCheck()))));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        } else {
            timeMsg.setTypeTime("...");
            timeMsg.setType("primary");
            timeMsg.setIcon("el-icon-more");
            timeMsgs.add(timeMsg);
        }

        timeMsg = new TimeMsg();
        timeMsg.setTypeName("收款确认");
        if (carReceipts.getReceiptsStatus() > 2){
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getGatheringMsg().getGatheringCheckTime()))));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        } else {
            timeMsg.setTypeTime("...");
            timeMsg.setType("primary");
            timeMsg.setIcon("el-icon-more");
            timeMsgs.add(timeMsg);
        }

        timeMsg = new TimeMsg();
        timeMsg.setTypeName("安装确认");
        if (carReceipts.getReceiptsStatus() > 3){
            timeMsg.setTypeTime(sdf.format(new Date(Long.parseLong(carReceipts.getThirdPartyCheckTime()))));
            timeMsg.setColor("#0bbd87");
            timeMsgs.add(timeMsg);
        } else {
            timeMsg.setTypeTime("...");
            timeMsg.setType("primary");
            timeMsg.setIcon("el-icon-more");
            timeMsgs.add(timeMsg);
        }

        return timeMsgs;
    }

    /**
     * 查询补全信息// 到店待确认，安装待确认
     * @return
     */
    public List<CarReceipts> getCarreceipts(List<CarReceipts> carReceipts){
        List<CarReceipts> carReceiptss = new ArrayList<>();
        if(carReceipts != null){
            for (CarReceipts carReceipts1 : carReceipts){
                carReceipts1.setUser(userMapper.selectUserById(carReceipts1.getUserId()));
                carReceipts1.setTpUser(userMapper.selectUserById(carReceipts1.getThirdPartyTerraceId()));
                // 获取每个单据的收款信息
                GatheringMsg gatheringMsg = sellReceiptsMapper.getGatheringMsgById(carReceipts1.getGatheringMsgId());
                // 判断 收款人id是该用户 且 销售单据的收款信息id 是收款信息id
                if (carReceipts1.getGatheringMsgId() == gatheringMsg.getId() ){
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
