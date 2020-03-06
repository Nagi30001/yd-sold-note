package com.ydxsj.ydsoldnote.bean.data.equipment;

import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseMsg implements Comparable<PurchaseMsg>{

    private Integer id;
    private Integer purchaseUserId;
    private Integer consigneeUserId;
    private Integer equipmentMsgId;
    private Integer count;
    private String purchaseTime;
    private String arriveTime;
    private String scrapTime;
    private Integer status;


    private User purchaseUser;
    private User consigneeUser;
    private EquipmentMsg EquipmentMsg;

    @Override
    public int compareTo(PurchaseMsg o) {
        return this.purchaseTime.compareTo(o.getPurchaseTime());
    }
}
