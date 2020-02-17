package com.ydxsj.ydsoldnote.bean.data.equipment;


import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeMsg {

    private Integer id;
    private Integer thirdPartyTerraceId;
    private Integer checkUserId;
    private String requestTime;
    private String passTime;
    private String changeTime;
    private String cancellationTime;
    private String catType;
    private String changeCause;
    private String videoUrl;
    private Integer status;
    private Integer equipmentMsgId;

    private User requestUser;
    private User checkUser;
    private EquipmentMsg equipmentMsg;

}
