package com.ydxsj.ydsoldnote.bean.data;


import com.ydxsj.ydsoldnote.bean.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Channel {

    private Integer id;
    private String province;
    private String city;
    private String channelName;
    private String site;
    private Integer status;
    private Integer createId;

    private User user;
}
