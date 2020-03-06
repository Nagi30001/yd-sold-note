package com.ydxsj.ydsoldnote;

import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.data.Channel;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.*;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import com.ydxsj.ydsoldnote.service.SellReceiptsService;
import com.ydxsj.ydsoldnote.service.UserService;
import com.ydxsj.ydsoldnote.util.JedisUtil.JedisUtil;
import com.ydxsj.ydsoldnote.util.JedisUtil.SellReceiptsJedisUtil;
import com.ydxsj.ydsoldnote.util.PublicUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.util.*;

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
    private DataManagementMapper dataManagementMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private SellReceiptsService sellReceiptsService;
    @Autowired
    private DataManagementService dataManagementService;

    @Test
    public void contextLoads() {
//        Map<String,String> map = new HashMap<>();
//        map.put("type","DT");
//        map.put("userId","1");
//        map.put("sellName","");
//        map.put("sellType","");
//        map.put("channel","");
//        map.put("TPId","");
//        map.put("clientName","");
//        map.put("clientCarNum","");
//        map.put("startingDate","2020-2-1");
//        map.put("endDay","2020-3-4");
//        map.put("status","");
//        map.put("page","0");
//        map.put("count","10");
//        map.put("checkTimeType","安装时间");
//        try {
//            List<CarReceipts> carReceiptsList = sellReceiptsService.searchQueryDate(map);
//            for (CarReceipts carReceipts : carReceiptsList){
//                System.err.println(carReceipts);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

    }

    @Test
    public void test1() {
//        List<Channel> channelMsgs = dataManagementService.getChannelMsgs("2e022d551986ea59f53f253de7d61b8a");
//        System.err.println(channelMsgs);

    }
}
