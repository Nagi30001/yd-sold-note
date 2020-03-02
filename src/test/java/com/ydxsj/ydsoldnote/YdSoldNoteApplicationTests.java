package com.ydxsj.ydsoldnote;

import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.RoleMapper;
import com.ydxsj.ydsoldnote.mapper.UserMapper;
import com.ydxsj.ydsoldnote.mapper.UserTokenMapper;
import com.ydxsj.ydsoldnote.service.UserService;
import com.ydxsj.ydsoldnote.util.JedisUtil.JedisUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.UserJedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YdSoldNoteApplicationTests {

    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Test
    public void contextLoads() {

    }

    @Test
    public void test1() {
        List<User> usersByType = userService.getUsersByType("c875306d309c6f422c315e70ef02c950", "yd");
        System.err.println(usersByType);

    }
}
