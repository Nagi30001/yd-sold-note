package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.alibaba.fastjson.JSON;
import com.ydxsj.ydsoldnote.bean.data.City;
import com.ydxsj.ydsoldnote.bean.data.Province;
import com.ydxsj.ydsoldnote.bean.user.User;
import com.ydxsj.ydsoldnote.mapper.CityMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CityJedisUtil {


    private static final String PROVINCE = "PROVINCE";
    private static final String CITY = "CITY:";
    private static CityJedisUtil cityJedisUtil;
    @Autowired
    private CityMapper cityMapper;

    static {

    }


    @PostConstruct
    public void init() {
        cityJedisUtil = this;
        cityJedisUtil.cityMapper = this.cityMapper;
        initialization();

    }

    /**
     * 省市数据初始化
     */
    public static void initialization() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(2);
            // 存放全部省对象
            List<Province> provinceList = cityJedisUtil.cityMapper.getProvince();
            for (Province province : provinceList) {
                jedis.sadd(PROVINCE, JSON.toJSONString(province));
                // 存放全部市对象
                List<City> cities = cityJedisUtil.cityMapper.getCitysByProvinceId(province.getId());
                for (City city : cities) {
                    jedis.sadd(CITY + province.getId(), JSON.toJSONString(city));
                    jedis.sadd(CITY + province.getProvince(), JSON.toJSONString(city));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("省市数据初始化失败!  #initialization");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }

    /**
     * 获取全部省份信息
     *
     * @return
     */
    public static List<Province> getAllProvince() {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(2);
            Set<String> list = jedis.smembers(PROVINCE);
            List<Province> provinces = new ArrayList<>();
            for (String str : list) {
                Province province = JSON.parseObject(str, Province.class);
                province.setCities(getCitiesByProvinceId(province.getId()));
                provinces.add(province);
            }
            return provinces;
        } catch (Exception e) {
            throw new RuntimeException("获取全部省份信息失败! #all");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }

    /**
     * 根据省份 id 获取城市信息
     *
     * @param id
     * @return
     */
    public static List<City> getCitiesByProvinceId(Integer id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(2);
            Set<String> list = jedis.smembers(CITY + id);
            List<City> cities = new ArrayList<>();
            for (String str : list) {
                cities.add(JSON.parseObject(str, City.class));
            }
            return cities;
        } catch (Exception e) {
            throw new RuntimeException("获取城市信息失败！ #id");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }

    /**
     * 根据省份 ids 获取城市信息
     *
     * @param ids
     * @return
     */
    public static List<City> getCitiesByProvinceIds(Set<Integer> ids) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(2);
            List<City> cities = new ArrayList<>();
            for (Integer id : ids) {
                Set<String> list = jedis.smembers(CITY + id);
                for (String str : list) {
                    cities.add(JSON.parseObject(str, City.class));
                }
            }
            return cities;
        } catch (Exception e) {
            throw new RuntimeException("获取城市信息失败！ #ids");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }

    /**
     * 根据省份 ids 获取城市信息
     *
     * @param provinces
     * @return
     */
    public static List<City> getCitiesByProvinces(List<Province> provinces) {
        try {
            Set<Integer> ids = new HashSet<>();
            for (Province province : provinces) {
                ids.add(province.getId());
            }
            return getCitiesByProvinceIds(ids);
        } catch (Exception e) {
            throw new RuntimeException("获取城市信息失败！ #ids");
        }

    }

    /**
     * 根据 id 获取省份信息
     *
     * @param id
     * @return
     */
    public static Province getProvinceById(Integer id) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(2);
            List<Province> list = getAllProvince();
            for (Province province : list) {
                if (id.equals(province.getId())) {
                    province.setCities(getCitiesByProvinceId(province.getId()));
                    return province;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取省份信息失败！ #id");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }

    /**
     * 根据 id 集合获取省份信息
     *
     * @param ids
     * @return
     */
    public static List<Province> getProvinceByIds(Set<Integer> ids) {
        Jedis jedis = null;
        try {
            jedis = JedisUtil.jedisPoolUtil.borrowJedis();
            jedis.select(2);
            List<Province> provinces = new ArrayList<>();
            for (Integer id : ids) {
                provinces.add(getProvinceById(id));
            }
            return provinces;
        } catch (Exception e) {
            throw new RuntimeException("获取省份信息失败！ #ids");
        } finally {
            JedisUtil.jedisPoolUtil.returnJedis(jedis);
        }

    }


    /**
     * 将用户的区域权限集合转换成Set<Integer>集合
     *
     * @param user
     * @return
     */
    public static Set<Integer> getUserProvinceIds(User user) {
        Set<Integer> set = new HashSet<>();
        String[] split = user.getBeProvince().split("-");
        for (int i = 0; i < split.length; i++) {
            set.add(Integer.valueOf(split[i]));
        }
        return set;
    }

    /**
     *
     * @param user
     * @return
     */
    public static List<Province> getBeProvincesByUser(User user) {
        Set<Integer> ids = getUserProvinceIds(user);
        List<Province> provinces = getProvinceByIds(ids);
        if (CollectionUtils.isEmpty(provinces)) {
            // no cache
            provinces = cityJedisUtil.cityMapper.getProvinceByIds(ids);
            // 省份缓存初始化
            initialization();
        }
        return provinces;
    }


}
