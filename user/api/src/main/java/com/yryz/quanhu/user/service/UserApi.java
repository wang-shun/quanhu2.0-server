package com.yryz.quanhu.user.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.pagehelper.Page;
import com.yryz.common.response.Response;
import com.yryz.quanhu.user.contants.RedisConstants;
import com.yryz.quanhu.user.dto.AdminUserInfoDTO;
import com.yryz.quanhu.user.vo.UserBaseInfoVO;
import com.yryz.quanhu.user.vo.UserLoginSimpleVO;
import com.yryz.quanhu.user.vo.UserSimpleVO;
/**
 * 用户基础信息服务
 * @author danshiyu
 *
 */
public interface UserApi {
	/**
	 * 用户信息key
	 * @param userId
	 * @return
	 */
	static String userInfoKey(String userId){
		return String.format("%s.%s", RedisConstants.USER_INFO,userId);
	}
	/**
	 * 查询用户简要信息
	 * @param userId
	 * @return
	 */
	Response<UserSimpleVO> getUserSimple(String userId);
	/**
	 * 查询用户简要信息并聚合关系信息<br/>
	 * userId不为空表示会聚合关系信息
	 * @param userId
	 * @param friendId 好友id
	 * @return
	 */
	Response<UserSimpleVO> getUserSimple(String userId,String friendId);
	/**
	 * 查询用户简要信息
	 * @param userIds 用户id
	 * @return
	 */
	Response<Map<String,UserSimpleVO>> getUserSimple(Set<String> userIds);
	/**
	 * 查询用户简要信息<br/>
	 * userId不为空表示查询好友的信息会聚合好友信息
	 * @param firendIds 好友id
	 * @param userId 用户id
	 * @return
	 */
	Response<Map<String,UserSimpleVO>> getUserSimple(String userId,Set<String> firendIds);
	/**
	 * 用户登录返回的用户信息
	 * @param userId
	 * @return
	 */
	Response<UserLoginSimpleVO> getUserLoginSimpleVO(String userId);
	/**
	 * 用户登录返回的用户信息<br/>
	 * friendId不为空表示会聚合关系信息
	 * @param userId
	 * @param friendId 好友id
	 * @return
	 */
	Response<UserLoginSimpleVO> getUserLoginSimpleVO(String userId,String friendId);
	
	/**
	 * 根据电话检索用户简要信息
	 * 
	 * @param phone
	 * @param appId 应用id
	 * @return
	 */
	Response<UserSimpleVO> getUserSimpleByPhone(String phone,String appId);

	
	/**
	 * 根据电话检索用户简要信息
	 * 
	 * @param phones
	 * @param appId 应用id
	 * @return
	 */
	Response<Map<String, UserSimpleVO>> getUserSimpleByPhone(Set<String> phones,String appId);
	
	/**
	 * 查询用户基本信息
	 * @param userIds
	 * @return
	 */
	Response<Map<String,UserBaseInfoVO>> getUser(Set<String> userIds);
	
	/**
	 * 根据手机号、昵称、注册时间模糊查询用户id
	 * @param custInfoDTO
	 * @return 昵称需要加特殊字符转义
	 */
	Response<List<String>> getUserIdByParams(AdminUserInfoDTO custInfoDTO);

	/**
	 * 获取用户推送设备号
	 * @param userId
	 * @return
	 */
	Response<String> getDeviceIdByUserId(String userId);
	
	/**
	 * 获取用户推送设备号
	 * @param userId
	 * @return
	 */
	Response<List<String>> getDeviceIdByUserId(List<String> userId);
	
	/**
	 * 管理后台查询用户列表
	 * @param pageNo
	 * @param pageSize
	 * @param custInfoDTO
	 * @return
	 * @Description 昵称需要加特殊字符转义
	 */
	Response<Page<UserBaseInfoVO>> listUserInfo(int pageNo,int pageSize,AdminUserInfoDTO custInfoDTO);

}