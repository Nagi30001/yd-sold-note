package com.ydxsj.ydsoldnote.mapper;


import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.GatheringMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SellReceiptsMapper {

    /**
     * 根据城市信息获取相关的单据
     * @param city
     * @return
     */
    List<CarReceipts> getCarReceipts(List<String> city);

    /**
     * 获取全部销售单据
     * @return
     */
    List<CarReceipts> getCarReceipts();

    /**
     * 添加收款信息
     * @param gatheringMsg
     * @return
     */
    Integer insertGatheringMsg(@Param("gatheringMsg") GatheringMsg gatheringMsg);

    /**
     * 插入销售单据数据
     * @param carReceipts
     * @return
     */
    Integer insertCarReceiots(@Param("carReceipts") CarReceipts carReceipts);

    /**
     * 根据省份获取该省份的单据
     * @param provinces
     * @return
     */
    List<CarReceipts> getCarReceiptsByProvince(@Param("provinces") List<String> provinces);


    /**
     * 根据收款信息id获取信息
     * @param gatheringMsgId
     * @return
     */
    GatheringMsg getGatheringMsgById(@Param("gatheringMsgId") Integer gatheringMsgId);

    /**
     * 获取某状态的单据
     * @param i
     * @return
     */
    List<CarReceipts> getCarReceiptsByStatus(int i);

    /**
     * 获取某状态的单据 平台用户的
     * @param i
     * @param id
     * @return
     */
    List<CarReceipts> getCarReceiptsByStatusOfTH(@Param("i") int i,@Param("id") Integer id);

    /**
     * 根据单据id获取单据
     * @param id
     * @return
     */
    CarReceipts getCarReceiptsById(@Param("id") Integer id);
}
