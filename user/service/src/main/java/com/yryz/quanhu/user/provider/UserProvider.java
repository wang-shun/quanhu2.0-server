package com.yryz.quanhu.user.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.yryz.common.exception.MysqlOptException;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.exception.RedisOptException;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.StringUtils;
import com.yryz.quanhu.user.dto.AdminUserInfoDTO;
import com.yryz.quanhu.user.service.UserApi;
import com.yryz.quanhu.user.service.UserService;
import com.yryz.quanhu.user.vo.UserBaseInfoVO;
import com.yryz.quanhu.user.vo.UserLoginSimpleVO;
import com.yryz.quanhu.user.vo.UserSimpleVO;
/**
 * 用户基础信息服务
 * @author danshiyu
 *
 */
@Service(interfaceClass=UserApi.class)
public class UserProvider implements UserApi{
	private static final Logger logger = LoggerFactory.getLogger(UserProvider.class);
	
	@Autowired
	private UserService userService;
	
	@Override
	public Response<UserSimpleVO> getUserSimple(String userId) {
		try {
			if(StringUtils.isBlank(userId)){
				throw QuanhuException.busiError("userId不能为空");
			}
			return ResponseUtils.returnObjectSuccess(userService.getUserSimple(userId));
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<UserSimpleVO> getUserSimple(String userId, String friendId) {
		try {
			if(StringUtils.isBlank(friendId)){
				throw QuanhuException.busiError("friendId不能为空");
			}
			UserSimpleVO simpleVO = userService.getUserSimple(friendId);
			
			if(StringUtils.isBlank(userId)){
				// TODO:聚合关系数据
			}
			
			return ResponseUtils.returnObjectSuccess(simpleVO);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Map<String, UserSimpleVO>> getUserSimple(Set<String> userIds) {
		try {
			if(CollectionUtils.isEmpty(userIds)){
				throw QuanhuException.busiError("userIds不能为空");
			}
			Map<String, UserSimpleVO> map = userService.getUserSimple(userIds);
			
			return ResponseUtils.returnObjectSuccess(map);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Map<String, UserSimpleVO>> getUserSimple(String userId, Set<String> firendIds) {
		try {
			if(CollectionUtils.isEmpty(firendIds)){
				throw QuanhuException.busiError("userIds不能为空");
			}
			Map<String, UserSimpleVO> map = userService.getUserSimple(firendIds);
			if(StringUtils.isBlank(userId)){
				// TODO:聚合关系数据
			}
			return ResponseUtils.returnObjectSuccess(map);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<UserLoginSimpleVO> getUserLoginSimpleVO(String userId) {
		try {
			if(StringUtils.isEmpty(userId)){
				throw QuanhuException.busiError("userId不能为空");
			}
			UserLoginSimpleVO loginSimpleVO = userService.getUserLoginSimpleVO(userId);
			return ResponseUtils.returnObjectSuccess(loginSimpleVO);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<UserLoginSimpleVO> getUserLoginSimpleVO(String userId, String friendId) {
		try {
			if(StringUtils.isEmpty(friendId)){
				throw QuanhuException.busiError("friendId不能为空");
			}
			UserLoginSimpleVO loginSimpleVO = userService.getUserLoginSimpleVO(userId);
			if(StringUtils.isBlank(userId)){
				// TODO:聚合关系数据
			}
			return ResponseUtils.returnObjectSuccess(loginSimpleVO);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<UserSimpleVO> getUserSimpleByPhone(String phone, String appId) {
		try {
			if(StringUtils.isEmpty(phone)){
				throw QuanhuException.busiError("phone不能为空");
			}
			if(StringUtils.isEmpty(appId)){
				throw QuanhuException.busiError("appId不能为空");
			}
			UserSimpleVO UserSimpleVO = userService.getUserSimpleByPhone(phone, appId);
			return ResponseUtils.returnObjectSuccess(UserSimpleVO);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Map<String, UserSimpleVO>> getUserSimpleByPhone(Set<String> phones, String appId) {
		try {
			if(CollectionUtils.isEmpty(phones)){
				throw QuanhuException.busiError("phones不能为空");
			}
			if(StringUtils.isEmpty(appId)){
				throw QuanhuException.busiError("appId不能为空");
			}
			Map<String, UserSimpleVO> map = userService.getUserSimpleByPhone(phones, appId);
			return ResponseUtils.returnObjectSuccess(map);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Map<String, UserBaseInfoVO>> getUser(Set<String> userIds) {
		try {
			if(CollectionUtils.isEmpty(userIds)){
				throw QuanhuException.busiError("userIds不能为空");
			}
			Map<String, UserBaseInfoVO> map = userService.getUser(userIds);
			return ResponseUtils.returnObjectSuccess(map);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<List<String>> getUserIdByParams(AdminUserInfoDTO custInfoDTO) {
		try {
			List<String> list = userService.getUserIdByParams(custInfoDTO);
			return ResponseUtils.returnObjectSuccess(list);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<String> getDeviceIdByUserId(String userId) {
		try {
			String devId = userService.getDeviceIdByUserId(userId);
			return ResponseUtils.returnObjectSuccess(devId);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<List<String>> getDeviceIdByUserId(List<String> userIds) {
		try {
			List<String> devIds = userService.getDeviceIdByUserId(userIds);
			return ResponseUtils.returnObjectSuccess(devIds);
		} catch (MysqlOptException e) {
			return ResponseUtils.returnException(e);
		} catch (RedisOptException e) {
			return ResponseUtils.returnException(e);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("用户信息查询异常", e);
			return ResponseUtils.returnException(e);
		}
	}

	@Override
	public Response<Page<UserBaseInfoVO>> listUserInfo(int pageNo, int pageSize, AdminUserInfoDTO custInfoDTO) {
		// TODO Auto-generated method stub
		return null;
	}
	
}