package com.ydxsj.ydsoldnote.bean;


import com.ydxsj.ydsoldnote.bean.data.TimeMsg;
import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarReceipts {


    private Integer id;
    private String receiptsNum;
    private Integer receiptsStatus;
    private String createTime;
    private String receiptsReachCheck;
    private Integer userId;
    private String province;
    private String city;
    private String clientName;
    private String clientIdentityDocument;
    private String openingBank;
    private String bankNum;
    private String clientPhone;
    private String carBrand;
    private String clientCarNum;
    private String equipmentBrand;
    private String equipmentTypeNum;
    private String size;
    private Integer thirdPartyTerraceId;
    private String thirdPartyCheckTime;
    private Integer count;
    private String sellTypeName;
    private String additionType;
    private String predictInstallTime;
    private Integer thirdPartyCheck;
    private String iccid;
    private Integer contractId;
    private Double money;
    private Double cashPledge;
    private Integer channelId;
    private String gatheringType;
    private Integer gatheringMsgId;
    private String cancellationTime;

    // 其他数据
    //收款信息数据
    private GatheringMsg gatheringMsg;
    // 平台信息
    private User tpUser;
    // 创建人信息
    private User user;
    // 时间集合
    private List<TimeMsg> timeMsgs;
    // 收款图片地址集合
    private List<ImageUrl> imageUrls;
}
