package com.ydxsj.ydsoldnote.mapper;

import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.CarType;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataManagementMapper {


    /**
     * 获取全部车型
     * @return
     */
    List<CarType> getCarType();

    /**
     * 获取全部销售类型
     * @return
     */
    List<SellType> getSellTypes();

    /**
     * 获取全部附加业务
     * @return
     */
    List<Addition> getAdditions();


    /**
     * 根据销售id获取销售类型信息
     * @param sellType
     * @return
     */
    SellType getSellTypeById(@Param("sellType") Integer sellType);

    /**
     * 根据附加业务id集合获取附加业务对象信息
     * @param additionIds
     * @return
     */
    List<Addition> getAdditionsById(@Param("additionIds") List<Integer> additionIds);
}
