package com.yryz.quanhu.behavior.like.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageHelper;
import com.yryz.common.response.PageList;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.DateUtils;
import com.yryz.quanhu.behavior.common.manager.EventManager;
import com.yryz.quanhu.behavior.common.manager.MessageManager;
import com.yryz.quanhu.behavior.common.manager.UserRemote;
import com.yryz.quanhu.behavior.common.util.ThreadPoolUtil;
import com.yryz.quanhu.behavior.count.contants.BehaviorEnum;
import com.yryz.quanhu.behavior.count.service.CountService;
import com.yryz.quanhu.behavior.like.dao.LikeCache;
import com.yryz.quanhu.behavior.like.dao.LikeDao;
import com.yryz.quanhu.behavior.like.dto.LikeFrontDTO;
import com.yryz.quanhu.behavior.like.entity.Like;
import com.yryz.quanhu.behavior.like.service.LikeNewService;
import com.yryz.quanhu.behavior.like.vo.LikeInfoVO;
import com.yryz.quanhu.behavior.like.vo.LikeVO;
import com.yryz.quanhu.support.id.api.IdAPI;
import com.yryz.quanhu.user.vo.UserSimpleNoneOtherVO;

@Service
public class LikeServiceNewImpl implements LikeNewService {
	private static final Logger logger = LoggerFactory.getLogger(LikeServiceNewImpl.class);
	@Autowired
	private LikeDao likeDao;
	@Autowired
	private LikeCache likeCache;

	@Reference(check = false)
	private IdAPI idAPI;
	@Autowired
	private EventManager eventService;
	@Autowired
	private CountService countService;
	@Autowired
	private MessageManager messageService;
	@Autowired
	private UserRemote userService;

	@Override
	@Transactional
	public int accretion(Like like) {
		//取消点赞
		if(isLike(like) == 1){
			return cleanLike(like);
		}
		like.setCreateDate(new Date());
		like.setKid(ResponseUtils.getResponseData(idAPI.getSnowflakeId()));		
		int result = likeDao.accretion(like);
		if(result > 0){
			likeCache.saveLike(like);
			
			//点赞数+1
			countService.commitCount(BehaviorEnum.Like,String.valueOf(like.getResourceId()) ,"", 1l);
			//发消息
			messageService.likeSendMsg(like);
			//对本人的点赞不计成长
			if(like.getUserId() != like.getResourceUserId()){
				eventService.likeCommitEvent(like);
			}
		}
		return result;
	}

	@Override
	public int isLike(Like like) {
		int flag = likeCache.checkLikeFlag(like.getResourceId(), like.getUserId()) ? 1 : 0;
		if(flag == 0){
			//异步同步点赞状态
			syncLikeCache(Lists.newArrayList(like.getResourceId()), like.getUserId());
		}
		return flag; 
	}

	@Override
	@Transactional
	public int cleanLike(Like like) {
		int result = likeDao.cleanLike(like);
		//清理缓存
		likeCache.delLike(like.getResourceId(), like.getUserId());
		//总数-1
		countService.commitCount(BehaviorEnum.Like, String.valueOf(like.getResourceId()), "", -1l);
		return result;
	}

	@Override
	public PageList<LikeInfoVO> listLike(LikeFrontDTO likeFrontDTO){
		PageList<LikeInfoVO> pageList = new PageList<>(likeFrontDTO.getCurrentPage(), likeFrontDTO.getPageSize(), null);
		Set<String> userIds = getLike(likeFrontDTO);
		if(CollectionUtils.isEmpty(userIds)){
			return pageList;
		}
		//查询点赞时间
		Map<String,Long> likeTimeMap = likeCache.getLikeTime(likeFrontDTO.getResourceId(), userIds);
		
		Long count = countService.getCount(String.valueOf(likeFrontDTO.getResourceId()), BehaviorEnum.Like.getCode(), "");
		
		pageList.setCount(count);
		Map<String,UserSimpleNoneOtherVO> userMap = userService.getUserInfo(userIds);
		List<LikeInfoVO> infoVOs = new ArrayList<>();
		for(Iterator<String> iterator = userIds.iterator();iterator.hasNext();){
			String userId = iterator.next();
			LikeInfoVO infoVO = new LikeInfoVO();
			infoVO.setResourceId(likeFrontDTO.getResourceId());	
			Long createTime = likeTimeMap.get(userId);
			infoVO.setCreateTime(createTime == null ? 0l : createTime);
			UserSimpleNoneOtherVO otherVO = userMap.get(userId);
			infoVO.setUser(otherVO == null ? new UserSimpleNoneOtherVO() : otherVO);
			infoVOs.add(infoVO);
		}
		pageList.setEntities(infoVOs);
		
		return pageList;
	}
	
	@Override
	public LikeVO querySingleLiker(Like like) {
		 return likeDao.querySingleLiker(like);
	}
	
	/**
	 * 查询点赞列表
	 * @param likeFrontDTO
	 * @return
	 */
	private Set<String> getLike(LikeFrontDTO likeFrontDTO){
		Set<String> userIds = likeCache.getLikeUserId(likeFrontDTO.getResourceId(), likeFrontDTO.getCurrentPage() * likeFrontDTO.getPageSize(), likeFrontDTO.getPageSize());
		if(CollectionUtils.isNotEmpty(userIds)){
			return userIds;
		}
		userIds = new HashSet<>();
		PageHelper.startPage(likeFrontDTO.getCurrentPage(), likeFrontDTO.getPageSize(), false);
		List<LikeVO> likeVOS = likeDao.queryLikers(likeFrontDTO);
		if(CollectionUtils.isNotEmpty(likeVOS)){
			for(LikeVO vo : likeVOS){
				Like like = new Like();
				like.setResourceId(vo.getResourceId());
				like.setUserId(vo.getUserId());
				like.setCreateDate(DateUtils.parseDate(vo.getCreateDate()));
				likeCache.saveLike(like);
				userIds.add(String.valueOf(vo.getUserId()));
			}
		}
		return userIds;
	}
	
	@Override
	public Map<String, Integer> getLikeFlagBatch(List<Long> resourceIds, long userId) {
		Map<String, Integer> flagMap = new HashMap<>(resourceIds.size());
		for(Long resourceId:resourceIds){
			Like like = new Like();
			like.setResourceId(resourceId);
			like.setUserId(userId);
			int flag = isLike(like);
			flagMap.put(resourceId.toString(), flag);
		}
		return flagMap;
	}
	
	
	
	/**
	 * 异步同步点赞状态无的点赞数据
	 * @param resourceIds
	 * @param userId
	 */
	public void syncLikeCache(List<Long> resourceIds, long userId){
		ThreadPoolUtil.execue(new Runnable() {
			
			@Override
			public void run() {
				Map<String, Integer> flagMap = getLikeFlagBatch(resourceIds, userId);
				for(Long resourceId : resourceIds){
					int result = flagMap.get(resourceId.toString());
					if(result == 0){
						Like like = new Like();
						like.setResourceId(resourceId);
						like.setUserId(userId);
						LikeVO likeVO = querySingleLiker(like);
						if(likeVO != null){
							like.setCreateDate(DateUtils.parseDate(likeVO.getCreateDate()));
							likeCache.saveLike(like);
						}
					}
				}
			}
		});
		
	}
}