package com.ydxsj.ydsoldnote.mapper;


import com.ydxsj.ydsoldnote.bean.CarReceipts;
import com.ydxsj.ydsoldnote.bean.GatheringMsg;
import com.ydxsj.ydsoldnote.bean.ImageUrl;
import com.ydxsj.ydsoldnote.bean.QueryCRMsg;
import com.ydxsj.ydsoldnote.bean.user.User;
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
    List<CarReceipts> getCarReceiptsByProvince(@Param("provinces") List<String> provinces, @Param("page") Integer page ,@Param("count") Integer count);


    /**
     * 根据收款信息id获取信息
     * @param gatheringMsgId
     * @return
     */
    GatheringMsg getGatheringMsgById(@Param("gatheringMsgId") Integer gatheringMsgId);

    /**
     * 获取某状态的单据
     * @param i
     * @param id
     * @return
     */
    List<CarReceipts> getCarReceiptsByStatus(@Param("i") int i,@Param("id") Integer id);

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

    /**
     * 更新单据时间
     * @param carReceipts
     * @return
     */
    Integer updateCheckTime(@Param("carReceipts") CarReceipts carReceipts,@Param("type") String type);



    /**
     * 保存图片路径
     * @param imageUrl
     * @return
     */
    Integer insertImageUrl(@Param("imageUrl") ImageUrl imageUrl);


    /**
     * 更新收款信息
     * @param gatheringMsg
     * @return
     */
    Integer updateGatherimgMsg(@Param("gatheringMsg") GatheringMsg gatheringMsg);

    /**
     * 根据平台ID获取该平台单据
     * @param id
     * @return
     */
    List<CarReceipts> getCarReceiptsByTPId(@Param("id") Integer id);


    /**
     * 根据用户ID查询该ID创建的单据
     * @param id
     * @return
     */
    List<CarReceipts> getCarReceiptsByCreateId(@Param("id")Integer id);

    /**
     * 更新状态及作废时间
     * @param carReceipts
     * @return
     */
    Integer updateCarReceiptsStatus(@Param("carReceipts") CarReceipts carReceipts);

    /**
     * 根据单据ID获取图片对象集合
     * @param id
     * @return
     */
    List<ImageUrl> getImageUrls(@Param("id") Integer id);





    /**
     * 更新单据信息
     * @param carReceipts1
     * @return
     */
    Integer updateCarReceipts(@Param("s") CarReceipts carReceipts1);

    /**
     * 根据查询条件查询单据
     * @param queryCRMsg
     * @return
     */
    List<CarReceipts> searchCarReceipts(@Param("queryCRMsg") QueryCRMsg queryCRMsg);
}
