package com.yryz.quanhu.order.score.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.yryz.quanhu.grow.service.GrowAPI;
import com.yryz.quanhu.order.score.manage.service.ScoreEventManageService;
import com.yryz.quanhu.order.score.service.EventAcountService;
import com.yryz.quanhu.order.score.service.ScoreFlowService;
import com.yryz.quanhu.score.entity.ScoreEventInfo;
import com.yryz.quanhu.score.entity.ScoreFlow;
import com.yryz.quanhu.score.entity.ScoreFlowQuery;
import com.yryz.quanhu.score.service.ScoreAPI;
import com.yryz.quanhu.score.vo.EventAcount;

@Service(interfaceClass=ScoreAPI.class)
public class ScoreAPIImpl implements ScoreAPI {

	@Autowired
	ScoreEventManageService scoreEventManageService;

	@Autowired
	ScoreFlowService scoreFlowService;

	@Autowired
	EventAcountService eventAcountService;

	@Override
	public Long saveScoreEvent(ScoreEventInfo sei) {
		return scoreEventManageService.save(sei);
	}

	@Override
	public int updateScoreEvent(ScoreEventInfo sei) {
		return scoreEventManageService.update(sei);
	}

	@Override
	public int delScoreEvent(Long id) {
		return scoreEventManageService.delById(id);
	}

	@Override
	public List<ScoreEventInfo> getScoreEventPage(int start, int limit) {
		return scoreEventManageService.getPage(start, limit);
	}

	@Override
	public List<ScoreFlow> getScoreFlowPage(ScoreFlowQuery sfq, int flowType, int start, int limit) {
		return scoreFlowService.getPage(sfq, flowType, start, limit);
	}

	@Override
	public int consumeScore(String custId, int score, String eventCode) {
		score = Math.abs(score);
		// 消费记流水
		EventAcount ea = eventAcountService.getLastAcount(custId);
		// 更新总积分值
		if (ea != null && ea.getId() != null) {
			Long allScore = ea.getScore();
			if (allScore >= score) {
				Date now = new Date();
				ScoreFlow sf = new ScoreFlow(custId, eventCode, score);
				sf.setConsumeFlag(1);
				sf.setAllScore(allScore - score);
				sf.setCreateTime(now);
				sf.setUpdateTime(now);
				scoreFlowService.save(sf);
				ea.setScore(-Math.abs(score + 0L));
				ea.setUpdateTime(now);
				ea.setGrow(null);
				ea.setGrowLevel(null);
				return eventAcountService.update(ea);
			}
			return 0;
		}
		return 0;
	}

	@Override
	public int addScore(String custId, int score, String eventCode) {
		EventAcount ea = eventAcountService.getLastAcount(custId);
		Date now = new Date();
		ScoreFlow sf = new ScoreFlow(custId, eventCode, score);
		sf.setConsumeFlag(0);
		sf.setNewScore(score);
		sf.setCreateTime(now);
		sf.setUpdateTime(now);
		if (ea != null && ea.getId() != null) {
			Long allScore = ea.getScore();
			sf.setAllScore(allScore + score);
			scoreFlowService.save(sf);
			ea.setScore(Math.abs(score + 0L));
			ea.setUpdateTime(now);
			ea.setGrow(null);
			ea.setGrowLevel(null);
			return eventAcountService.update(ea);
		}

		sf.setAllScore(score + 0L);
		scoreFlowService.save(sf);
		ea = new EventAcount(custId);
		ea.setCreateTime(now);
		ea.setUpdateTime(now);
		ea.setScore(score + 0L);
		eventAcountService.save(ea);
		return 0;
	}

	@Override
	public List<ScoreEventInfo> getScoreEvent() {
		return scoreEventManageService.getAll();
	}

}