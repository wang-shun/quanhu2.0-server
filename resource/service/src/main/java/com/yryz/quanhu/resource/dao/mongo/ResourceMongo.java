/**
 * Copyright (c) 2018-2019 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * 
 * Created on 2018年1月16日
 * Id: ResourceMongo.java, 2018年1月16日 下午4:21:13 yehao
 */
package com.yryz.quanhu.resource.dao.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.yryz.common.mongodb.AbsBaseMongoDAO;
import com.yryz.common.utils.DateUtils;
import com.yryz.common.utils.GsonUtils;
import com.yryz.quanhu.resource.entity.ResourceModel;
import com.yryz.quanhu.resource.enums.ResourceEnum;

/**
 * @author yehao
 * @version 2.0
 * @date 2018年1月16日 下午4:21:13
 * @Description 资源管理的mongo访问类
 */
@Service
public class ResourceMongo extends AbsBaseMongoDAO<ResourceModel> {
	
	/**
	 * mongoTemplate
	 * @param mongoTemplate
	 * @see com.yryz.common.mongodb.AbsBaseMongoDAO#setMongoTemplate(org.springframework.data.mongodb.core.MongoTemplate)
	 */
	@Override
	protected void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	/**
	 * 保存对象
	 * @param resourceModel
	 * @return
	 * @see com.yryz.common.mongodb.AbsBaseMongoDAO#save(java.lang.Object)
	 */
	public ResourceModel save(ResourceModel resourceModel){
		//设置默认值
		if(resourceModel.getTalentType() == null){
			resourceModel.setTalentType(ResourceEnum.TALENT_TYPE_FALSE);
		}
		if(resourceModel.getHeat() == null){
			resourceModel.setHeat(0L);
		}
		if(resourceModel.getCreateTime() == null){
			resourceModel.setCreateTime(System.currentTimeMillis());
		}
		if(resourceModel.getUpdateTime() == null){
			resourceModel.setUpdateTime(System.currentTimeMillis());
		}
		if(resourceModel.getPublicState() == null){
			resourceModel.setPublicState(ResourceEnum.PUBLIC_STATE_FALSE);
		}
		if(resourceModel.getRecommend() == null){
			resourceModel.setRecommend(ResourceEnum.RECOMMEND_TYPE_FALSE);
		}
		if(resourceModel.getDelFlag() == null){
			resourceModel.setDelFlag(ResourceEnum.DEL_FLAG_FALSE);
		}
		if(resourceModel.getDelFlag() == null){
			resourceModel.setDelFlag(ResourceEnum.DEL_FLAG_FALSE);
		}
		if(resourceModel.getSort() == null){
			resourceModel.setSort(0L);
		}
		if(resourceModel.getCoterieSort() == null){
			resourceModel.setCoterieSort(0L);
		}
		return super.save(resourceModel);
	}
	
	/**
	 * 更新对象
	 * @param resourceModel
	 * @return
	 */
	public ResourceModel update(ResourceModel resourceModel){
		Query query = new Query();
		query.addCriteria(Criteria.where("resourceId").is(resourceModel.getResourceId()));
		String obj = GsonUtils.parseJson(resourceModel);
		JSONObject json = new JSONObject(obj);
		Set<String> keys = json.keySet();
		Update update = new Update();
		for (String key : keys) {
			update.set(key, json.get(key));
		}
		mongoTemplate.updateMulti(query, update, ResourceModel.class);
		return null;
	}
	
	/**
	 * 获取唯一对象
	 * @param resourceId
	 * @return
	 */
	public ResourceModel get(String resourceId){
		Query query = new Query();
		query.addCriteria(Criteria.where("resourceId").is(resourceId));
		return findOne(query);
	}
	
	/**
	 * 获取唯一对象
	 * @param resourceModel
	 * @return
	 */
	public ResourceModel get(ResourceModel resourceModel){
		Query query = new Query();
		query.addCriteria(Criteria.where("resourceId").is(resourceModel.getResourceId()));
		return findOne(query);
	}
	
	/**
	 * 删除资源库，传对象
	 * @param resourceModel
	 */
	public void delete(ResourceModel resourceModel){
		delete(resourceModel.getResourceId());
	}
	
	/**
	 * 删除资源库，通过ID
	 * @param resourceId
	 */
	public void delete(String resourceId){
		Query query = new Query();
		query.addCriteria(Criteria.where("resourceId").is(resourceId));
		Update update = new Update();
		update.set("delFlag", ResourceEnum.DEL_FLAG_TRUE);
		mongoTemplate.updateMulti(query, update, ResourceModel.class);
	}
	
