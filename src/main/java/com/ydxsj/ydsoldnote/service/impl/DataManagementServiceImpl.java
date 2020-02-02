package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.bean.data.Addition;
import com.ydxsj.ydsoldnote.bean.data.CarType;
import com.ydxsj.ydsoldnote.bean.data.SellType;
import com.ydxsj.ydsoldnote.mapper.DataManagementMapper;
import com.ydxsj.ydsoldnote.service.DataManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataManagementServiceImpl implements DataManagementService {

    @Autowired
    private DataManagementMapper dataManagementMapper;

    @Override
    public List<CarType> getCarType() {
        List<CarType> carTypes = dataManagementMapper.getCarType();
        for (CarType carType : carTypes){
            carType.setSubsidiarys(carType.getSubsidiary().split(","));
        }

        return carTypes;
    }

    @Override
    public List<SellType> getSellTypes() {
        return dataManagementMapper.getSellTypes();
    }

    @Override
    public List<Addition> getAdditions() {
        return dataManagementMapper.getAdditions();
    }
}
