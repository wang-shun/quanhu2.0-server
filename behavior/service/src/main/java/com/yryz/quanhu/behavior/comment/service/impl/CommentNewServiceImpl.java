package com.yryz.quanhu.behavior.comment.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yryz.common.constant.ExceptionEnum;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.PageList;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.JsonUtils;
import com.yryz.common.utils.StringUtils;
import com.yryz.quanhu.behavior.comment.dao.CommentDao;
import com.yryz.quanhu.behavior.comment.dao.redis.CommentCache;
import com.yryz.quanhu.behavior.comment.dto.CommentDTO;
import com.yryz.quanhu.behavior.comment.dto.CommentFrontDTO;
import com.yryz.quanhu.behavior.comment.dto.CommentSubDTO;
import com.yryz.quanhu.behavior.comment.entity.Comment;
import com.yryz.quanhu.behavior.comment.service.CommentNewService;
import com.yryz.quanhu.behavior.comment.vo.CommentDetailVO;
import com.yryz.quanhu.behavior.comment.vo.CommentListInfoVO;
import com.yryz.quanhu.behavior.comment.vo.CommentSimpleVO;
import com.yryz.quanhu.behavior.comment.vo.CommentVOForAdmin;
import com.yryz.quanhu.behavior.common.manager.EventManager;
import com.yryz.quanhu.behavior.common.manager.MessageManager;
import com.yryz.quanhu.behavior.common.manager.UserRemote;
import com.yryz.quanhu.behavior.count.contants.BehaviorEnum;
import com.yryz.quanhu.behavior.count.service.CountService;
import com.yryz.quanhu.behavior.like.contants.LikeContants.LikeFlag;
import com.yryz.quanhu.behavior.like.entity.Like;
import com.yryz.quanhu.behavior.like.service.LikeNewService;
import com.yryz.quanhu.support.id.api.IdAPI;
import com.yryz.quanhu.user.vo.UserSimpleNoneOtherVO;

@Service
public class CommentNewServiceImpl implements CommentNewService {
	private static final Logger logger = LoggerFactory.getLogger(CommentNewServiceImpl.class);
	@Autowired
	private CommentDao commentDao;
	@Autowired
	private CommentCache commentCache;
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
	@Autowired
	private LikeNewService likeService;

	@Override
	@Transactional
	public Comment accretion(Comment comment) {
		// 上级评论或者根评论删除下架后不能评论
		if (comment.getParentId() != 0l) {
			Comment parentComment = getComment(comment.getParentId());
			if (null == parentComment || (parentComment.getDelFlag() == 11 || parentComment.getShelveFlag() == 11)) {
				throw QuanhuException.busiError(ExceptionEnum.COMMENT_CHECK_ERROR);
			}else{
				comment.setParentUserId(parentComment.getCreateUserId());
			}
			Comment topComment = getComment(comment.getTopId());
			if (null == topComment || (topComment.getDelFlag() == 11 || topComment.getShelveFlag() == 11)) {
				throw QuanhuException.busiError(ExceptionEnum.COMMENT_CHECK_ERROR);
			}
		}
		
		comment.setShelveFlag((byte) 10);
		comment.setDelFlag((byte) 10);
		comment.setCreateDate(new Date());
		comment.setKid(ResponseUtils.getResponseData(idAPI.getSnowflakeId()));

		int result = commentDao.accretion(comment);
		if (result > 0) {
			// 发消息
			messageService.commentSendMsg(comment);
			// 给自己发的文章、帖子不发事件
			if (comment.getTargetUserId() != comment.getCreateUserId()) {
				eventService.commentCommitEvent(comment);
			}
			
			commentCache.saveComment(comment);
			commentCache.addCommentList(comment);
			// 增加统计数
			countService.commitCount(BehaviorEnum.Comment, String.valueOf(comment.getResourceId()), "-1", 1L);
		}
		return comment;
	}

	@Override
	public Comment querySingleComment(Comment comment) {
		return commentDao.querySingleComment(comment);
	}

