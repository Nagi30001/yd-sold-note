package com.ydxsj.ydsoldnote.bean.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellType {

    private Integer id;
    private String sellType;
    private Double money;
    private Double cashPledge;
    private String explains;
}
