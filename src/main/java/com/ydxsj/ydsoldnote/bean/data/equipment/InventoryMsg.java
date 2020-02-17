package com.ydxsj.ydsoldnote.bean.data.equipment;

import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryMsg {

    private Integer id;
    private Integer thirdPartyTerraceId;
    private Integer equipmentMsgId;
    private Integer sumInventory;
    private Integer awaitInstall;
    private Integer inPurchase;
    private Integer inMaintain;
    private Integer inInventory;
    private Integer awaitReceive;

    private User user;
    private EquipmentMsg equipmentMsg;
}
