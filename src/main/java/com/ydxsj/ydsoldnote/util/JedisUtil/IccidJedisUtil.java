package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.data.Iccid;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class IccidJedisUtil {

    private static final String ICCID_ID = "ICCID_ID:";
    private static final String ICCID_TOP_19 = "ICCID_TOP_19:";
    private static final String ICCID_ACTIVITY = "ICCID_ACTIVITY";
    private static final String ICCID_INACTIVITY = "ICCID_INACTIVITY";
    private static IccidJedisUtil iccidJedisUtil;
    @Autowired
    private DataManagementMapper dataManagementMapper;




    @PostConstruct
    public void init() {
        iccidJedisUtil = this;
        iccidJedisUtil.dataManagementMapper = this.dataManagementMapper;
        initialization();
    }

    // 初始化信息
    public void initialization() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            List<Iccid> iccids = iccidJedisUtil.dataManagementMapper.getAllIccid();
            for (Iccid iccid : iccids) {
                addIccid(iccid);
            }
        } catch (Exception e) {
            throw new RuntimeException("ICCID 初始化失败！");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void addIccid(Iccid iccid){
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            jedis.set(ICCID_ID + iccid.getId(), JSON.toJSONString(iccid));
            jedis.set(ICCID_TOP_19 + getIccidTop19(iccid.getIccid()), iccid.getId() + "");
            if (iccid.getStatus() == 0) {
                jedis.sadd(ICCID_INACTIVITY, iccid.getId() + "");
            } else if (iccid.getStatus() == 1) {
                jedis.sadd(ICCID_ACTIVITY, iccid.getId() + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static String getIccidTop19(String iccid) {
        StringBuilder newIccid = new StringBuilder();
        char[] chars = iccid.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            newIccid.append(chars[i]);
        }
        return String.valueOf(newIccid);
    }



    /**
     * 检查iccid是否可用
     *
     * @param iccid
     * @return
     */
    public static boolean checkIccid(String iccid) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            String s = jedis.get(ICCID_TOP_19 + iccid);
            if (StringUtils.isEmpty(s)) {
                return false;
            }
            Iccid iccid1 = JSON.parseObject(jedis.get(ICCID_ID + s), Iccid.class);
            if (iccid1.getStatus() == 1){
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 根据状态获取iccid集合
     *
     * @param status
     * @return
     */
    public static List<Iccid> getIccidsByStatus(int status) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            List<Iccid> iccids = new ArrayList<>();
            Set<String> s = new HashSet<>();
            if (status == 0) {
                s = jedis.smembers(ICCID_INACTIVITY);
            } else {
                s = jedis.smembers(ICCID_ACTIVITY);
            }
            for (String str : s) {
                iccids.add(getIccidById(str));
            }
            return iccids;
        } catch (Exception e) {
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static Iccid getIccidById(String id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            return JSON.parseObject(jedis.get(ICCID_ID + id), Iccid.class);
        } catch (Exception e) {
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static Iccid getIccidByTop19(String iccid) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            return getIccidById(jedis.get(ICCID_TOP_19 + iccid));
        } catch (Exception e) {
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    /**
     * 更新iccid
     * @param iccid1
     */
    public static void updateIccid(Iccid iccid1) {
        deleteIccidStatus(iccid1);
        addIccid(iccid1);

    }

    public static void addIccidStatus(Iccid iccid){
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            if (iccid.getStatus() == 0){
                jedis.sadd(ICCID_ACTIVITY,iccid.getId()+"");
            } else if (iccid.getStatus() == 1){
                jedis.sadd(ICCID_INACTIVITY,iccid.getId()+"");
            }
        } catch (Exception e) {
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

    public static void deleteIccidStatus(Iccid iccid){
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(4);
            if (iccid.getStatus() == 0){
                jedis.srem(ICCID_ACTIVITY,iccid.getId()+"");
            } else if (iccid.getStatus() == 1){
                jedis.srem(ICCID_INACTIVITY,iccid.getId()+"");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ICCID ##");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }
    }

}
