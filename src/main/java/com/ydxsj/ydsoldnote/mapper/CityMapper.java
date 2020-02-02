package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.data.City;
import com.ydxsj.ydsoldnote.bean.data.Province;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CityMapper {

    /**
     * 获取全部省份
     * @return
     */
    List<Province> getProvince();

    /**
     * 获取全部市
     * @return
     */
    List<City> getCitys();

    /**
     * 使用省份id获取省份名称
     * @param provincesIds
     * @return
     */
    List<String> getProvinceById(@Param("provincesIds") List<String> provincesIds);

    /**
     * 根据省份id获取省份对象集合
     * @param provincesIds
     * @return
     */
    List<Province> getProvinceByIds(@Param("provincesIds") List<String> provincesIds);



    /**
     * 根据省份id 获取全部市
     * @param id
     * @return
     */
    List<City> getCitysByProvinceId(@Param("id") Integer id);
}
