package com.yryz.quanhu.order.score.rule.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yryz.common.utils.JsonUtils;
import com.yryz.quanhu.order.score.rule.service.RuleScoreService;
import com.yryz.quanhu.order.score.service.EventAcountService;
import com.yryz.quanhu.order.score.service.ScoreFlowService;
import com.yryz.quanhu.score.entity.ScoreEventInfo;
import com.yryz.quanhu.score.entity.ScoreFlow;
import com.yryz.quanhu.score.vo.EventAcount;

import net.sf.json.JSONObject;

/**
 * 循环规则积分事件服务基类
 * 
 * @author lsn
 *
 */
@Transactional
@Service
public abstract class BaseRuleScoreServiceImpl implements RuleScoreService {
	
	
	private static final Logger logger = LoggerFactory.getLogger(BaseRuleScoreServiceImpl.class);

	@Autowired
	ScoreFlowService scoreFlowService;

	@Autowired
	EventAcountService eventAcountService;
	
    // RedisTemplate 含有泛型,无法使用 @Autowired by type 注入,只能使用@Resource by name注入
    @Autowired
    private StringRedisTemplate redisTemplate;
    


	Long getRedisExpireMillis() {
		Calendar curDate = Calendar.getInstance();
		Calendar tommorowDate = new GregorianCalendar(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH),
				curDate.get(Calendar.DATE) + 1, 0, 0, 0);
		return tommorowDate.getTimeInMillis() - curDate.getTimeInMillis();
	}
	


	/**
	 * statusKey: 状态key needScore: 是否需要记录积分流水
	 */
	@Override
	public boolean updateStatus(String statusKey,  boolean needScore, boolean status) {
		boolean statusPresent = redisTemplate.hasKey(statusKey);
		//NX——如果它还不存在，就只设置键。XX——只设置它已经存在的键
		if (statusPresent) {
			//redisTemplate.opsForValue().set(statusKey, String.valueOf(status), "XX", "PX", getRedisExpireMillis());
			//redisTemplate.opsForValue().set(statusKey, String.valueOf(status),getRedisExpireMillis(), TimeUnit.MILLISECONDS);
			eventAcountService.redislocksset(redisTemplate, statusKey, String.valueOf(status), "XX", "PX", getRedisExpireMillis());
			
			return true;
		}
		//	jedis.set(statusKey, String.valueOf(status), "NX", "PX", getRedisExpireMillis());
		//redisTemplate.opsForValue().set(statusKey, "", getRedisExpireMillis(),TimeUnit.MILLISECONDS);
		eventAcountService.redislocksset(redisTemplate, statusKey, String.valueOf(status), "NX", "PX", getRedisExpireMillis());
		return true;
	}

	// 规则判定的最终目标是要记录积分流水，记录积分流水与更新积分总值总是同步进行的
	// @Transactional(propagation = Propagation.REQUIRED)
	public Long saveScoreFlow(String userId, String eventCode, ScoreEventInfo sei, int newScore, double amount) {
		// 计算新增积分数
		// 数据库记录的积分类型与编码中的枚举映射 关系 1：一次性触发 Once 2：每次触发 Pertime 3：条件日期循环触发 Loop
		// 4:受连续区间影响的触发积分
		Date now = new Date();
		switch (sei.getEventType()) {
		case "4":
			// 签到的特殊情况，新增积分通过参数传递，不在内部计算
			break;
		default:
			newScore = sei.getEventScore();
			if (amount > 0) {
				// 统一转换金额单位为：元
				newScore = (int) (amount * sei.getAmountPower() / 100);
				break;
			}
			break;
		}


		
//		long allScore = 0;
		// 会导致重复写入问题，初始化放到注册时统一初始化
		//EventAcount ea = eventAcountService.getLastAcount(userId);
		// 总值表无数据 ，则初始化该表
//		if (ea == null || ea.getId() == null) {
//			allScore = 0L + newScore;
//			ea = new EventAcount(userId);
//			ea.setScore(Math.abs(allScore));
//			ea.setCreateTime(now);
//			ea.setUpdateTime(now);
//			logger.info("-------处理积分运算事件if(EventAcount) == null，每次触发传入数据：={}",JSONObject.fromObject(ea));
//			eventAcountService.save(ea);
//		} else {
//
//			// 更新时，由于积分和成长都在更新，可能取出来的跟积分无关的数据在更新积分时被回写到数据库
//			allScore = ea.getScore() + newScore;
//			logger.info("-------处理积分运算事件else(EventAcount)，每次触发传入数据：ea.getScore(): " + ea.getScore()+" newScore: "+newScore+" allScore: "+allScore);
//			ea.setScore(Math.abs(allScore + 0L));
//			ea.setUpdateTime(now);
//			ea.setGrow(null);
//			ea.setGrowLevel(null);
//			logger.info("-------处理积分运算事件else(EventAcount)，每次触发传入数据：={}",JSONObject.fromObject(ea));
//			eventAcountService.update(ea);
//		}
		
		//记录用户事件账户表,数据库判断是否新增还是更新
		EventAcount ea = new EventAcount();
		ea.setScore(Long.valueOf(Math.abs(newScore)));
		ea.setCreateTime(now);
		ea.setUpdateTime(now);
		ea.setUserId(userId);
		logger.info("-------处理积分运算事件(EventAcount总表)，每次触发传入数据：={}", JsonUtils.toFastJson(ea,null));
		eventAcountService.saveOrUpdate(ea);
		
		// 无论总值表有无数据，sf是要记的
		ScoreFlow sf = new ScoreFlow(userId, eventCode, newScore);
		//数据库计算
		sf.setAllScore(Long.valueOf(newScore));
		sf.setCreateTime(now);
		sf.setUpdateTime(now);
		logger.info("-------处理积分运算事件(ScoreFlow流水表)，每次触发传入数据：={}",JsonUtils.toFastJson(ea,null));
		scoreFlowService.save(sf);

		return 0L;
	}

}
