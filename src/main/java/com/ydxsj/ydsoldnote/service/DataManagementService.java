package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.CarType;
import com.ydxsj.ydsoldnote.bean.data.SellType;

import java.util.List;

public interface DataManagementService {

    /**
     * 获取车辆品牌型号信息
     * @return
     */
    List<CarType> getCarType();

    /**
     * 获取销售类型信息集合
     * @return
     */
    List<SellType> getSellTypes();

    /**
     * 获取附加业务
     * @return
     */
    List<Addition> getAdditions();
}
