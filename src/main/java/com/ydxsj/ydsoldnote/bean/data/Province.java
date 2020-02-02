package com.ydxsj.ydsoldnote.bean.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Province {

    private Integer id;
    private String province;
    private List<City> cities;
}
