package com.ydxsj.ydsoldnote.controller;

import com.alibaba.fastjson.JSONObject;
import com.ydxsj.ydsoldnote.bean.data.*;
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
}
