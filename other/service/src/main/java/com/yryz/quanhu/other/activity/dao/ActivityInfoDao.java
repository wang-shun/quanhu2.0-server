package com.yryz.quanhu.other.activity.dao;

import com.yryz.quanhu.other.activity.dto.AdminActivityInfoDto;
import com.yryz.quanhu.other.activity.dto.AdminActivityInfoSignUpDto;
import com.yryz.quanhu.other.activity.entity.ActivityInfo;
import com.yryz.quanhu.other.activity.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
  * @ClassName: ActivityInfoDao
  * @Description: ActivityInfo数据访问接口
  * @author jiangzhichao
  * @date 2018-01-20 14:22:12
  *
 */
@Mapper
public interface ActivityInfoDao {

    ActivityInfoVo selectByKid(Long kid);

    ActivityVoteInfoVo selectVoteByKid(Long kid);

    int delete(Long kid);

    void insert(ActivityInfo activityInfo);

    void insertByPrimaryKeySelective(ActivityInfo activityInfo);

    int update(ActivityInfo activityInfo);

    List<ActivityInfoAppListVo> selectMyList(@Param("custId") Long custId);

    Integer selectMylistCount(@Param("custId") Long custId);

    List<ActivityInfoAppListVo> selectAppList(@Param("type") Integer type);

    int updateJoinCount(@Param("kid") Long kid, @Param("userNum") Integer userNum);

    int adminUpdateJoinCount(@Param("kid") Long kid);





    int	addActivity(ActivityInfo activityInfo);

    AdminActivityInfoVo1 selectByPrimaryKey(Long kid);

    List<AdminActivityInfoSignUpVo> selectSignAdminlist(Integer pageNo, Integer pageSize,
                                                        @Param("activityInfoSignUpDto") AdminActivityInfoSignUpDto adminActivityInfoSignUpDto);

    Integer selectMaxId();

    long selectSignAdminlistCount(@Param("activityInfoSignUpDto") AdminActivityInfoSignUpDto adminActivityInfoSignUpDto);

    List<AdminActivityInfoVo> adminAllSharelist(Integer pageNo, Integer pageSize, @Param("activityInfoDto") AdminActivityInfoDto adminActivityInfoDto);

    long adminAllSharelistCount(@Param("activityInfoDto") AdminActivityInfoDto adminActivityInfoDto);

    List<AdminActivityInfoVo> adminAllSharelistNoPage();

    int updateJoinCountDiff(@Param("kid") Long activityInfoId);
}