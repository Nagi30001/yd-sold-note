package com.ydxsj.ydsoldnote.bean.data.equipment;

import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintainMsg {
    private Integer id;
    private Integer requestUserId;
    private Integer consigneeUserId;
    private Integer equipmentMsgId;
    private Integer count;
    private String requestTime;
    private String arriveTime;
    private String scrapTime;
    private Integer status;


    private User requestUser;
    private User consigneeUser;
    private EquipmentMsg EquipmentMsg;
}
