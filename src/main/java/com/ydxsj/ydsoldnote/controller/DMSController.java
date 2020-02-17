package com.ydxsj.ydsoldnote.controller;

import com.alibaba.fastjson.JSONObject;
import com.ydxsj.ydsoldnote.bean.data.Channel;
import com.ydxsj.ydsoldnote.bean.data.City;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.data.equipment.*;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            jsonObject.put("code",20000);
            jsonObject.put("informationMsg",inventoryMsg);
            jsonObject.put("transferMsg",transferMsg);
            jsonObject.put("changeMsg",changeMsg);
            return jsonObject;

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
            return jsonObject;
        } else {
            jsonObject.put("code",20001);
            jsonObject.put("message","用户信息失效");
            return jsonObject;
        }
    }
}