	@Override
	public int delComment(Comment comment) {
		Comment localComment = getComment(comment.getKid());
		if(localComment == null){
			throw QuanhuException.busiShowError("该评论不存在","该评论不存在");
		}
		if(localComment.getShelveFlag() == 11 || localComment.getDelFlag() == 11){
			throw QuanhuException.busiShowError("该评论已下架","该评论已下架");
		}
		// 只有本人或者资源作者可以删除评论
		if (!comment.getCreateUserId().equals(localComment.getCreateUserId())
				&& !comment.getCreateUserId().equals(localComment.getTargetUserId())) {
			throw QuanhuException.busiShowError("只有本人或者资源发布者可以删除评论", "只有本人或者资源发布者可以删除评论");
		}
		
		
		int result = commentDao.delComment(comment);
		commentCache.delCommentList(comment);
		commentCache.deleteComment(comment.getKid());
		countService.commitCount(BehaviorEnum.Comment, String.valueOf(comment.getResourceId()), "-1", -1L);
		if (comment.getTopId() == 0l) {
			delReplyComment(comment);
		}
		return result;
	}

	/**
	 * 删除下级全部回复
	 * 
	 * @param comment
	 */
	public void delReplyComment(Comment comment) {
		CommentFrontDTO commenFront = new CommentFrontDTO();
		commenFront.setTopId(comment.getKid());
		commenFront.setResourceId(comment.getResourceId());
		commenFront.setCheckType(1L);
		// 查询是否有回复
		List<Comment> comments = commentDao.listCommentByParams(commenFront);
		if (CollectionUtils.isEmpty(comments)) {
			return;
		}
		int length = comments.size();
		List<Long> commentIds = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			commentIds.add(comments.get(i).getKid());
		}
		// 删除子集评论
		commentDao.delCommentByTopId(comment);
		// 删除缓存子评论
		commentCache.deleteComment(commentIds);
		// 删除缓存回复评论列表
		commentCache.delCommentReplyList(comment.getKid());
		// 减少评论数
		countService.commitCount(BehaviorEnum.Comment, String.valueOf(comment.getResourceId()), "-1", -(long) length);
	}

	@Override
	public PageList<CommentListInfoVO> listComments(CommentFrontDTO commentFrontDTO) {
		PageList<CommentListInfoVO> pageList = new PageList<>(commentFrontDTO.getCurrentPage(),
				commentFrontDTO.getPageSize(), null);
		List<Comment> comments = getComment(commentFrontDTO);

		if (CollectionUtils.isEmpty(comments)) {
			return pageList;
		}

		// 获取评论总数
		Long pageTotals = countService.getCount(String.valueOf(commentFrontDTO.getResourceId()),
				BehaviorEnum.Comment.getCode(), "-1");
		pageList.setCount(pageTotals);

		int commentLength = comments.size();
		Set<String> userIds = new HashSet<>(commentLength);
		Set<Long> topCommentIds = new HashSet<>(commentLength);
		Map<String, UserSimpleNoneOtherVO> userMap = new HashMap<>(commentLength);
		Map<Long, List<Comment>> replyMap = new HashMap<>(commentLength);
		Map<String, Integer> likeFlagMap = new HashMap<>(commentLength);
		Map<Long,Long> likeCountMap = new HashMap<>(commentLength);
		
		for (int i = 0; i < commentLength; i++) {
			Comment comment = comments.get(i);
			userIds.add(comment.getCreateUserId().toString());
			topCommentIds.add(comment.getKid());
		}
		replyMap = getReply(topCommentIds);
		likeCountMap = getLikeCount(topCommentIds);
		
		if (StringUtils.isNoneBlank(commentFrontDTO.getUserId())) {
			likeFlagMap = likeService.getLikeFlagBatch(Lists.newArrayList(topCommentIds),
					NumberUtils.createLong(commentFrontDTO.getUserId()));
		}

		if (MapUtils.isNotEmpty(replyMap)) {
			userIds.addAll(getUserIdByReplyMap(replyMap));
		}
		
		userMap = userService.getUserInfo(userIds);
		List<CommentListInfoVO> commentVOs = new ArrayList<>();

		for (int i = 0; i < commentLength; i++) {
			Comment comment = comments.get(i);
			if(comment == null){
				continue;
			}
			CommentListInfoVO listInfoVO = CommentListInfoVO
					.parseByCommentSimple(parseCommentToCommentVO(comment, userMap));
			// 点赞状态
			if (MapUtils.isNotEmpty(likeFlagMap)) {
				listInfoVO.setLikeFlag(likeFlagMap.get(comment.getKid().toString()));
			}else{
				listInfoVO.setLikeFlag(LikeFlag.FALSE.getFlag());
			}

			// 获取回复总数
			Long replycount = commentCache.getCommentReplyCount(comment.getKid());
			replycount = replycount == null ? 0l : replycount;
			
			//设置点赞、评论总数
			listInfoVO.setCommentCount(replycount.intValue());
			if(MapUtils.isNotEmpty(likeCountMap)){
				listInfoVO.setLikeCount(likeCountMap.get(comment.getKid()));
			}else{
				listInfoVO.setLikeCount(0);
			}
			
			List<Comment> replys = replyMap.get(comment.getKid());
			// 聚合评论回复
			if (CollectionUtils.isNotEmpty(replys)) {
				List<CommentSimpleVO> replyVOs = new ArrayList<>(replys.size());
				for (int j = 0; j < replys.size(); j++) {
					Comment reply = replys.get(j);
					CommentSimpleVO replyVo = parseCommentToCommentVO(reply, userMap);
					replyVOs.add(replyVo);
				}
				listInfoVO.setReplyVos(replyVOs);
			}
			commentVOs.add(listInfoVO);
		}
		pageList.setEntities(commentVOs);
		return pageList;
	}

	@Override
	@Transactional
	public int updownBatch(List<Comment> comments) {
		for (Comment comment : comments) {
			updownSingle(comment);
		}
		return comments.size();
	}

	@Override
	public PageList<CommentVOForAdmin> queryCommentForAdmin(CommentDTO commentDTO) {
		Page<CommentVOForAdmin> page = PageHelper.startPage(commentDTO.getCurrentPage().intValue(),
				commentDTO.getPageSize().intValue());
		List<CommentVOForAdmin> commentVOForAdmins = commentDao.queryCommentForAdmin(commentDTO);
		PageList<CommentVOForAdmin> pageList = new PageList<>();
		pageList.setCurrentPage(commentDTO.getCurrentPage());
		pageList.setPageSize(commentDTO.getPageSize());
		pageList.setEntities(commentVOForAdmins);
		pageList.setCount(page.getTotal());
		return pageList;
	}

	@Override
	public CommentDetailVO queryCommentDetail(CommentSubDTO commentSubDTO) {
		Comment comment = getComment(commentSubDTO.getKid());
		if (comment == null) {
			return null;
		}
		CommentFrontDTO commentFrontDTO = new CommentFrontDTO();
		commentFrontDTO.setCurrentPage(commentSubDTO.getCurrentPage());
		commentFrontDTO.setPageSize(commentSubDTO.getPageSize());
		commentFrontDTO.setTopId(commentSubDTO.getKid());
		commentFrontDTO.setUserId(commentSubDTO.getUserId());
		
		//查询回复
		List<Comment> replys = getComment(commentFrontDTO);
		
		CommentDetailVO detailVO = new CommentDetailVO();
		Set<String> userIds = Sets.newHashSet(comment.getCreateUserId().toString());
		Map<String, UserSimpleNoneOtherVO> userMap = new HashMap<>();
		int replyLength = 0;
		if (CollectionUtils.isNotEmpty(replys)) {
			replyLength = replys.size();
			for (int i = 0; i < replyLength; i++) {
				userIds.add(replys.get(i).getCreateUserId().toString());
			}
		}
		
		//查询用户信息
		userMap = userService.getUserInfo(userIds);
		detailVO = CommentDetailVO.parseByCommentSimple(parseCommentToCommentVO(comment, userMap));
		
		//查询回复总数
		Long totalReply = commentCache.getCommentReplyCount(comment.getKid());
		totalReply = totalReply == null ? 0 : totalReply;
		detailVO.setCommentCount(totalReply.intValue());

		// 点赞状态
		if (StringUtils.isNoneBlank(commentFrontDTO.getUserId())) {
			Like like = new Like();
			like.setResourceId(comment.getKid());
			like.setUserId(NumberUtils.createLong(commentFrontDTO.getUserId()));
			detailVO.setLikeFlag(likeService.isLike(like));
		}

		// 设置点赞总数
		Long likeCount = countService.getCount(comment.getKid().toString(), BehaviorEnum.Like.getCode(), "-1");
		detailVO.setLikeCount(likeCount == null ? 0 : likeCount);

		if (CollectionUtils.isNotEmpty(replys)) {
			List<CommentSimpleVO> replyVOs = new ArrayList<>(replys.size());
			for (int j = 0; j < replyLength; j++) {
				Comment reply = replys.get(j);
				CommentSimpleVO replyVo = parseCommentToCommentVO(reply, userMap);
				replyVOs.add(replyVo);
			}
			PageList<CommentSimpleVO> pageList = new PageList<>(commentSubDTO.getCurrentPage(),
					commentSubDTO.getPageSize(), replyVOs,totalReply);
			detailVO.setReplys(pageList);
		}

		return detailVO;
	}

	@Override
	@Transactional
	public int updownSingle(Comment comment) {
		Comment localComment = getComment(comment.getKid());
		if(localComment == null){
			throw QuanhuException.busiError("评论不存在");
		}
		if(localComment.getShelveFlag() == 11 || localComment.getDelFlag() == 11){
			logger.info("[updownSingle]:comment:{} is updown",JsonUtils.toFastJson(localComment));
			return 0;
		}
		localComment.setLastUpdateUserId(comment.getLastUpdateUserId());
		int result = commentDao.updownSingle(localComment);

		// 刪除缓存评论列表
		commentCache.delCommentList(localComment);
		// 删除缓存评论
		commentCache.deleteComment(comment.getKid());

		// 评论总数-1
		countService.commitCount(BehaviorEnum.Comment, String.valueOf(localComment.getResourceId()), "-1", -1l);

		if (localComment.getTopId() != 0l) {
			// 删除回复
			delReplyComment(localComment);
		}else{
			//发消息
			messageService.commentUpdownSendMsg(Lists.newArrayList(localComment));
		}
		return result;
	}

	/**
	 * 查询评论
	 * 
	 * @param commentId
	 * @return
	 */
	public Comment getComment(Long commentId) {
		Comment comment = commentCache.getComment(commentId);
		if (comment != null) {
			return comment;
		}
		comment = new Comment();
		comment.setKid(commentId);
		comment = commentDao.querySingleComment(comment);
		if (comment != null) {
			if(comment.getDelFlag() != 10 && comment.getShelveFlag() != 10){
				commentCache.saveComment(comment);
				commentCache.addCommentList(comment);
			}
		}
		return comment;
	}

	/**
	 * 获取评论回复
	 * 
	 * @param topCommentIds
	 * @return
	 */
	private Map<Long, List<Comment>> getReply(Set<Long> topCommentIds) {
		if (CollectionUtils.isEmpty(topCommentIds)) {
			return null;
		}
		int topCommendLength = topCommentIds.size();
		Map<Long, List<Comment>> map = new HashMap<>(topCommendLength);
		CommentFrontDTO commentFrontDTO = new CommentFrontDTO();
		commentFrontDTO.setCurrentPage(1);
		commentFrontDTO.setPageSize(3);
		for (Iterator<Long> iterator = topCommentIds.iterator(); iterator.hasNext();) {
			Long topCommentId = iterator.next();
			commentFrontDTO.setTopId(topCommentId);
			List<Comment> replys = getComment(commentFrontDTO);
			if (CollectionUtils.isNotEmpty(replys)) {
				map.put(topCommentId, replys);
			}
		}
		return map;
	}

	/**
	 * 查询评论列表
	 * 
	 * @param commentId
	 * @return
	 */
	private List<Comment> getComment(CommentFrontDTO commentFrontDTO) {
		List<Comment> comments = commentCache.listComment(commentFrontDTO.getResourceId(), commentFrontDTO.getTopId(),
				(commentFrontDTO.getCurrentPage() - 1) * commentFrontDTO.getPageSize(), commentFrontDTO.getPageSize());

		if (CollectionUtils.isNotEmpty(comments)) {
			return comments;
		}
		//查询回复直接查询全部回复写入缓存，查询评论按需查询
		if(commentFrontDTO.getTopId() == 0l){
			PageHelper.startPage(commentFrontDTO.getCurrentPage(), commentFrontDTO.getPageSize(),false);
		}
		
		comments = commentDao.listCommentByParams(commentFrontDTO);
		if (CollectionUtils.isNotEmpty(comments)) {
			commentCache.saveComment(comments);
			commentCache.addCommentList(comments);
		}
		
		//返回回复数据
		if(commentFrontDTO.getTopId() != 0l && CollectionUtils.isNotEmpty(comments)){
			int length = comments.size();
			return length > commentFrontDTO.getPageSize() ? comments.subList(0, commentFrontDTO.getPageSize()) : comments.subList(0, length);
		}
		return comments;
	}

	/**
	 * 根据评论回复集合得到全部用户id
	 * 
	 * @param map
	 * @return
	 */
	private Set<String> getUserIdByReplyMap(Map<Long, List<Comment>> map) {
		if (MapUtils.isEmpty(map)) {
			return null;
		}
		Set<String> userIds = new HashSet<>();
		for (Map.Entry<Long, List<Comment>> entry : map.entrySet()) {
			List<Comment> comments = entry.getValue();
			if (CollectionUtils.isNotEmpty(comments)) {
				for (int i = 0; i < comments.size(); i++) {
					Comment comment = comments.get(i);
					if(comment != null){
						userIds.add(comment.getCreateUserId().toString());
						if (comment.getParentUserId() != 0) {
							userIds.add(String.valueOf(comment.getParentUserId()));
						}
					}
				}
			}
		}
		return userIds;
	}

	/**
	 * 转换comment对象为返回的vo
	 * 
	 * @param comment
	 * @param userMap
	 * @return
	 */
	public static CommentSimpleVO parseCommentToCommentVO(Comment comment, Map<String, UserSimpleNoneOtherVO> userMap) {
		CommentSimpleVO simpleVO = new CommentSimpleVO();
		simpleVO.setKid(comment.getKid());
		simpleVO.setTopId(comment.getTopId());
		simpleVO.setContentComment(comment.getContentComment());
		simpleVO.setCoterieId(comment.getCoterieId());
		simpleVO.setModuleEnum(comment.getModuleEnum());
		simpleVO.setParentId(comment.getParentId());
		simpleVO.setParentUserId(comment.getParentUserId());
		simpleVO.setResourceId(comment.getResourceId());
		simpleVO.setTargetUserId(comment.getTargetUserId());
		simpleVO.setCreateTime(comment.getCreateDate().getTime());
		UserSimpleNoneOtherVO noneOtherVO = userMap.get(comment.getCreateUserId().toString());
		if (noneOtherVO == null) {
			noneOtherVO = new UserSimpleNoneOtherVO();
			noneOtherVO.setUserId(comment.getCreateUserId());
		}
		simpleVO.setUser(noneOtherVO);
		// 设置被评论人用户信息
		if (simpleVO.getParentUserId() != 0l) {
			UserSimpleNoneOtherVO otherVO = userMap.get(String.valueOf(comment.getParentUserId()));
			simpleVO.setParentUser(otherVO);
		}
		return simpleVO;
	}
	
	/**
	 * 获取点赞数量
	 * @param topCommentIds
	 * @return
	 */
	private Map<Long,Long> getLikeCount(Set<Long> topCommentIds){
		if(CollectionUtils.isEmpty(topCommentIds)){
			return null;
		}
		Map<Long,Long> countMap = new HashMap<>(topCommentIds.size());
		for(Iterator<Long> iterator = topCommentIds.iterator();iterator.hasNext();){
			Long commentId = iterator.next();
			Long count = countService.getCount(commentId.toString(),  BehaviorEnum.Like.getCode(), "-1");
			countMap.put(commentId, count == null ? 0: count);
		}
		return countMap;
	}
}
