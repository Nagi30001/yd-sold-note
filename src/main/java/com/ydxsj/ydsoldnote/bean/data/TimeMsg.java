package com.ydxsj.ydsoldnote.bean.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeMsg {

    private String typeName;
    private String typeTime;
    private String color;
    private String type;
    private String icon;
}
