package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.role.Role;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.config.shiro.TokenGenerator;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import com.ydxsj.ydsoldnote.service.UserService;
import com.ydxsj.ydsoldnote.service.UserUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.CityJedisUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.UserJedisUtil;
import com.ydxsj.ydsoldnote.util.PublicUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.net.www.ParseUtil;

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
    // 一周后后过期
    private final static int EXPIRE = 3600 * 168 * 1000;





    public User getUserByJobNum(String jobNum) throws RuntimeException {
        Integer id  = UserJedisUtil.getUserByJobNum(Integer.valueOf(jobNum));
        if (id == null){
            User user = userMapper.selectUserByJobNum(Integer.valueOf(jobNum));
            if(user != null){
                UserJedisUtil.addUser(user);
            }
            return user;
        } else {
            User user =  getUserById(id);
            return user;
        }
    }

    @Override
    public User getUserByToken(String token) throws RuntimeException {
        Integer id = UserJedisUtil.getUserIdByToken(token);
        User user = getUserById(id);;
        return user;
    }

    @Override
    public List<Province> getCitys(User user) {
        // 获取用户省份代码
        List<String> list = Arrays.asList(user.getBeProvince().split("-"));
        Set<Integer> provinceIds = new HashSet<>();
        for (String str : list){
            provinceIds.add(Integer.valueOf(str));
        }
        List<Province> provinces = CityJedisUtil.getProvinceByIds(provinceIds);
//        // 根据省份id获取名称
//        List<Province> provinces = cityMapper.getProvinceByIds(provinceIds);
//        // 根据省份id获取市对象
//        for (Province province : provinces) {
//            List<City> cities = cityMapper.getCitysByProvinceId(province.getId());
//            province.setCities(cities);
//        }
        return provinces;
    }

    @Override
    public List<User> getUserByRole(String role) {
        List<User> users = UserJedisUtil.getUserByRole(role);
//        List<User> users = userMapper.selectUserByRole(role);
        return users;
    }

    @Override
    public List<String> getUserInfoByToken(String token)throws RuntimeException {
        //获取用户
        Integer id = UserJedisUtil.getUserIdByToken(token);
        User user = UserJedisUtil.getUserById(id);
        List<String> roles = Arrays.asList(user.getRoleNum().split(","));
        return roles;
    }

    @Override
    public List<User> getUsersByType(String token, String type) throws RuntimeException {
        List<User> userList = new ArrayList<>();
        Integer id  = UserJedisUtil.getUserIdByToken(token);
        if (id == null){
            throw  new RuntimeException("登陆失效");
        }
        User user = UserJedisUtil.getUserById(id);
        if (user == null){
            user = getUserById(id);
        }
        Set<Integer> ids = getProvincesByUser(user);
        List<Province> list = CityJedisUtil.getProvinceByIds(ids);
//        List<String> provinces = userUtil.getProvinceByUser(user);
        if (type.equals("yd")) {
            // 获取除 R1004 的用户
            List<User> r1001 = UserJedisUtil.getUserByRoleAndProvinces("R1001",list);
            List<User> r1002 = UserJedisUtil.getUserByRoleAndProvinces("R1002",list);
            List<User> r1003 = UserJedisUtil.getUserByRoleAndProvinces("R1003",list);
            List<User> r1005 = UserJedisUtil.getUserByRoleAndProvinces("R1005",list);
            userList = (List<User>) CollectionUtils.union(r1001,r1002);
            userList = (List<User>) CollectionUtils.union(userList,r1003);
            userList = (List<User>) CollectionUtils.union(userList,r1005);

//            List<User> users = userMapper.getUsersByProvince(provinces, type);
//            for (User user : users) {
//                user.setUser(userMapper.selectUserById(user.getCreateUser()));
//                List<String> p = Arrays.asList(user.getBeProvince().split("-"));
//                List<Integer> s = new ArrayList<>();
//                for (String str : p) {
//                    s.add(Integer.parseInt(str));
//                }
//                user.setBeProvinces(s);
//                if (!StringUtils.isEmpty(user.getCreateTime())) {
//                    user.setCreateTime(sdf.format(new Date(Long.parseLong(user.getCreateTime()))));
//                }
//                user.setRoles(Arrays.asList(user.getRoleNum().split(",")));
//            }
        } else if (type.equals("pt")) {
            userList = UserJedisUtil.getUserByRoleAndProvinces("R1004",list);
//            List<User> users = userMapper.getUsersByProvince(provinces, type);
//            for (User user : users) {
//                user.setUser(userMapper.selectUserById(user.getCreateUser()));
//                List<String> p = Arrays.asList(user.getBeProvince().split("-"));
//                List<Integer> s = new ArrayList<>();
//                for (String str : p) {
//                    s.add(Integer.parseInt(str));
//                }
//                user.setBeProvinces(s);
//                if (!StringUtils.isEmpty(user.getCreateTime())) {
//                    user.setCreateTime(sdf.format(new Date(Long.parseLong(user.getCreateTime()))));
//                }
//                user.setRoles(Arrays.asList(user.getRoleNum().split(",")));
//            }
        } else {
            return null;
        }

        for (User user1 : userList){
            user1.setCreateTime(PublicUtil.timestampToString(user1.getCreateTime(),PublicUtil.SDF_YYYY_MM_DD));
            user1.setBeProvinces((new ArrayList<>(CityJedisUtil.getUserProvinceIds(user1))));
            if (user1.getCreateUser() != null){
                user1.setUser(UserJedisUtil.getUserById(user1.getCreateUser()));
            }
        }
        return userList;
    }

    @Override
    public List<Role> getRolesBy(User user) {
       return UserJedisUtil.getRolesByType(user);
//        if (user != null && user.getRoleNum().indexOf("R1001") != -1) {
//            List<Role> roles = userMapper.getRoles("admin");
//            return roles;
//        } else {
//            List<Role> roles = userMapper.getRoles("common");
//            return roles;
//        }
    }

    @Override
    public User addUser(String token, Map userMap)throws RuntimeException {
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
        Integer row = userMapper.selectUserCountByJobNum(Integer.parseInt(jobNum));
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
        UserJedisUtil.addUser(user);
        return user;
    }

    @Override
    public boolean updateUser(String token, Map userMap) throws RuntimeException {

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
//        Integer row = userMapper.selectUserCountByJobNum(Integer.parseInt(jobNum));
        Integer id = UserJedisUtil.getUserByJobNum(Integer.valueOf(jobNum));
        User userByJobNum = UserJedisUtil.getUserById(id);
        if (userByJobNum == null) {
            return false;
        }
        user.setId(userByJobNum.getId());
        user.setJobNum(Integer.valueOf(jobNum));
        user.setJobPassword(String.valueOf(userMap.get("jobPassword")));
        user.setUserName(userName.trim());
        user.setPhone(phone.trim());
        user.setProvince(province.trim());
        user.setCity(city.trim());
        user.setRoleNum(roleNum.trim());
        user.setAddress(address);
        user.setBeProvince(StringUtils.join(beProvince, "-"));
        user.setCreateTime(userByJobNum.getCreateTime());
        if (status1) {
            user.setStatus(1);
        } else {
            user.setStatus(0);
        }

        Integer i = userMapper.updateUser(user);
        if (i < 1) {
            return false;
        } else {
            // 更新数据
            UserJedisUtil.updateUser(user);
            // 判断与旧数据相比 有哪些不同
            if (!userByJobNum.getUserName().equals(user.getUserName())){
                UserJedisUtil.updateUserName(userByJobNum,user);
            }
            if (!userByJobNum.getProvince().equals(user.getProvince())){
                UserJedisUtil.updateUserProvince(userByJobNum,user);
            }
            if (!userByJobNum.getCity().equals(user.getCity())){
                UserJedisUtil.updateUserCity(userByJobNum,user);
            }
            if (!userByJobNum.getRoleNum().equals(user.getRoleNum())){
                UserJedisUtil.updateUserRole(userByJobNum,user);
            }
            return true;
        }

    }

    @Override
    public List<User> getPlatformsByProvince(List<Province> provinces) {
        List<User> users1 = UserJedisUtil.getUserByRoleAndProvinces("R1004",provinces);
        List<User> users2 = UserJedisUtil.getUserByRoleAndProvinces("R1005",provinces);
        users1 = (List<User>) CollectionUtils.union(users1,users2);
//        List<User> users = userMapper.getPlatformsByProvince(provinces);
        return users1;
    }

    @Override
    @Transactional
    public void updatePassword(Map map) {
        String oldPassword = String.valueOf(map.get("oldPassword"));
        String newPassword = String.valueOf(map.get("newPassword"));
        String id = String.valueOf(map.get("id"));
        if (oldPassword.equals(newPassword))

        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword) || StringUtils.isEmpty(id)){
            throw new RuntimeException("数据请求错误!");
        }
        // 获取用户信息
