package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.City;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.role.Role;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.bean.user.UserToken;
import com.ydxsj.ydsoldnote.config.shiro.TokenGenerator;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import com.ydxsj.ydsoldnote.service.UserService;
import com.ydxsj.ydsoldnote.service.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private UserUtil userUtil;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    //12小时后过期
    private final static int EXPIRE = 3600 * 12 * 1000;

    public User getUserByJobName(String jobName) {
        User user = userMapper.selectUserByJobName(jobName);
        return user;
    }

    @Override
    public User getUserByToken(String token) {
        System.err.println("token" + token);
        Integer userId = userTokenMapper.getUserIdByToken(token);
        User user = userMapper.selectUserById(userId);
        return user;
    }

    @Override
    public List<Province> getCitys(User user) {
        // 获取用户省份代码
        List<String> provinceIds = Arrays.asList(user.getBeProvince().split("-"));
        // 根据省份id获取名称
        List<Province> provinces = cityMapper.getProvinceByIds(provinceIds);
        // 根据省份id获取市对象
        for (Province province : provinces) {
            List<City> cities = cityMapper.getCitysByProvinceId(province.getId());
            province.setCities(cities);
        }
        return provinces;
    }

    @Override
    public List<User> getUserByRole(String role) {
        List<User> users = userMapper.selectUserByRole(role);
        return users;
    }


    @Override
    public List<String> getUserInfoByToken(String token) {
        //获取用户
        User user = userMapper.selectUserById(userTokenMapper.getUserIdByToken(token));
        List<String> roles = Arrays.asList(user.getRoleNum().split(","));
        return roles;
    }

    @Override
    public List<User> getUsersByType(String token, String type) {
        List<String> provinces = userUtil.getProvinceByToken(token);
        if (type.equals("yd")) {
            List<User> users = userMapper.getUsersByProvince(provinces, type);
            for (User user : users) {
                user.setUser(userMapper.selectUserById(user.getCreateUser()));
                List<String> p = Arrays.asList(user.getBeProvince().split("-"));
                List<Integer> s = new ArrayList<>();
                for (String str : p) {
                    s.add(Integer.parseInt(str));
                }
                user.setBeProvinces(s);
                if (!StringUtils.isEmpty(user.getCreateTime())) {
                    user.setCreateTime(sdf.format(new Date(Long.parseLong(user.getCreateTime()))));
                }
                user.setRoles(Arrays.asList(user.getRoleNum().split(",")));
            }
            System.err.println(users);
            return users;
        } else if (type.equals("pt")) {
            List<User> users = userMapper.getUsersByProvince(provinces, type);
            for (User user : users) {
                user.setUser(userMapper.selectUserById(user.getCreateUser()));
                List<String> p = Arrays.asList(user.getBeProvince().split("-"));
                List<Integer> s = new ArrayList<>();
                for (String str : p) {
                    s.add(Integer.parseInt(str));
                }
                user.setBeProvinces(s);
                if (!StringUtils.isEmpty(user.getCreateTime())) {
                    user.setCreateTime(sdf.format(new Date(Long.parseLong(user.getCreateTime()))));
                }
                user.setRoles(Arrays.asList(user.getRoleNum().split(",")));
            }
            return users;
        } else {
            return null;
        }
    }

    @Override
    public List<Role> getRolesBy(User user) {
        if (user != null && user.getRoleNum().indexOf("R1001") != -1) {
            List<Role> roles = userMapper.getRoles("admin");
            return roles;
        } else {
            List<Role> roles = userMapper.getRoles("common");
            return roles;
        }
    }

    @Override
    public User addUser(String token, Map userMap) {
        User createUser = userUtil.getUserByToken(token);
        String jobNum = (String) userMap.get("jobNum");
        String jobPassword = (String) userMap.get("jobPassword");
        String userName = (String) userMap.get("userName");
        String phone = (String) userMap.get("phone");
        String province = (String) userMap.get("province");
        String city = (String) userMap.get("city");
        String roleNum = (String) userMap.get("roleNum");
        String address = (String) userMap.get("address");
        List<String> beProvince = (List<String>) userMap.get("beProvince");
        if (StringUtils.isEmpty(jobNum) && StringUtils.isEmpty(jobPassword) && StringUtils.isEmpty(userName) &&
                StringUtils.isEmpty(phone) && StringUtils.isEmpty(province) && StringUtils.isEmpty(city) &&
                StringUtils.isEmpty(city) && StringUtils.isEmpty(roleNum) && StringUtils.isEmpty(address) && beProvince.size() < 1) {
            return null;
        }
        User user = new User();
        //检查工号是否被占用
        Integer row = userMapper.selectUserByJobNum(Integer.parseInt(jobNum));
        System.err.println(row);
        if (row > 0) {
            return null;
        }
        user.setJobNum(Integer.valueOf(jobNum));
        user.setJobPassword(String.valueOf(userMap.get("jobPassword")));
        user.setUserName(userName.trim());
        user.setPhone(phone.trim());
        user.setProvince(province.trim());
        user.setCity(city.trim());
        user.setRoleNum(roleNum.trim());
        user.setAddress(address);
        user.setBeProvince(StringUtils.join(beProvince, "-"));
        user.setCreateUser(createUser.getId());
        user.setCreateTime(String.valueOf(System.currentTimeMillis()));
        user.setStatus(1);

        Integer i = userMapper.insertUser(user);
        if (i < 0) {
            return null;
        }
        user.setCreateTime(sdf.format(new Date(Long.parseLong(user.getCreateTime()))));
        return user;
    }

    @Override
    public boolean updateUser(String token, Map userMap) {
        System.err.println(token);
        System.err.println(userMap);
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        Boolean status1 = (Boolean) userMap.get("status1");
        String jobNum = String.valueOf(userMap.get("jobNum"));
        String jobPassword = (String) userMap.get("jobPassword");
        String userName = (String) userMap.get("userName");
        String phone = (String) userMap.get("phone");
        String province = (String) userMap.get("province");
        String city = (String) userMap.get("city");
        String roleNum = (String) userMap.get("roleNum");
        String address = (String) userMap.get("address");
        List<String> beProvince = (List<String>) userMap.get("beProvince");
        if (StringUtils.isEmpty(jobNum) && StringUtils.isEmpty(jobPassword) && StringUtils.isEmpty(userName) &&
                StringUtils.isEmpty(phone) && StringUtils.isEmpty(province) && StringUtils.isEmpty(city) &&
                StringUtils.isEmpty(city) && StringUtils.isEmpty(roleNum) && StringUtils.isEmpty(address) && beProvince.size() < 1) {
            return false;
        }
        User user = new User();
        //检查工号是否存在
        Integer row = userMapper.selectUserByJobNum(Integer.parseInt(jobNum));
        if (row != 1) {
            return false;
        }
        user.setJobNum(Integer.valueOf(jobNum));
        user.setJobPassword(String.valueOf(userMap.get("jobPassword")));
        user.setUserName(userName.trim());
        user.setPhone(phone.trim());
        user.setProvince(province.trim());
        user.setCity(city.trim());
        user.setRoleNum(roleNum.trim());
        user.setAddress(address);
        user.setBeProvince(StringUtils.join(beProvince, "-"));
        user.setCreateTime(String.valueOf(System.currentTimeMillis()));
        if (status1) {
            user.setStatus(1);
        } else {
            user.setStatus(0);
        }
        Integer i = userMapper.updateUser(user);
        if (i < 1) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public List<User> getPlatformsByProvince(List<Province> provinces) {
        List<User> users = userMapper.getPlatformsByProvince(provinces);
        return users;
    }

    @Override
    public boolean checkJobNum(String value) {

        Integer row = userMapper.selectUserByJobNum(Integer.parseInt(value));
        if (row > 0) {
            return false;
        }
        return true;
    }


    @Override
    public User getUserById(Integer userId) {
        User user = userMapper.selectUserById(userId);
        return user;
    }

    @Override
    public UserToken saveToken(Integer userId) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        //生成一个token
        userToken.setToken(TokenGenerator.generateValue());

        Long nowTime = System.currentTimeMillis();
        //过期时间
        userToken.setExpireTime(nowTime + EXPIRE);
        //更新时间
        userToken.setUpdateTime(nowTime);

//       查询token表中是否有该id的token信息
        UserToken userToken1 = userTokenMapper.selectTokenById(userId);
//       没有
        if (userToken1 == null) {
            // 将token信息插入数据库
            userTokenMapper.insertToken(userToken);
            return userToken;
        }
//        有的话就是更新
        userTokenMapper.updateUserToken(userToken);
        return userToken;

    }

    @Override
    public UserToken queryByToken(String token) {
        if (token == null || "".equals(token)) {
            return null;
        }
        UserToken userToken = userTokenMapper.selectTokenByToken(token);
        return userToken;
    }


}
