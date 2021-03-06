package com.yryz.quanhu.other.activity.dao;

import com.yryz.quanhu.other.activity.entity.ActivityEnrolConfig;
import com.yryz.quanhu.other.activity.vo.ActivityEnrolConfigVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
  * @ClassName: ActivityEnrolConfigDao
  * @Description: ActivityEnrolConfig数据访问接口
  * @author jiangzhichao
  * @date 2018-01-20 14:21:28
  *
 */
@Mapper
public interface ActivityEnrolConfigDao {

    ActivityEnrolConfigVo selectByKid(Long kid);

    int delete(Long kid);

    int insert(ActivityEnrolConfig activityEnrolConfig);

    int insertByPrimaryKeySelective(ActivityEnrolConfig activityEnrolConfig);

    int update(ActivityEnrolConfig activityEnrolConfig);

    ActivityEnrolConfig selectByActivityId(Long activityKid);







    //ActivityEnrolConfig selectByPrimaryKey(Long id);

    //void updateByActivityId(ActivityEnrolConfig activityEnrolConfig);
}