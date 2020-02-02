package com.ydxsj.ydsoldnote.bean.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarType {

    private Integer id;
    private String brand;
    private String subsidiary;
    private String[] subsidiarys;
}
