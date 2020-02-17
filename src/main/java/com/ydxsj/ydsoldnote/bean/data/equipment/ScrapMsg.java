package com.ydxsj.ydsoldnote.bean.data.equipment;

import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapMsg {
    private Integer id;
    private Integer requestUserId;
    private Integer equipmentMsgId;
    private Integer count;
    private String createTime;

    private User requestUser;
    private EquipmentMsg EquipmentMsg;


}
