package com.yryz.quanhu.user.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.GsonUtils;
import com.yryz.common.utils.StringUtils;
import com.yryz.quanhu.user.dto.UserTagDTO;
import com.yryz.quanhu.user.service.UserTagApi;
import com.yryz.quanhu.user.service.UserTagService;
import com.yryz.quanhu.user.vo.UserTagVO;

@Service(interfaceClass=UserTagApi.class)
public class UserTagProvider implements UserTagApi{
	private static final Logger logger = LoggerFactory.getLogger(UserTagProvider.class);
	
	@Autowired
	private UserTagService tagService;
	
	@Override
	public Response<Boolean> batchSaveUserTag(UserTagDTO tagDTO){
		try {
			checkUserTagDTO(tagDTO);
			tagService.batch(tagDTO);
			return ResponseUtils.returnObjectSuccess(true);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("[tag.batchSaveUserTag]", e);
			return ResponseUtils.returnException(e);
		}
	}
	
	private void checkUserTagDTO(UserTagDTO tagDTO){
		if(tagDTO == null || tagDTO.getUserId() == null){
			throw QuanhuException.busiError("用户id不能为空");
		}
		if(tagDTO.getTagType() == null){
			throw QuanhuException.busiError("标签类型不能为空");
		}
		if(ArrayUtils.isEmpty(StringUtils.split(tagDTO.getTagIds(), ","))){
			throw QuanhuException.busiError("标签id不能为空");
		}
	}

	@Override
	public Response<List<String>> getTags(Long userId, Integer tagType) {
		try {
			if(userId == null){
				throw QuanhuException.busiError("用户id不能为空");
			}
			if(tagType == null){
				throw QuanhuException.busiError("标签类型不能为空");
			}
			
			return ResponseUtils.returnListSuccess(tagService.getTagByUserId(userId, tagType));
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("[tag.batchSaveUserTag]", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Map<Long, List<UserTagVO>>> getUserTags(List<Long> userIds) {
		try {
			Map<Long, List<UserTagVO>> userTags = tagService.getUserTags(userIds);
			logger.info("getUserTags result: {}", GsonUtils.parseJson(userTags));
			return ResponseUtils.returnObjectSuccess(userTags);
		} catch (Exception e) {
			logger.error("getUserTags error", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Map<String, Long>> getTagCountByUser(Set<String> tagIds) {
		try {
			logger.info("getTagCountByUser start: {}", JSON.toJSON(tagIds));

			Map<String,Long> tagMap = tagService.getTagCountByUser(tagIds);
			logger.info("getTagCountByUser finish: {}", JSON.toJSON(tagMap));
			return ResponseUtils.returnObjectSuccess(tagMap);
		} catch (Exception e) {
			logger.error("getTagCountByUser error", e);
			return ResponseUtils.returnException(e);
		}
	}

}
