package com.yryz.quanhu.order.score.manage.dao.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yryz.common.response.PageList;
import com.yryz.quanhu.score.entity.ScoreEventInfo;

/**
 * 积分事件管理
 * Created by lsn on 2017/8/28.
 */
@Mapper
public interface ScoreEventManageDao {

    void save(ScoreEventInfo se);

    int delById(Long id);

    int update(ScoreEventInfo se);

    ScoreEventInfo getByCode(String code);

    PageList<ScoreEventInfo> getAll();
    
    List<ScoreEventInfo> getPage(ScoreEventInfo se);
}
