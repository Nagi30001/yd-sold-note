package com.ydxsj.ydsoldnote.controller;

import com.alibaba.fastjson.JSONObject;
import com.ydxsj.ydsoldnote.base.BaseResponse;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;
import com.ydxsj.ydsoldnote.service.UserService;
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

    /**
     * 登陆
     * @param username
     * @param password
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
}
