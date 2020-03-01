package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.role.Role;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.config.redis.JedisPoolUtil;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class UserJedisUtil {

    private static final String USER_ID = "USER_ID:";
    private static final String USER_JOB_NUM = "USER_JOB_NUM:";
    private static final String USER_STATUS_ACTIVITY = "USER_STATUS_ACTIVITY";
    private static final String USER_STATUS_INACTIVITY = "USER_STATUS_INACTIVITY";
    private static final String USER_NAME_LIKE = "USER_NAME_LIKE:";
    private static final String USER_PROVINCE = "USER_PROVINCE:";
    private static final String USER_CITY = "USER_CITY:";
    private static final String USER_ROLE_NUM = "USER_ROLE_NUM:";
    private static final String USER_TOKEN_ID = "USER_TOKEN_ID:";

    private static JedisPoolUtil jedisPoolUtil = new JedisPoolUtil("49.234.210.89", 6379, "rk123321");
    private static Jedis jedis = jedisPoolUtil.borrowJedis();

    private static UserJedisUtil userJedisUtil;

    @Autowired
    private UserMapper userMapper;


    // 静态块
    static {

    }


    /**
     * @ PostConstruct该注解被用来修饰一个非静态的void（）方法。被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。PostConstruct在构造函数之后执行，init（）方法之前执行。
     * 通常我们会是在Spring框架中使用到@PostConstruct注解 该注解的方法在整个Bean初始化中的执行顺序：  Constructor(构造方法) -> @Autowired(依赖注入) -> @PostConstruct(注释的方法)
     */
    @PostConstruct
    public void init() {
        userJedisUtil = this;
        userJedisUtil.userMapper = this.userMapper;
//        initializationRoleUser("R1001");
//        initializationRoleUser("R1002");
//        initializationRoleUser("R1003");
//        initializationRoleUser("R1004");
//        initializationRoleUser("R1005");
//        initializationUser();

    }

    /**
     * 初始化信息（角色）
     */
    public static void initializationRoleUser(String role) {
        jedis.select(1);
        //角色初始化
        List<User> users = userJedisUtil.userMapper.selectUserByRole(role);
        for (User user : users) {
            jedis.sadd(USER_ROLE_NUM + role, user.getId() + "");
        }
    }

    /**
     * 初始化信息（姓名/省份/城市/状态）
     */
    private static void initializationUser() {
        jedis.select(1);
        List<User> users = userJedisUtil.userMapper.allUser();
        for (User user : users) {
            // 姓名
            char[] chars = user.getUserName().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                jedis.sadd(USER_NAME_LIKE + chars[i], user.getId() + "");
            }
            // 省份
            jedis.sadd(USER_PROVINCE + user.getProvince(), user.getId() + "");
            // 城市
            jedis.sadd(USER_CITY + user.getCity(), user.getId() + "");
            // 状态
            if (user.getStatus() == 0) {
                jedis.sadd(USER_STATUS_INACTIVITY, user.getId() + "");
            }
            if (user.getStatus() == 1) {
                jedis.sadd(USER_STATUS_ACTIVITY, user.getId() + "");
            }
        }
    }

    /**
     * 添加用户,并将各种用户信息字段添加到 redis 中
     *
     * @param user
     */
    public static void addUser(User user) {
        jedis.select(1);
        try {
            // id : user
            jedis.set(USER_ID + user.getId(), JSON.toJSONString(user));
            // 工号 ： user
            jedis.set(USER_JOB_NUM + user.getJobNum(), user.getId() + "");
            // 状态 set
            jedis.sadd(USER_STATUS_ACTIVITY, user.getId() + "");
            // 姓名 ： 拆分到每个like中
            char[] nameChar = user.getUserName().toCharArray();
            for (int i = 0; i < nameChar.length; i++) {
                jedis.sadd(USER_NAME_LIKE + nameChar[i], user.getId() + "");

            }
            // 省份 : set
            jedis.sadd(USER_PROVINCE + user.getProvince(), user.getId() + "");
            // 城市 ： set
            jedis.sadd(USER_CITY + user.getCity(), user.getId() + "");
            // 角色 ：set
            jedis.sadd(USER_ROLE_NUM + user.getRoleNum(), user.getId() + "");
        } catch (Exception e) {
            throw new RuntimeException("添加用户失败");
        }
    }

    /**
     * 更新 user
     *
     * @param
     */
    public static void updateUser(User user) {
        jedis.select(1);
        // 更新 id
        jedis.set(USER_ID + user.getId(), JSON.toJSONString(user));
    }

    /**
     * 更新 nameLike
     *
     * @param oldUser
     * @param newUser
     */
    public static void updateUserName(User oldUser, User newUser) {
        jedis.select(1);
        char[] nameChar = oldUser.getUserName().toCharArray();
        // 删除
        for (int i = 0; i < nameChar.length; i++) {
            jedis.srem(USER_NAME_LIKE + nameChar[i], oldUser.getId() + "");
        }
        char[] chars = newUser.getUserName().toCharArray();
        // 新增
        for (int i = 0; i < chars.length; i++) {
            jedis.sadd(USER_NAME_LIKE + chars[i], newUser.getId() + "");
        }
    }

    /**
     * 更新省份
     *
     * @param oldUser
     * @param newUser
     */
    public static void updateUserProvince(User oldUser, User newUser) {
        jedis.select(1);
        jedis.srem(USER_PROVINCE + oldUser.getProvince(), oldUser.getId() + "");
        jedis.sadd(USER_PROVINCE + newUser.getProvince(), newUser.getId() + "");
    }

    /**
     * 更新城市
     *
     * @param oldUser
     * @param newUser
     */
    public static void updateUserCity(User oldUser, User newUser) {
        jedis.select(1);
        jedis.srem(USER_CITY + oldUser.getCity(), oldUser.getId() + "");
        jedis.sadd(USER_CITY + newUser.getCity(), newUser.getId() + "");
    }

    /**
     * 更新角色信息
     *
     * @param oldUser
     * @param newUser
     */
    public static void updateUserRole(User oldUser, User newUser) {
        jedis.select(1);
        jedis.srem(USER_ROLE_NUM + oldUser.getRoleNum(), oldUser.getId() + "");
        jedis.sadd(USER_ROLE_NUM + newUser.getRoleNum(), newUser.getId() + "");
    }

    /**
     * 添加 User 到缓存中
     *
     * @param user
     */
    public static void addUserById(User user) {
        jedis.select(1);
        jedis.set(USER_ID + user.getId(), JSON.toJSONString(user));
    }


    /**
     * 根据 id 获取用户信息
     *
     * @param id
     * @return
     */
    public static User getUserById(Integer id) {
        jedis.select(1);
        try {
            User user = JSON.parseObject(jedis.get(USER_ID + id), User.class);
            if (user == null) {
                user = userJedisUtil.userMapper.selectUserById(id);
                if (user != null) {
                    UserJedisUtil.addUser(user);
                }
                return user;
            }
            return user;
        } catch (Exception e) {
            throw new RuntimeException("查询错误！");
        }
    }

    /**
     * 通过 jobNum 获取用户信息
     *
     * @param jobNum
     * @return
     */
    public static Integer getUserByJobNum(Integer jobNum) {
        jedis.select(1);
        try {
            String id = jedis.get(USER_JOB_NUM + jobNum);
//            if (StringUtils.isEmpty(id)) {
//                User user = userJedisUtil.userMapper.selectUserByJobNum(Integer.valueOf(id));
//                if (user != null) {
//                    UserJedisUtil.addUser(user);
//                    return user;
//                } else {
//                    return null;
//                }
//            }
//            return getUserById(Integer.valueOf(id));
            return Integer.valueOf(id);
        } catch (Exception e) {
            throw new RuntimeException("查询错误！");
        }
    }

    /**
     * 保存 token ,设定过期时间
     *
     * @param id      id
     * @param token   token
     * @param timeOut 过期时间
     */
    public static void saveUserToken(Integer id, String token, Integer timeOut) {
        jedis.select(1);
        try {
            jedis.set(USER_TOKEN_ID + token, String.valueOf(id));
            jedis.expire(USER_TOKEN_ID + token, timeOut);
        } catch (Exception e) {
            throw new RuntimeException("查询错误！");
        }
    }


    /**
     * 根据 token 获取用户 id
     *
     * @param token
     * @return
     */
    public static Integer getUserIdByToken(String token) {
        jedis.select(1);
        try {
            String id = jedis.get(USER_TOKEN_ID + token);
            if (StringUtils.isEmpty(id)) {
                throw new RuntimeException("登陆失效，请重新登陆！");
            }
            return Integer.valueOf(id);
        } catch (Exception e) {
            throw new RuntimeException("查询错误！");
        }
    }

    /**
     * 根据 token 获取用户信息
     *
     * @param token
     * @return
     */
    public static User getUserByToken(String token) {
        jedis.select(1);
        try {
            String id = jedis.get(USER_TOKEN_ID + token);
            if (StringUtils.isEmpty(id)) {
                throw new RuntimeException("登陆失效，请重新登陆！");
            }
            return getUserById(Integer.valueOf(id));
        } catch (Exception e) {
            throw new RuntimeException("查询错误！");
        }
    }

    /**
     * 根据 role 获取用户集合
     *
     * @param role
     * @return
     */
    public static List<User> getUserByRole(String role) {
        jedis.select(1);
        List<User> users = new ArrayList<>();
        Set<String> list = jedis.smembers(USER_ROLE_NUM + role);
        for (String str : list) {
            User user = JSON.parseObject(str, User.class);
            List<Integer> list2 = new ArrayList<>();
            List<String> list1 = Arrays.asList(user.getBeProvince().split("-"));
            for (String s : list1) {
                list2.add(Integer.valueOf(s));
            }
            user.setBeProvinces(list2);
            users.add(user);
        }
        return users;
    }

    /**
     * 根据角色&省份 交集的用户信息
     *
     * @param role
     * @param province
     * @return
     */
    public static List<User> getUserByRoleAndProvince(String role, Province province) {
        jedis.select(1);
        Set<String> ids = jedis.sinter(USER_ROLE_NUM + role, USER_PROVINCE + province.getProvince());
        List<User> users = new ArrayList<>();
        for (String id : ids) {
            users.add(getUserById(Integer.valueOf(id)));
        }
        return users;
    }

    /**
     * 根据角色&省份s 交集的用户信息
     *
     * @param role
     * @param provinces
     * @return
     */
    public static List<User> getUserByRoleAndProvinces(String role, List<Province> provinces) {
        jedis.select(1);
        List<User> users = new ArrayList<>();
        for (Province province : provinces) {
            Set<String> ids = jedis.sinter(USER_ROLE_NUM + role, USER_PROVINCE + province.getProvince());
            for (String id : ids) {
                users.add(getUserById(Integer.valueOf(id)));
            }
        }
        return users;
    }

    /**
     * 根据用户获取角色信息
     *
     * @param user
     * @return
     */
    public static List<Role> getRolesByType(User user) {
        jedis.select(1);
        Set<String> s = jedis.smembers(USER_ROLE_NUM + "OBJECT");
        List<Role> rolesAdmin = new ArrayList<>();
        List<Role> roles = new ArrayList<>();
        if (CollectionUtils.isEmpty(s)) {
            roles = userJedisUtil.userMapper.getRoles("admin");
            for (Role role : roles) {
                jedis.sadd(USER_ROLE_NUM + "OBJECT", JSON.toJSONString(role));
            }
        } else {
            for (String s1 : s) {
                Role role = JSON.parseObject(s1, Role.class);
                if (role.getRoleNum().equals("R1001")) {
                    rolesAdmin.add(role);
                } else {
                    rolesAdmin.add(role);
                    roles.add(role);
                }
            }
        }
        if (user != null && user.getRoleNum().indexOf("R1001") != -1) {
            return rolesAdmin;
        } else {
            return roles;
        }
    }

    /**
     * 检查工号是否存在
     *
     * @param jobNum
     * @return
     */
    public static boolean checkJobNum(Integer jobNum) {
        jedis.select(1);
        return jedis.exists(USER_JOB_NUM + jobNum);
    }


}
