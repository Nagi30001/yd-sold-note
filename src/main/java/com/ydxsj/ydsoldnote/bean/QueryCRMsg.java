package com.ydxsj.ydsoldnote.bean;

import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryCRMsg {
    private Integer userId;
    private String type;
    private String startingDate;
    private String endDate;
    private String sellName;
    private String sellType;
    private Integer TPId;
    private String clientName;
    private String clientCarNum;
    private String checkTimeType;
    private List<String> additionType;
    private String channel;
    private List<User> users;
    private List<String> provinces;
    private Integer status;
}
