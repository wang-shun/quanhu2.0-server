package com.yryz.quanhu.support.activity.service.impl;

import com.yryz.common.response.PageList;
import com.yryz.quanhu.support.activity.dao.*;
import com.yryz.quanhu.support.activity.entity.ActivityInfo;
import com.yryz.quanhu.support.activity.entity.ActivityPrizes;
import com.yryz.quanhu.support.activity.entity.ActivityVoteConfig;
import com.yryz.quanhu.support.activity.service.AdminActivityVoteService;
import com.yryz.quanhu.support.activity.util.DateUtils;
import com.yryz.quanhu.support.activity.dto.AdminActivityInfoVoteDto;
import com.yryz.quanhu.support.activity.dto.AdminActivityVoteDetailDto;
import com.yryz.quanhu.support.activity.vo.AdminActivityInfoVo1;
import com.yryz.quanhu.support.activity.vo.AdminActivityVoteDetailVo;
import com.yryz.quanhu.support.activity.vo.AdminActivityVoteVo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

import static com.yryz.common.constant.ModuleContants.ACTIVITY_ENUM;

//import com.github.pagehelper.PageHelper;


@Service
public class AdminActivityVoteServiceImpl implements AdminActivityVoteService {

	private Logger logger = LoggerFactory.getLogger(AdminActivityVoteServiceImpl.class);

	@Autowired
	ActivityInfoDao activityInfoDao;

	@Autowired
	ActivityVoteDao activityVoteDao;

	@Autowired
	ActivityVoteConfigDao activityVoteConfigDao;

	@Autowired
	ActivityPrizesDao activityPrizesDao;

	@Autowired
	ActivityVoteDetailDao activityParticipationDao;

	/*@Autowired
	CustAPI custAPI;

	@Autowired
	MessageUtils messageUtils;*/
	
/*	@Autowired
	EventReportDao eventReportDao;*/

	/**
	 * 活动列表
	 */
	@Override
	public PageList adminlist(AdminActivityInfoVoteDto param) {
//		PageHelper.startPage(param.getPageNo(), param.getPageSize());
		Integer pageNo = param.getPageNo();
		Integer pageSize = param.getPageSize();
		if(param.getPageNo()==null||param.getPageNo()<=0){
			param.setPageNo(0);
		}else{
			param.setPageNo((param.getPageNo()-1)*param.getPageSize());
		}
		if(param.getPageSize()==null||param.getPageSize()<=0){
			param.setPageSize(10);
		}
		List<AdminActivityVoteVo> list = activityVoteDao.adminlist(param);
		if (CollectionUtils.isEmpty(list)) {
			return new PageList(pageNo, pageSize, list, 0L);
		}
		Date date = new Date();
		for (AdminActivityVoteVo activity : list) {
			try {
				//设置分享数
				/*Integer count = eventReportDao.getShareCount(activity.getId().toString(),"1006");
				System.out.println();
				if(count!=null && count>0){
					activity.setShareCount(count.longValue());
				}else{
					activity.setShareCount(0l);
				}*/
				//设置总投票数
				Integer voteTotalCount = activityVoteDao.getVoteTotalCount(activity.getId());
				if(voteTotalCount!=null && voteTotalCount>0){
					activity.setVoteTotalCount(voteTotalCount);
				}else{
					activity.setVoteTotalCount(0);
				}
				// 设置活动状态
				if (DateUtils.getDistanceOfTwoDate(date, activity.getBeginTime()) > 0) {
					// 未开始
					activity.setActivityStatus(1);
					// 进行中
				} else if (DateUtils.getDistanceOfTwoDate(activity.getBeginTime(), date) >= 0
						&& DateUtils.getDistanceOfTwoDate(date, activity.getEndTime()) >= 0) {
					activity.setActivityStatus(2);
				} else if (DateUtils.getDistanceOfTwoDate(date, activity.getEndTime()) < 0) {
					activity.setActivityStatus(3);
				}
				
			} catch (Exception e) {
				logger.info("获取报名列表失败");
			}
		}
		return new PageList(pageNo, pageSize, list, activityVoteDao.adminlistCount(param));
	}

	@Override
	public Integer activityRelease(ActivityInfo activity, ActivityVoteConfig config, List<ActivityPrizes> prizes) {
		Assert.notNull(activity, "activity 不能为空");
		/*TODO activity.setCreateUserId(UserUtils.getUser().getId());*/
		activity.setActivityChannelCode("");
		activity.setModuleEnum(ACTIVITY_ENUM);
		activityInfoDao.insert(activity);		
		Long activityInfoId = 0L;
		if(null==activity.getId() || activity.getId().intValue()==0){
			logger.info("insert activity return null");
			Integer id = activityInfoDao.selectMaxId();
			logger.info("insert activity getMaxId:"+id);
			activityInfoId = Long.valueOf(id);
		}else{
			activityInfoId = activity.getId();
		}
		activity.setActivityChannelCode("HD-"+activityInfoId);
		activityInfoDao.update(activity);
		Assert.notNull(config, "config 不能为空");
		config.setActivityInfoId(activityInfoId);
		/*TODO config.setCreateUserId(UserUtils.getUser().getId());*/
		activityVoteConfigDao.insert(config);

		Assert.notNull(prizes, "prizes 不能为空");
		for (ActivityPrizes activityPrizes : prizes) {
			activityPrizes.setActivityInfoId(activityInfoId);
			/*TODO activityPrizes.setCreateUserId(UserUtils.getUser().getId());*/
			activityPrizesDao.insert(activityPrizes);
		}
		return 1;
	}

