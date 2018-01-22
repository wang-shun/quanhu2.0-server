package com.yryz.quanhu.order.score.service;

import java.util.Date;

import com.yryz.quanhu.order.score.entity.ScoreStatus;

/**
 * Created by lsn on 2017/8/28.
 */

public interface ScoreStatusService {
	
	Long save(ScoreStatus ss);

    int update(ScoreStatus ss);

    ScoreStatus getById(String userId , String appId , Long id , Date createDate);

    ScoreStatus getByCode(String userId , String appId , String eventCode , Date createDate);
}
