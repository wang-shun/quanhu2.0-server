package com.yryz.quanhu.other.activity.dao;

import com.yryz.quanhu.other.activity.entity.ActivityPrizes;
import com.yryz.quanhu.other.activity.vo.ActivityPrizesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
  * @ClassName: ActivityPrizesDao
  * @Description: ActivityPrizes数据访问接口
  * @author jiangzhichao
  * @date 2018-01-20 14:23:02
  *
 */
@Mapper
public interface ActivityPrizesDao {

    ActivityPrizesVo selectByKid(Long kid);

    int delete(Long kid);

    void insert(ActivityPrizes activityPrizes);

    void insertByPrimaryKeySelective(ActivityPrizes activityPrizes);

    int update(ActivityPrizes activityPrizes);

    List<ActivityPrizesVo> selectListCondition(@Param("activityInfoId") Long activityInfoId);

    List<ActivityPrizesVo> selectAvailablePrizesVo(Long activityInfoId);

    int updateIssueNum(@Param("kid") Long kid);

    ActivityPrizes selectByPrimaryKey(Long kid);

    List<ActivityPrizes> selectAvailablePrizes(@Param("activityInfoId") Long activityInfoId);

}