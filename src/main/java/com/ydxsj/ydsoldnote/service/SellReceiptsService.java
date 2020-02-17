package com.ydxsj.ydsoldnote.service;

import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface SellReceiptsService {

    /**
     * 获取相关销售单据
     * @param user
     * @return
     */
    List<CarReceipts> getCarReceipts(User user);

    /**
     * 销售单据新增
     * @param map
     * @return
     */
    CarReceipts addSellReceipts(Map map);

    /**
     * 根据用户 待收款状态 收款人为该用户的单据
     * @param user
     * @return
     */
    List<CarReceipts> getReceiptsByGathering(User user);

    /**
     * 获取用户待到店确认状态的表单
     * @param user
     * @return
     */
    List<CarReceipts> getReceiptsByReach(User user);

    /**
     * 获取安装戴确认的单据
     * @param user
     * @return
     */
    List<CarReceipts> getReceiptsByInstall(User user);

    /**
     * 审核单据
     * @param token 审核人token
     * @param type 审核类型
     * @param id 单据id
     * @param checkTime 确认时间
     * @param map
     * @return
     */
    Integer checkReceipts(String token, String type, Integer id, String checkTime, Map map);

    /**
     * 根据类型保存响应的图片/文件
     * @param map
     * @param file
     * @return
     */
    boolean uploadFile(Map map, MultipartFile file);


    /**
     * 销售获取我的报单，平台获取平台的报单
     * @param token
     * @return
     */
    List<CarReceipts> getMyReceipts(String token);


    /**
     * 作废该单据
     * @param id
     * @return
     */
    boolean cancellationCarReceiptsById(String token , Integer id);

    /**
     * 根据条件查询单据
     * @param map
     * @return
     */
    List<CarReceipts> searchQueryDate(Map map) throws ParseException;
}