	/**
	 * 获取资源列表
	 * 多条件查询，支持 custId=查询，createTime组合条件查询，title模糊查询，resourceType的in查询
	 * 支持动态排序
	 * 支持start和limit分页
	 * @param resourceModel
	 * @param orderColumn
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<ResourceModel> getList(ResourceModel resourceModel , String orderColumn ,String startTime , String endTime,  int start , int limit){
		Query query = new Query();
		Criteria criteria = new Criteria();
		if(resourceModel != null){
			
			criteria = Criteria.where("delFlag").is(ResourceEnum.DEL_FLAG_FALSE);
			
			//资源ID，用户ID，是否达人，私圈ID
			if(resourceModel.getResourceId() != null){
				criteria = Criteria.where("resourceId").is(resourceModel.getResourceId()).andOperator(criteria);
			}
			if(resourceModel.getUserId() != null){
				criteria = Criteria.where("userId").is(resourceModel.getUserId()).andOperator(criteria);
			}
			if(StringUtils.isNotEmpty(resourceModel.getTalentType())){
				criteria = Criteria.where("talentType").is(resourceModel.getTalentType()).andOperator(criteria);
			}
			if("0".equals(resourceModel.getCoterieId())){
				criteria = Criteria.where("coterieId").is(null).andOperator(criteria);
			} else if("1".equals(resourceModel.getCoterieId())){
				criteria = Criteria.where("coterieId").ne(null).andOperator(criteria);
			} else if(StringUtils.isNotEmpty(resourceModel.getCoterieId())){
				criteria = Criteria.where("coterieId").is(resourceModel.getCoterieId()).andOperator(criteria);
			}
			
			if(null != resourceModel.getPrice()){
				long price = resourceModel.getPrice().longValue();
				if(price == 0L){ //为0，只看免费的
					criteria = Criteria.where("price").is(price).andOperator(criteria);
				} else if(price == -1L){ //为-1，只看收费的
					criteria = Criteria.where("price").gte(0L).andOperator(criteria);
				} else { //大于0，则表示是小于等于当前值
					criteria = Criteria.where("price").lte(resourceModel.getPrice()).andOperator(criteria);
				}
			}
			
			//标题，正文，简介模糊匹配
			if(StringUtils.isNotEmpty(resourceModel.getTitle())){
				criteria = Criteria.where("title").regex(resourceModel.getTitle()).andOperator(criteria);
			}
			if(StringUtils.isNotEmpty(resourceModel.getContent())){
				criteria = Criteria.where("content").regex(resourceModel.getContent()).andOperator(criteria);
			}
			
			//公开状态
			if(resourceModel.getPublicState() != null){
				criteria = Criteria.where("publicState").is(resourceModel.getPublicState()).andOperator(criteria);
			}
			
			//私密状态
			if(resourceModel.getIntimate() != null){
				criteria = Criteria.where("intimate").is(resourceModel.getIntimate()).andOperator(criteria);
			}
			
			//首页排序值
			if(resourceModel.getSort() != null){
				criteria = Criteria.where("sort").is(resourceModel.getSort()).andOperator(criteria);
			}
			
			//私圈排序值
			if(resourceModel.getCoterieSort() != null){
				criteria = Criteria.where("coterieSort").is(resourceModel.getCoterieSort()).andOperator(criteria);
			}
			
			//资源类型,多条件查询，resourceType支持多类型的枚举值，以,分隔
			if(StringUtils.isNotEmpty(resourceModel.getModuleEnum())){
				Criteria resouceTypeCriteria = Criteria.where("moduleEnum");
				String[] resourceTypes = resourceModel.getModuleEnum().split(",");
				if(resourceTypes != null && resourceTypes.length == 1){
					resouceTypeCriteria = resouceTypeCriteria.is(resourceTypes[0]);
				} else {
					List<String> resourceList = new ArrayList<>();
					for (int i = 0; i < resourceTypes.length; i++) {
						String resourceType = resourceTypes[i];
						resourceList.add(resourceType);
					}
					resouceTypeCriteria.in(resourceList);
				}
				criteria = resouceTypeCriteria.andOperator(criteria);
			}
			if(StringUtils.isNotEmpty(resourceModel.getRecommend())){
				criteria = Criteria.where("recommend").is(resourceModel.getRecommend()).andOperator(criteria);
			}
			
			if(StringUtils.isNotEmpty(startTime) || StringUtils.isNotEmpty(endTime)){
				Criteria createTimeCriteria = Criteria.where("createTime");
				if(startTime != null){
					Date dstartTime = DateUtils.parseDate(startTime);
					if(dstartTime != null){
						//大于等于开始时间
						createTimeCriteria = createTimeCriteria.gte(dstartTime.getTime());
					}
				}
				if(endTime != null){
					Date dendTime = DateUtils.parseDate(endTime);
					if(dendTime != null){
						//大于等于结束时间
						createTimeCriteria = createTimeCriteria.lt(dendTime.getTime());
					}
				}
				criteria = createTimeCriteria.andOperator(criteria);
			}
			
		}
		
		query.addCriteria(criteria);
		
		if(StringUtils.isNotEmpty(orderColumn)){
			String[] orderColumns = orderColumn.split(",");
			List<Order> orders = new ArrayList<>();
			for (int i = 0; i < orderColumns.length; i++) {
				Order order = new Order(Direction.DESC, orderColumns[i]);
				orders.add(order);
			}
			Sort sort = new Sort(orders);
			query.with(sort);
		}
		query.skip(start);
		query.limit(limit);
		return find(query);
	}
	
	public long count(ResourceModel resourceModel ,String startTime , String endTime){
		Query query = new Query();
		Criteria criteria = new Criteria();
		if(resourceModel != null){
			
			criteria = Criteria.where("delFlag").is(ResourceEnum.DEL_FLAG_FALSE);
			
			//资源ID，用户ID，是否达人，私圈ID
			if(resourceModel.getResourceId() != null){
				criteria = Criteria.where("resourceId").is(resourceModel.getResourceId()).andOperator(criteria);
			}
			if(resourceModel.getUserId() != null){
				criteria = Criteria.where("userId").is(resourceModel.getUserId()).andOperator(criteria);
			}
			if(StringUtils.isNotEmpty(resourceModel.getTalentType())){
				criteria = Criteria.where("talentType").is(resourceModel.getTalentType()).andOperator(criteria);
			}
			if("0".equals(resourceModel.getCoterieId())){
				criteria = Criteria.where("coterieId").is(null).andOperator(criteria);
			} else if("1".equals(resourceModel.getCoterieId())){
				criteria = Criteria.where("coterieId").ne(null).andOperator(criteria);
			} else if(StringUtils.isNotEmpty(resourceModel.getCoterieId())){
				criteria = Criteria.where("coterieId").is(resourceModel.getCoterieId()).andOperator(criteria);
			}
			
			//标题，正文，简介模糊匹配
			if(StringUtils.isNotEmpty(resourceModel.getTitle())){
				criteria = Criteria.where("title").regex(resourceModel.getTitle()).andOperator(criteria);
			}
			if(StringUtils.isNotEmpty(resourceModel.getContent())){
				criteria = Criteria.where("content").regex(resourceModel.getContent()).andOperator(criteria);
			}
			
			//公开状态
			if(resourceModel.getPublicState() != null){
				criteria = Criteria.where("publicState").is(resourceModel.getPublicState()).andOperator(criteria);
			}
			
			//私密状态
			if(resourceModel.getIntimate() != null){
				criteria = Criteria.where("intimate").is(resourceModel.getIntimate()).andOperator(criteria);
			}
			
			//首页排序值
			if(resourceModel.getSort() != null){
				criteria = Criteria.where("sort").is(resourceModel.getSort()).andOperator(criteria);
			}
			
			//私圈排序值
			if(resourceModel.getCoterieSort() != null){
				criteria = Criteria.where("coterieSort").is(resourceModel.getCoterieSort()).andOperator(criteria);
			}
			
			//资源类型,多条件查询，resourceType支持多类型的枚举值，以,分隔
			if(StringUtils.isNotEmpty(resourceModel.getModuleEnum())){
				Criteria resouceTypeCriteria = Criteria.where("moduleEnum");
				String[] resourceTypes = resourceModel.getModuleEnum().split(",");
				if(resourceTypes != null && resourceTypes.length == 1){
					resouceTypeCriteria = resouceTypeCriteria.is(resourceTypes[0]);
				} else {
					List<String> resourceList = new ArrayList<>();
					for (int i = 0; i < resourceTypes.length; i++) {
						String resourceType = resourceTypes[i];
						resourceList.add(resourceType);
					}
					resouceTypeCriteria.in(resourceList);
				}
				criteria = resouceTypeCriteria.andOperator(criteria);
			}
			if(StringUtils.isNotEmpty(resourceModel.getRecommend())){
				criteria = Criteria.where("recommend").is(resourceModel.getRecommend()).andOperator(criteria);
			}
			
			if(StringUtils.isNotEmpty(startTime) || StringUtils.isNotEmpty(endTime)){
				Criteria createTimeCriteria = Criteria.where("createTime");
				if(startTime != null){
					Date dstartTime = DateUtils.parseDate(startTime);
					if(dstartTime != null){
						//大于等于开始时间
						createTimeCriteria = createTimeCriteria.gte(dstartTime.getTime());
					}
				}
				if(endTime != null){
					Date dendTime = DateUtils.parseDate(endTime);
					if(dendTime != null){
						//大于等于结束时间
						createTimeCriteria = createTimeCriteria.lt(dendTime.getTime());
					}
				}
				criteria = createTimeCriteria.andOperator(criteria);
			}
			
		}
		
		query.addCriteria(criteria);
		
		return mongoTemplate.count(query, ResourceModel.class);
	}


}