	@Override
	public AdminActivityInfoVo1 getActivityDetail(Long id) {
		return activityVoteDao.selectByPrimaryKey(id);
	}

	@Override
	public ActivityVoteConfig getConfigDetailByActivityId(String activityId) {
		return activityVoteConfigDao.selectVoteByActivityInfoId(Long.valueOf(activityId));
	}

	@Override
	public List<ActivityPrizes> getPrizesListByActivityId(String activityId) {
		return activityPrizesDao.selectAvailablePrizes(Long.valueOf(activityId));
	}

	@Override
	public PageList<AdminActivityVoteDetailVo> selectRankList(AdminActivityVoteDetailDto adminActivityVoteDetailDto) {
//		PageHelper.startPage(adminActivityVoteDetailDto.getPageNo(), adminActivityVoteDetailDto.getPageSize());
		Integer pageNo = adminActivityVoteDetailDto.getPageNo();
		Integer pageSize = adminActivityVoteDetailDto.getPageSize();
		if(adminActivityVoteDetailDto.getPageNo()==null|| adminActivityVoteDetailDto.getPageNo()<=0){
			adminActivityVoteDetailDto.setPageNo(0);
		}else{
			adminActivityVoteDetailDto.setPageNo((adminActivityVoteDetailDto.getPageNo()-1)* adminActivityVoteDetailDto.getPageSize());
		}
		if(adminActivityVoteDetailDto.getPageSize()==null|| adminActivityVoteDetailDto.getPageSize()<=0){
			adminActivityVoteDetailDto.setPageSize(10);
		}
		List<AdminActivityVoteDetailVo> list = activityParticipationDao.selectRankList(adminActivityVoteDetailDto.getActivityInfoId());
		if (CollectionUtils.isEmpty(list)) {
			return new PageList(pageNo, pageSize, list, 0L);
		}
		for (AdminActivityVoteDetailVo detailVo : list) {
			if(detailVo.getAddVote()!=null){
			detailVo.setTotalCount(detailVo.getVoteCount()+ detailVo.getAddVote().intValue());
			}else{
				detailVo.setTotalCount(detailVo.getVoteCount());
			}
			// TODO 获取平台用户数据
			/*CustInfo custInfo = custAPI.getCustInfo(detailVo.getCreateUserId());
			if (custInfo != null) {
				detailVo.setUserName(custInfo.getCustName());
				detailVo.setNickName(custInfo.getCustNname());
				detailVo.setUserImg(custInfo.getCustImg());
			}*/

		}
		return new PageList(pageNo, pageSize, list,
				activityParticipationDao.adminRanklistCount(adminActivityVoteDetailDto));
	}

	// TODO 恭喜！您在YYYY中获得了第X名，奖励将由工作人员联系您后进行发放，先去看看获得的奖励吧！
	public Integer sendMessageVote(ActivityInfo activityInfo) {
		try {
			/*CustInfo custInfo = new CustInfo();
			ActivityInfo actInfo = activityInfoDao.selectByPrimaryKey(activityInfo.getId());
			// 排行榜
			List<AdminActivityVoteDetailVo> list = activityParticipationDao.selectRankList(activityInfo.getId());
			
			for (int i = 0; i < list.size(); i++) {
				InteractiveBody interactiveBody = new InteractiveBody();
                // 获取平台用户
				custInfo = custAPI.getCustInfo(list.get(i).getCreateUserId());
				if (custInfo != null) {
					interactiveBody.setCustId(custInfo.getCustId());
					interactiveBody.setCustName(custInfo.getCustNname());
				}
				interactiveBody.setBodyTitle(actInfo.getTitle());
				interactiveBody.setBodyImg(list.get(i).getCoverPlan());
				
				messageUtils.sendMessages(MessageConstant.ACTIVITY_VOTE_REWARD, custInfo.getCustId(), actInfo.getTitle(),(i+1+""),
						interactiveBody);
			}*/

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("活动投票奖励，发送消息错误:{}", e);
		}
		return 1;
	}

	@Override
	public Integer updateSave(ActivityInfo activity) {
		// TODO Auto-generated method stub
		Assert.notNull(activity, "activity 不能为空");
		/*activity.setLastUpdateUserId(UserUtils.getUser().getId());*/
		return activityVoteDao.update(activity);
	}

}