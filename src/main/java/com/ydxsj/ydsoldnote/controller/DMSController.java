package com.ydxsj.ydsoldnote.controller;

import com.alibaba.fastjson.JSONObject;
import com.ydxsj.ydsoldnote.bean.data.*;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
public class DMSController {

    @Autowired
    private DataManagementService dataManagementService;
    @Autowired
    private UserService userService;

    private final static String PLATFORM_ABBREVIATION = "PT";
    private final static String YOUDAO_ABBREVIATION = "YD";
    private final static String SUPER_ADMIN = "R1001";
    private final static String ADMIN = "R1002";
    private final static String SELL_USER = "R1003";
    private final static String PLATFORM_USER = "R1004";
    private final static String YOUDAO_PLATFORM_USER= "R1005";


    /**
     * 渠道页面初始化信息
     * @param token
     * @return
     */
    @RequestMapping("/getChannelMsg")
    public JSONObject getChannelMsg(String token){
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isEmpty(token)){
            jsonObject.put("code",20001);
            jsonObject.put("message","账号失效，请重新登陆");
        }
        System.err.println("渠道权限"+token);
        // 渠道信息
        List<Channel> channels =  dataManagementService.getChannelMsgs(token);
        // 省份信息
        List<Province> provinces = dataManagementService.getProvinces(token);
        // 城市信息
        List<City> cities = dataManagementService.getCitysByProvinces(provinces);
        // 用户信息
        User user = userService.getUserByToken(token);
        jsonObject.put("user",user);
        jsonObject.put("channelMsgs",channels);
        jsonObject.put("provinces",provinces);
        jsonObject.put("cities",cities);
        jsonObject.put("code",20000);
        jsonObject.put("message","成功获取渠道信息");
        return jsonObject;
    }

    /**
     *  添加渠道信息
     * @param map
     * @return
     */
    @RequestMapping("/addChannel")
    public JSONObject addChannelMsg(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        System.err.println(map);
        String token = (String) map.get("token");
        Map channelMsg = (Map) map.get("channel");
        if (StringUtils.isEmpty(token) || channelMsg == null){
            jsonObject.put("code",20001);
            jsonObject.put("message","账号失效，请重新登陆");
            return jsonObject;
        }
        Channel channel = dataManagementService.addChannelMsg(token,channelMsg );
        jsonObject.put("channel",channel);
        jsonObject.put("code",20000);
        jsonObject.put("message","添加成功");
        return jsonObject;
    }

    /**
     * 更新渠道信息
     * @param map
     * @return
     */
    @RequestMapping("/updateChannel")
    public JSONObject updateChannelMsg(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        String token = (String) map.get("token");
        Map channelMsg = (Map) map.get("channel");
        if (StringUtils.isEmpty(token) || channelMsg == null){
            jsonObject.put("code",20001);
            jsonObject.put("message","账号失效，请重新登陆");
            return jsonObject;
        }
        Channel channel = dataManagementService.updateChannelMsg(token,channelMsg );
        jsonObject.put("channel",channel);
        jsonObject.put("code",20000);
        jsonObject.put("message","更新成功");
        return jsonObject;
    }


    /**
     * 获取销售类型信息
     * @return
     */
    @RequestMapping("/getSellType")
    public JSONObject getSellType(){
        JSONObject jsonObject = new JSONObject();
        List<SellType> sellTypes = dataManagementService.getSellTypes();
        jsonObject.put("code",20000);
        jsonObject.put("sellTypes",sellTypes);
        return jsonObject;
    }

    /**
     * 获取设备型号信息
     * @return
     */
    @RequestMapping("/getEquipmentMsg")
    public JSONObject getEquipmentMsg(){
        JSONObject jsonObject = new JSONObject();
        List<EquipmentMsg> equipmentMsg = dataManagementService.getEquipmentMsg();
        jsonObject.put("code",20000);
        jsonObject.put("equipmentMsg",equipmentMsg);
        return jsonObject;
    }

    /**
     * 添加设备信息
     * @param map
     * @return
     */
    @PostMapping("/addEquipment")
    public JSONObject addEquipment(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        System.err.println(map);
        EquipmentMsg equipmentMsg = dataManagementService.addEquipment(map);
        if (equipmentMsg != null){
            jsonObject.put("code",20000);
            jsonObject.put("equipmentMsg",equipmentMsg);
            return jsonObject;
        } else {
            jsonObject.put("code",20001);
            jsonObject.put("message","添加失败");
            return jsonObject;
        }

    }


    /**
     * 优道用户:获取该省份权限内所有平台的 设备库存信息/维修信息/采购信息/报废信息/转移信息/更换信息
     * 安装平台:获取自己的 设备库存信息/转移信息/更换信息
     * @param token
     * @return
     */
    @RequestMapping("/getEquipmentInventory")
    public JSONObject getEquipmentInventory(String token){
        JSONObject jsonObject = new JSONObject();
        // 判断用户是什么用户
        User user = userService.getUserByToken(token);
        if (user == null){
            jsonObject.put("code",20001);
            jsonObject.put("message","用户信息失效");
            return jsonObject;
        }
        // 安装平台
        if (PLATFORM_USER.equals(user.getRoleNum())){
            // 获取库存信息
            List<InventoryMsg> inventoryMsg = dataManagementService.getInventoryMsgByTPId(user, PLATFORM_ABBREVIATION);
            // 获取转移信息
            List<TransferMsg> transferMsg = dataManagementService.getTransferMsgById(user,PLATFORM_ABBREVIATION);
            // 获取更换信息
            List<ChangeMsg> changeMsg = dataManagementService.getChangeMsgByTPId(user,PLATFORM_ABBREVIATION);
            // 采购信息
            List<PurchaseMsg> purchaseMsg = dataManagementService.getPurchaseMsg(user,YOUDAO_ABBREVIATION);
            jsonObject.put("code",20000);
            jsonObject.put("informationMsg",inventoryMsg);
            jsonObject.put("transferMsg",transferMsg);
            jsonObject.put("changeMsg",changeMsg);
            jsonObject.put("purchaseMsg",purchaseMsg);
        } else if (SUPER_ADMIN.equals(user.getRoleNum()) || ADMIN.equals(user.getRoleNum()) || YOUDAO_PLATFORM_USER.equals(user.getRoleNum()) ){
            //优道用户
            // 获取库存信息
            List<InventoryMsg> inventoryMsg = dataManagementService.getInventoryMsgByTPId(user, YOUDAO_ABBREVIATION);
            // 获取维修信息
            List<MaintainMsg> maintainMsg = dataManagementService.getMaintainMsg(user,YOUDAO_ABBREVIATION);
            // 获取采购信息
            List<PurchaseMsg> purchaseMsg = dataManagementService.getPurchaseMsg(user,YOUDAO_ABBREVIATION);
            // 获取转移信息
            List<TransferMsg> transferMsg = dataManagementService.getTransferMsgById(user,YOUDAO_ABBREVIATION);
            // 获取更换信息
            List<ChangeMsg> changeMsg = dataManagementService.getChangeMsgByTPId(user,YOUDAO_ABBREVIATION);
            // 获取报废信息
            List<ScrapMsg> scrapMsg = dataManagementService.getScrapMsg(user,YOUDAO_ABBREVIATION);
            jsonObject.put("code",20000);
            jsonObject.put("informationMsg",inventoryMsg);
            jsonObject.put("transferMsg",transferMsg);
            jsonObject.put("changeMsg",changeMsg);
            jsonObject.put("maintainMsg",maintainMsg);
            jsonObject.put("purchaseMsg",purchaseMsg);
            jsonObject.put("scrapMsg",scrapMsg);
        } else {
            jsonObject.put("code",20001);
            jsonObject.put("message","用户信息失效");
            return jsonObject;
        }


        // 设备品牌信息
        List<EquipmentMsg> equipmentMsg = dataManagementService.getEquipmentMsg();
        // 省份权限列表
        List<Province> provinces = dataManagementService.getProvinces(token);
        // 城市权限列表
        List<City> cities = dataManagementService.getCitysByProvinces(provinces);
        // 收货平台列表
        List<User> users = userService.getPlatformsByProvince(provinces);
        jsonObject.put("equipmentMsg",equipmentMsg);
        jsonObject.put("provinces",provinces);
        jsonObject.put("cities",cities);
        jsonObject.put("platform",users);
        return jsonObject;
    }


    /**
     * 获取车型数据
     * @return
     */
    @GetMapping("/getCarTypeMsg")
    public JSONObject getCarTypeMsg(){
        JSONObject jsonObject = new JSONObject();
        List<CarType> carTypeList = dataManagementService.getCarTypeMsg();

        jsonObject.put("code",20000);
        jsonObject.put("carTypeMsg",carTypeList);
        return jsonObject;
    }

    /**
     * 添加车型信息
     * @return
     */
    @PostMapping("/addCarType")
    public JSONObject addCarType(@RequestBody Map map){
        System.err.println(map);
        JSONObject jsonObject = new JSONObject();
        CarType carType = dataManagementService.addCarType(map);
        if (carType == null){
            jsonObject.put("code",200001);
            jsonObject.put("message","添加失败");
            return jsonObject;
        } else {
            jsonObject.put("code",20000);
            jsonObject.put("carType",carType);
            return jsonObject;
        }
    }

    /**
     * 添加采购单据
     * @param map
     * @return
     */
    @PostMapping("/addPurchaseMsg")
    public JSONObject addPurchaseMsg(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        try {
            PurchaseMsg purchaseMsg =  dataManagementService.addPurchaseMsg(map);
            jsonObject.put("code",20000);
            jsonObject.put("purchaseMsg",purchaseMsg);
            return jsonObject;
        } catch(Exception e){
            jsonObject.put("code",20001);
            jsonObject.put("message",e.getMessage());
            return jsonObject;
        }
    }

    /**
     * 作废采购单据
     * @return
     */
    @PostMapping("/scrapPurchaseMsg")
    public JSONObject scrapPurchaseMsg(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        try {
            dataManagementService.scrapPurchaseMsg(map);
            jsonObject.put("code",20000);
            return jsonObject;

        } catch (Exception e){
            jsonObject.put("code",20001);
            jsonObject.put("message",e.getMessage());
            return jsonObject;
        }

    }

    /**
     * 采购单据收货
     * @param map
     * @return
     */
    @PostMapping("/receivePurchaseMsg")
    public JSONObject receivePurchaseMsg(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        try{
            dataManagementService.receivePurchaseMsg(map);
            jsonObject.put("code",20000);
            return jsonObject;
        } catch (Exception e){
            jsonObject.put("code",20001);
            jsonObject.put("message",e.getMessage());
            return jsonObject;
        }
    }

    /**
     * 检查iccid是否可以正常使用
     * @return
     */
    @PostMapping("/checkIccid")
    public JSONObject checkIccid(String iccid){
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isEmpty(iccid)){
            jsonObject.put("code",20000);
            jsonObject.put("result",false);
            jsonObject.put("message","请检查iccid信息！");
            return jsonObject;
        } else {
            CheckIccidResult checkIccidResult = dataManagementService.checkIccid(iccid);
            if (!checkIccidResult.getResult()){
                jsonObject.put("code",20000);
                jsonObject.put("result",false);
                jsonObject.put("message",checkIccidResult.getMessage());
                return jsonObject;
            } else {
                jsonObject.put("code",20000);
                jsonObject.put("result",true);
                return jsonObject;
            }
        }
    }
}
