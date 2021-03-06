/**
 * Copyright (c) 2018-2019 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * 
 * Created on 2018年1月16日
 * Id: ResourceService.java, 2018年1月16日 下午4:25:27 yehao
 */
package com.yryz.quanhu.resource.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yryz.quanhu.resource.entity.ResourceModel;

/**
 * @author yehao
 * @version 2.0
 * @date 2018年1月16日 下午4:25:27
 * @Description 资源管理服务接口声明
 */
public interface ResourceService {
	
	/**
	 * 创建/更新核心资源信息
	 * @param resources 资源信息实体
	 */
	public void commitResource(List<ResourceModel> resources);
	
	/**
	 * 更新用户资源
	 * @param resources
	 */
	public void updateResource(List<ResourceModel> resources);
	
	/**
	 * 删除资源
	 * @param resources
	 */
	public void deleteResource(List<ResourceModel> resources);
	
	/**
	 * 资源列表获取,如果有排序字段，默认按字段倒序排序
	 * @param resource 资源查询条件
	 * @param orderColumn 排序字段，resource的columnName，如果有多个值，以逗号分隔。默认倒序排序
	 * @param start 开始条目
	 * @param limit 结束条目
	 * @param startTime 开始时间，yyyy-MM-dd HH:mm:ss
	 * @param endTime 结束时间，yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public List<ResourceModel> getResources(ResourceModel resource , String orderColumn , int start , int limit ,String startTime ,String endTime);
	
	/**
	 * 批量查询资源信息
	 * @param resourceIds
	 * @return
	 */
	public Map<String, ResourceModel> getResources(Set<String> resourceIds);
	
	/**
	 * 单一查询资源信息
	 * @param resourceId
	 * @return
	 */
	public ResourceModel getResource(String resourceId);
	
	/**
	 * 创建首页缓存
	 */
	public void createRecommend();
	
	/**
	 * app首页推荐
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<ResourceModel> appRecommend(int start , int limit);
	
	/**
	 * 统计用户信息
	 * @param resource
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Long count(ResourceModel resource ,String startTime ,String endTime);

}
