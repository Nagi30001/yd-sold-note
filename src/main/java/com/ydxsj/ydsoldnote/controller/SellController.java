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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
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

    @Value("${uploadDir}")
    private String uploadDir;


    @RequestMapping("/sell/sellmsg")
    public JSONObject getReceipts(String token) {
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
            jsonObject.put("message", "账号已失效,请重新登陆");
            jsonObject.put("code", 50014);
            return jsonObject;
        }
        // 城市信息
        List<Province> citys = userService.getCitys(user);
        // 车型信息
        List<CarType> carType = dataManagementService.getCarType();
        // 平台信息
        List<User> users = userService.getUserByRole("R1004");
        // 销售类型
        List<SellType> sellTypes = dataManagementService.getSellTypes();
        // 附加业务
        List<Addition> additions = dataManagementService.getAdditions();
        // 全部单据列表(该角色所属地区的报单数据)
        List<CarReceipts> carReceipts = sellReceiptsService.getCarReceipts(user);

        jsonObject.put("user", user);
        jsonObject.put("citys", citys);
        jsonObject.put("carType", carType);
        jsonObject.put("thirdPartyMsg", users);
        jsonObject.put("sellType", sellTypes);
        jsonObject.put("additionType", additions);
        jsonObject.put("receipts", carReceipts);
        jsonObject.put("code", 20000);
        return jsonObject;


    }

    @RequestMapping("/sell/reachCheckMsg")
    public JSONObject getCheckReceipts(String token) {
        JSONObject jsonObject = new JSONObject();
        // 获取用户信息
        User user = userService.getUserByToken(token);
        System.err.println(user);
        if (user == null) {
            System.err.println(user.toString());
            jsonObject.put("message", "账号已失效,请重新登陆");
            jsonObject.put("code", 50014);
            return jsonObject;
        }
        List<String> roles = Arrays.asList(user.getRoleNum().split(","));
        System.err.println("roles"+roles);

        // 判断是优道管理 还是平台用户
        if (roles.contains("R1001") || roles.contains("R1002") || roles.contains("R1003"))  {
            //优道管理
            //获取收款待确认信息
            List<CarReceipts> carReceipts = sellReceiptsService.getReceiptsByGathering(user);
            System.err.println(carReceipts);
            jsonObject.put("user", user);
            jsonObject.put("gatheringCheck", carReceipts);
            jsonObject.put("code", 20000);
            return jsonObject;
        } else if (roles.contains("R1004")) {
            //平台用户
            //获取到店待确认信息
            List<CarReceipts> carReceipts1 = sellReceiptsService.getReceiptsByReach(user);
            //获取收款待确认信息
            List<CarReceipts> carReceipts2 = sellReceiptsService.getReceiptsByGathering(user);
            //获取安装待确认信息
            List<CarReceipts> carReceipts3 = sellReceiptsService.getReceiptsByInstall(user);
            jsonObject.put("user", user);
            jsonObject.put("gatheringCheck", carReceipts2);
            jsonObject.put("reachCheck", carReceipts1);
            jsonObject.put("installCheck", carReceipts3);
            jsonObject.put("code", 20000);
            return jsonObject;

        } else {
            // 无效角色
            jsonObject.put("message", "账号已失效,请重新登陆");
            jsonObject.put("code", 50014);
            return jsonObject;
        }
    }

    /**
     * 销售单据新增
     *
     * @param map
     * @return
     */
    @PostMapping("/sell/addSellReceipts")
    public JSONObject addSellReceipts(@RequestBody Map map) {
        System.err.println(map.toString());
        JSONObject jsonObject = new JSONObject();
        CarReceipts carReceipts = sellReceiptsService.addSellReceipts(map);
        if (carReceipts != null) {
            jsonObject.put("newCarReceipts", carReceipts);
            jsonObject.put("code", 20000);
            jsonObject.put("message", "添加成功");
            return jsonObject;
        } else {
            jsonObject.put("code", 20001);
            jsonObject.put("message", "添加失败");

            return jsonObject;
        }

    }


    /**
     * 状态确认
     *
     * @param map
     * @return
     */
    @RequestMapping("/sell/reachCheck")
    public JSONObject reachCheck(@RequestBody Map map) {
        JSONObject jsonObject = new JSONObject();
        String token = (String) map.get("token");
        String type = (String) map.get("type");
        Integer id = (Integer) map.get("id");
        Map checkData = (Map) map.get("data");
        System.err.println(checkData);
        String checkTime = String.valueOf(checkData.get("time"));
        System.err.println("token:" + token);
        System.err.println("type:" + type);
        System.err.println("id:" + id);
        Integer result = sellReceiptsService.checkReceipts(token,type,id,checkTime,map);
        if (result.equals(1)){
            jsonObject.put("code",20000);
            jsonObject.put("message","确认成功！");
            return jsonObject;
        } else {
            jsonObject .put("message","未知异常");
            return jsonObject;
        }
//        return null;
    }

    /**
     * 接收图片
     *
     * @param map
     * @param file
     * @return
     */
    @RequestMapping("/sell/uploadFile")
    public JSONObject uploadFile(@RequestParam Map map, @RequestParam("file") MultipartFile file) {
        System.err.println("map===="+map);
        System.err.println("file===="+file);
        JSONObject jsonObject = new JSONObject();
          boolean i = sellReceiptsService.uploadFile(map, file);
        if (i) {
            jsonObject.put("code", 20000);
            jsonObject.put("message", "上传成功");
            return jsonObject;
        } else {
            jsonObject.put("code", 20001);
            jsonObject.put("message", "上传失败");
            return jsonObject;
        }
    }


    /**
     * 获取该用户的报单数据，如果是平台，则获取该平台的单据
     * @param token
     * @return
     */
    @RequestMapping("/sell/getMyReceipts")
    public JSONObject getMyReceipts(String token){
        JSONObject jsonObject = new JSONObject();
        List<CarReceipts> carReceipts = sellReceiptsService.getMyReceipts(token);
        jsonObject.put("code",20000);
        jsonObject.put("myReceipts",carReceipts);
        return jsonObject;
    }

    /**
     * 作废单据
     * @param id
     * @return
     */
    @RequestMapping("/sell/cancellation")
    public JSONObject cancellation(String token,Integer id){
        JSONObject jsonObject = new JSONObject();

        boolean row = sellReceiptsService.cancellationCarReceiptsById(token,id);

        if (row){
            jsonObject.put("code",20000);
            return jsonObject;
        } else {
            jsonObject.put("code",20001);
            jsonObject.put("message","已报废或无法报废该单据");
            return jsonObject;
        }
    }

    /**
     * 根据条件查询单据
     * @param map
     * @return
     */
    @RequestMapping("/sell/searchQueryDate")
    public JSONObject searchQueryDate(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        List<CarReceipts> carReceipts = null;
        try {
            carReceipts = sellReceiptsService.searchQueryDate(map);
            jsonObject.put("code",20000);
            jsonObject.put("carReceipts",carReceipts);
            return jsonObject;
        } catch (ParseException e) {
            jsonObject.put("code",20001);
            jsonObject.put("message","查询错误，请重新查询");
            return jsonObject;
        }


    }


}
