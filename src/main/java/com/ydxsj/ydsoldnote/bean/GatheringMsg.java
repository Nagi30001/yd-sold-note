package com.ydxsj.ydsoldnote.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatheringMsg {
    private Integer id;
    private Integer ydGathering;
    private Integer thirdPartyGathering;
    private Integer gatheringStatus;
    private Integer gatheringUserId;
    private String gatheringCheckTime;
    private String gatheringImageUrl;
    private String text;
}
