package com.ydxsj.ydsoldnote.bean.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentMsg {

    private Integer id;
    private String equipmentBrand;
    private String equipmentTypeNum;
    private String size;
    private Integer status;
}
