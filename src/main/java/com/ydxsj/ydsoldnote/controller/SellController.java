package com.ydxsj.ydsoldnote.controller;

import com.alibaba.fastjson.JSONObject;
import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.CarType;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.SellReceiptsService;
import com.ydxsj.ydsoldnote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class SellController {

    @Autowired
    private UserService userService;
    @Autowired
    private DataManagementService dataManagementService;
    @Autowired
    private SellReceiptsService sellReceiptsService;


    @RequestMapping("/sell/sellmsg")
    public JSONObject getReceipts(String token){
        //打桩
        System.err.println("走到这里了----------" + token);
//        String token = (String) map.get("token");
        JSONObject jsonObject = new JSONObject();
        // 用户信息
//        try {
        User user = null;
        try {
            user = userService.getUserByToken(token);
        } catch (Exception e) {
            jsonObject.put("message","账号已失效,请重新登陆");
            jsonObject.put("code",50014);
            return jsonObject;
        }
            // 城市信息
            List<Province> citys = userService.getCitys(user);
            // 车型信息
            List<CarType>carType = dataManagementService.getCarType();
            // 平台信息
            List<User> users = userService.getUserByRole("R1004");
            // 销售类型
            List<SellType> sellTypes = dataManagementService.getSellTypes();
            // 附加业务
            List<Addition> additions = dataManagementService.getAdditions();
            // 全部单据列表(该角色所属地区的报单数据)
            List<CarReceipts> carReceipts = sellReceiptsService.getCarReceipts(user);

            jsonObject.put("user",user);
            jsonObject.put("citys",citys);
            jsonObject.put("carType",carType);
            jsonObject.put("thirdPartyMsg",users);
            jsonObject.put("sellType",sellTypes);
            jsonObject.put("additionType",additions);
            jsonObject.put("receipts",carReceipts);
            jsonObject.put("code", 20000);
            return jsonObject;


    }

    @RequestMapping("/sell/reachCheckMsg")
    public JSONObject getCheckReceipts(String token){
        JSONObject jsonObject = new JSONObject();
        // 获取用户信息
        User user = userService.getUserByToken(token);
        if (user == null){
            System.err.println(user.toString());
            jsonObject.put("message","账号已失效,请重新登陆");
            jsonObject.put("code",50014);
            return jsonObject;
        }
        List<String> roles = Arrays.asList(user.getRoleNum().split(","));

        // 判断是优道管理 还是平台用户
            if (roles.contains("R1001") || roles.contains("R1002")) {
                //优道管理
                //获取收款待确认信息
                List<CarReceipts> carReceipts = sellReceiptsService.getReceiptsByGathering(user);
                jsonObject.put("user", user);
                jsonObject.put("gatheringCheck", carReceipts);
                jsonObject.put("code", 20000);
                return jsonObject;
            } else if (roles.contains("R1004")){
                //平台用户
                //获取到店待确认信息
                List<CarReceipts> carReceipts1 = sellReceiptsService.getReceiptsByReach(user);
                //获取收款待确认信息
                List<CarReceipts> carReceipts2 = sellReceiptsService.getReceiptsByGathering(user);
                //获取安装待确认信息
                List<CarReceipts> carReceipts3 = sellReceiptsService.getReceiptsByInstall(user);
                jsonObject.put("user",user);
                jsonObject.put("gatheringCheck",carReceipts2);
                jsonObject.put("reachCheck",carReceipts1);
                jsonObject.put("installCheck",carReceipts3);
                jsonObject.put("code",20000);
                return jsonObject;

            } else {
                // 无效角色
                jsonObject.put("message","账号已失效,请重新登陆");
                jsonObject.put("code",50014);
                return jsonObject;
            }
    }

    /**
     * 销售单据新增
     * @param map
     * @return
     */
    @PostMapping("/sell/addSellReceipts")
    public JSONObject addSellReceipts(@RequestBody Map map){
        System.err.println(map.toString());
        JSONObject jsonObject = new JSONObject();
        try {
            CarReceipts carReceipts = sellReceiptsService.addSellReceipts(map);
            jsonObject.put("newCarReceipts",carReceipts);
            jsonObject.put("code",20000);
            jsonObject.put("msg","添加成功");
        } catch (Exception e){
            jsonObject.put("code",20001);
            jsonObject.put("msg","添加失败");
        }

        return jsonObject;
    }


}
