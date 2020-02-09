package com.ydxsj.ydsoldnote.controller;

import com.alibaba.fastjson.JSONObject;
import com.ydxsj.ydsoldnote.base.BaseResponse;
import com.ydxsj.ydsoldnote.bean.data.City;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.role.Role;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private DataManagementService dataManagementService;

    /**
     * 登陆
     * @return
     */
    @RequestMapping("/user/login")
    public JSONObject login(@RequestBody Map map) {
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        System.err.println("用户名："+username+"------密码："+password);
        JSONObject json = new JSONObject();
        json.put("result", false);
        json.put("message", "账号或密码不正确");

        // 用户信息
//        ManagerInfo managerInfo = managerService.getManagerInfo(username);
        User user = userService.getUserByJobName(username);
        // 账号不存在、密码错误
        if (user == null || !user.getJobPassword().equals(password)) {
            return json;
        }

        UserToken userToken = userService.saveToken(user.getId());
        json.put("token", userToken.getToken());
        json.put("result", true);
        json.put("message", "登陆成功");
        json.put("code", 20000);

        return json;
    }

    /**
     * 根据token获取他的权限
     * @param token
     * @return
     */
    @RequestMapping("/user/info")
    public JSONObject info(String token){
        JSONObject json = new JSONObject();
        System.err.println("获取权限："+token);
        if (token != null && token != ""){
            List<String> roles = userService.getUserInfoByToken(token);
            json.put("code",20000);
            json.put("roles",roles);
            return json;
        } else {
            json.put("code",20001);
            json.put("message","权限错误");
            return json;
        }
    }

    /**
     * 必须带token请求, 否则返回401
     */
    @GetMapping("/article")
    public BaseResponse article() {
        return new BaseResponse(true, "article: You are already logged in", null);
    }

    /**
     * 不必带token也能请求到内容, 因为在shiro中配置了过滤规则
     */
    @GetMapping("/app/article")
    public BaseResponse appArticle() {
        return new BaseResponse(true, "appArticle: You are already logged in", null);
    }

    /**
     * 需要是超级管理员的token才能查看,
     */
    @GetMapping("/require_role")
//    2个以上必须同时拥有才能访问
    @RequiresRoles("R1001")
//    二者有一即可访问
//    @RequiresRoles(value = {"R1001","R1005"},logical = Logical.OR)
    public BaseResponse requireRole() {
        return new BaseResponse(true, "You are visiting require_role", null);
    }

    /**
     * 需要有update权限才能访问
     */
    @GetMapping("/require_permission")
    // @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
//    拥有该update的permission才能访问
//    @RequiresPermissions(logical = Logical.AND, value = {"20001"})
    @RequiresPermissions("20001")
    public BaseResponse requirePermission() {
        return new BaseResponse(true, "You are visiting permission require update", null);
    }

    /**
     * 登出请求
     * @return
     */
    @RequestMapping("/user/logout")
    public JSONObject logout(){
        JSONObject jsonObject  =  new JSONObject();
        jsonObject.put("code",20000);
        return jsonObject;
    }

    @RequestMapping("/user/getUsers")
    public JSONObject getUsers(String token){
        JSONObject jsonObject  =  new JSONObject();
        if (StringUtils.isEmpty(token)){
            jsonObject.put("code",20001);
            jsonObject.put("message","登陆失效，请重新登陆");
            return jsonObject;
        }
        // 用户信息
        User user = userService.getUserByToken(token);
        if (user == null ){
            jsonObject.put("code",20001);
            jsonObject.put("message","登陆失效，请重新登陆");
            return jsonObject;
        }
        // 获取yd用户
        List<User> ydUsers = userService.getUsersByType(token,"yd");
        // 获取平台用户
        List<User> ptUsers = userService.getUsersByType(token,"pt");
        // 获取用户省份信息
        List<Province> provinces = dataManagementService.getProvinces(token);
        // 获取用户城市信息
        List<City> cities = dataManagementService.getCitysByProvinces(provinces);
        // 权限信息
        List<Role> roles = userService.getRolesBy(user);

        jsonObject.put("roles",roles);
        jsonObject.put("user",user);
        jsonObject.put("ydUsers",ydUsers);
        jsonObject.put("ptUsers",ptUsers);
        jsonObject.put("provinces",provinces);
        jsonObject.put("cities",cities);
        jsonObject.put("code",20000);
        jsonObject.put("message","获取成功用户信息");
        return jsonObject;
    }


    /**
     * 添加用户
     * @param map
     * @return
     */
    @RequestMapping("/user/addUser")
    public JSONObject addUser(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();
        System.err.println(map);
        String token = (String) map.get("token");
        Map userMap = (Map) map.get("data");
        User user = userService.addUser(token,userMap);
        if (user == null){
            jsonObject.put("code",20001);
            jsonObject.put("message","添加失败");
            return jsonObject;
        } else {
            jsonObject.put("user",user);
            jsonObject.put("code",20000);
            jsonObject.put("message","添加成功");
            return jsonObject;
        }
    }

    /**
     * 检查工号是否被占用
     * @param value
     * @return
     */
    @RequestMapping("/user/checkJobNum")
    public JSONObject checkJobNum(String value){
        System.err.println(value);
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isEmpty(value)){
            jsonObject.put("code",2000);

            jsonObject.put("message","异常");
            return jsonObject;
        }
        boolean row = userService.checkJobNum(value);
        if (!row){
            jsonObject.put("code",20000);
            jsonObject.put("res","No");
            return jsonObject;
        } else {
            jsonObject.put("code",20000);
            return jsonObject;
        }
    }


    /**
     * 更新用户信息
     * @param map
     * @return
     */
    @RequestMapping("/user/updateUser")
    public JSONObject updateUser(@RequestBody Map map){
        JSONObject jsonObject = new JSONObject();

        String token = (String) map.get("token");
        Map userMap = (Map) map.get("data");
        boolean row = userService.updateUser(token,userMap);
        if (!row){
            jsonObject.put("code",20001);
            jsonObject.put("message","异常");
            return jsonObject;
        } else {
            jsonObject.put("code",20000);
            jsonObject.put("message","更新成功");
            return jsonObject;
        }

    }
}