//        User user = userMapper.selectUserById(Integer.valueOf(id));
        User user = UserJedisUtil.getUserById(Integer.valueOf(id));
        if (user == null || !oldPassword.equals(user.getJobPassword())){
            throw new RuntimeException("原密码不正确");
        } else {
            User user1 = new User();
            user1.setId(Integer.valueOf(id));
            user1.setJobPassword(newPassword);
            Integer row = userMapper.updateUserMsg(user1);
            if (row != 1){
                throw new RuntimeException("数据请求错误!");
            }
            user.setJobPassword(newPassword);
            UserJedisUtil.updateUser(user);
        }



    }

    @Override
    public boolean checkJobNum(String value) {
        return UserJedisUtil.checkJobNum(Integer.valueOf(value));
//        Integer row = userMapper.selectUserCountByJobNum(Integer.parseInt(value));
//        if (row > 0) {
//            return false;
//        }
//        return true;
    }


    @Override
    public String saveToken(Integer userId) {
//        UserToken userToken = new UserToken();
//        userToken.setUserId(userId);
        //生成一个token
//        userToken.setToken(TokenGenerator.generateValue());

//        Long nowTime = System.currentTimeMillis();
        //过期时间
//        userToken.setExpireTime(nowTime + EXPIRE);
        //更新时间
//        userToken.setUpdateTime(nowTime);

//       查询token表中是否有该id的token信息
//        UserToken userToken1 = userTokenMapper.selectTokenById(userId);
//        String token = UserJedisUtil.getUserTokenById(userId);
//       没有
//        if (StringUtils.isEmpty(token)) {
            // 将 token 放到 redis 中
        String token = TokenGenerator.generateValue();
        UserJedisUtil.saveUserToken(userId,token,EXPIRE);
//            userTokenMapper.insertToken(userToken);
//            return userToken;
//        }
//        有的话就是更新
//        userTokenMapper.updateUserToken(userToken);
        return token;

    }

    @Override
    public Integer queryByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
//        UserToken userToken = userTokenMapper.selectTokenByToken(token);
        Integer id = UserJedisUtil.getUserIdByToken(token);
        return id;
    }

    @Override
    public User getUserById(Integer id) throws RuntimeException {
        User user = UserJedisUtil.getUserById(id);
        if (user != null){
            return user;
        } else {
            user = userMapper.selectUserById(id);
            if (user != null){
                UserJedisUtil.addUser(user);
            }
            return user;
        }
    }

    /**
     * 获取用户信息的 ProvinceIds 集合
     * @param user
     * @return
     */
    public Set<Integer> getProvincesByUser(User user){
        List<String> list = Arrays.asList(user.getBeProvince().split("-"));
        Set<Integer> ids = new HashSet<>();
        for( String s : list){
            ids.add(Integer.valueOf(s));
        }
        return ids;
    }


}
