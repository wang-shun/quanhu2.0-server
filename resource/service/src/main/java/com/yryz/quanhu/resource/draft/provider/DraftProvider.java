/**
 * Copyright (c) 2018-2019 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * 
 * Created on 2018年2月6日
 * Id: DraftProvider.java, 2018年2月6日 下午5:14:00 yehao
 */
package com.yryz.quanhu.resource.draft.provider;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.yryz.common.constant.CommonConstants;
import com.yryz.common.constant.ModuleContants;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.DateUtils;
import com.yryz.common.utils.JsonUtils;
import com.yryz.quanhu.behavior.count.api.CountApi;
import com.yryz.quanhu.behavior.count.contants.BehaviorEnum;
import com.yryz.quanhu.behavior.read.api.ReadApi;
import com.yryz.quanhu.resource.api.ResourceApi;
import com.yryz.quanhu.resource.api.ResourceDymaicApi;
import com.yryz.quanhu.resource.draft.api.DraftApi;
import com.yryz.quanhu.resource.draft.service.DraftService;
import com.yryz.quanhu.resource.enums.ResourceEnum;
import com.yryz.quanhu.resource.release.config.service.ReleaseConfigService;
import com.yryz.quanhu.resource.release.config.vo.ReleaseConfigVo;
import com.yryz.quanhu.resource.release.constants.ReleaseConstants;
import com.yryz.quanhu.resource.release.info.dto.ReleaseInfoDto;
import com.yryz.quanhu.resource.release.info.entity.ReleaseInfo;
import com.yryz.quanhu.resource.release.info.provider.ReleaseInfoProvider;
import com.yryz.quanhu.resource.release.info.vo.ReleaseInfoVo;
import com.yryz.quanhu.resource.vo.ResourceTotal;
import com.yryz.quanhu.score.service.EventAPI;
import com.yryz.quanhu.support.id.api.IdAPI;
import com.yryz.quanhu.user.service.UserApi;
import com.yryz.quanhu.user.vo.UserSimpleVO;

/**
 * @author yehao
 * @version 2.0
 * @date 2018年2月6日 下午5:14:00
 * @Description 草稿箱API
 */
@Service(interfaceClass=DraftApi.class)
public class DraftProvider implements DraftApi {
	
    private Logger logger = LoggerFactory.getLogger(ReleaseInfoProvider.class);

    @Autowired
    private ReleaseConfigService releaseConfigService;

    @Autowired
    private DraftService draftService;

    @Reference
    private UserApi userApi;

    @Reference
    private IdAPI idAPI;

    @Reference
    private CountApi countApi;

    @Reference
    private ResourceDymaicApi resourceDymaicApi;

    @Reference
    private ResourceApi resourceApi;

    @Reference
    private EventAPI eventAPI;
    
    @Reference
    private ReadApi readApi;

	/**
	 * 发布
	 * @param record
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#release(com.yryz.quanhu.resource.release.info.entity.ReleaseInfo)
	 */
	@Override
	public Response<ReleaseInfo> release(ReleaseInfo record) {
		try {
            Assert.hasText(record.getContentSource(), "ContentSource is NULL !");

            record.setClassifyId(ReleaseConstants.APP_DEFAULT_CLASSIFY_ID);
            record.setDelFlag(CommonConstants.DELETE_NO);
            record.setShelveFlag(CommonConstants.SHELVE_YES);

            Assert.isNull(record.getCoterieId(), "平台文章发布 没有：CoterieId");
            Assert.isTrue(null == record.getContentPrice() || record.getContentPrice() == 0L,
                    "平台文章发布 不能设置付费：ContentPrice");

            // 校验用户是否存在
            UserSimpleVO createUser = ResponseUtils.getResponseData(userApi.getUserSimple(record.getCreateUserId()));
            Assert.notNull(createUser, "发布者用户不存在！userId：" + record.getCreateUserId());

            ReleaseConfigVo cfgVo = releaseConfigService.getTemplate(ReleaseConstants.APP_DEFAULT_CLASSIFY_ID);
            Assert.notNull(cfgVo, "平台发布文章，发布模板不存在！classifyId：" + ReleaseConstants.APP_DEFAULT_CLASSIFY_ID);

            // 校验模板
            Assert.isTrue(draftService.releaseInfoCheck(record, cfgVo), "平台发布文章，参数校验不通过！");

            // 用户输入时至少有一个（富文本）元素不为空
            if (StringUtils.isEmpty(record.getContent()) && StringUtils.isEmpty(record.getImgUrl())
                    && StringUtils.isEmpty(record.getVideoUrl()) && StringUtils.isEmpty(record.getAudioUrl())) {
                throw QuanhuException.busiError("富文本元素(文字、图片、视频、音频)至少有一个不能为空！");
            }

            // kid 生成
            record.setKid(ResponseUtils.getResponseData(idAPI.getSnowflakeId()));
            draftService.insertSelective(record);

//            // 资源进聚合、进好友动态
//            this.commitResourceAndDynamic(record, createUser);
//            try {
//                // 接入统计计数
//                countApi.commitCount(BehaviorEnum.Release, record.getCreateUserId(), null, 1L);
//            } catch (Exception e) {
//                logger.error("统计计数 接入异常！", e);
//            }
//
//            // 对接积分事件
//            draftService.commitEvent(eventAPI, record);

            return ResponseUtils.returnObjectSuccess(record);

        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("平台发布文章异常！", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 详情
	 * @param kid
	 * @param headerUserId
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#infoByKid(java.lang.Long, java.lang.Long)
	 */
	@Override
	public Response<ReleaseInfoVo> infoByKid(Long kid, Long headerUserId) {
		try {
            ReleaseInfoVo infoVo = draftService.selectByKid(kid);
            Assert.notNull(infoVo, "文章不存在！kid:" + kid);

            Assert.isTrue(0L == infoVo.getCoterieId(), "非平台文章禁止访问！");

            // 创建者用户信息
            UserSimpleVO createUser = ResponseUtils.getResponseData(userApi.getUserSimple(infoVo.getCreateUserId()));
            Assert.notNull(createUser, "文章创建者用户不存在！userId:" + infoVo.getCreateUserId());
            infoVo.setUser(createUser);

            // 若资源已删除、或者 已经下线 直接返回
            if (CommonConstants.DELETE_YES.equals(infoVo.getDelFlag())
                    || CommonConstants.SHELVE_NO.equals(infoVo.getShelveFlag())) {
                draftService.resourcePropertiesEmpty(infoVo);

                return ResponseUtils.returnObjectSuccess(infoVo);
            }

//            try {
//                // 接入阅读统计计数
//                readApi.read(infoVo.getKid(), infoVo.getCreateUserId());
//            } catch (Exception e) {
//                logger.error("阅读统计计数 接入异常！", e);
//            }

            return ResponseUtils.returnObjectSuccess(infoVo);
        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("获取平台文章详情异常！", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 分页查询
	 * @param dto
	 * @param headerUserId
	 * @param isCount
	 * @param isGetCreateUser
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#pageByCondition(com.yryz.quanhu.resource.release.info.dto.ReleaseInfoDto, java.lang.Long, boolean, boolean)
	 */
	@Override
	public Response<PageList<ReleaseInfoVo>> pageByCondition(ReleaseInfoDto dto, Long headerUserId, boolean isCount,
			boolean isGetCreateUser) {
		try {
            if (null == dto.getOrderType()) {
                dto.setOrderType(ReleaseConstants.OrderType.time_new);
            }
            
            PageList<ReleaseInfoVo> pageList = draftService.pageByCondition(dto, isCount);
            if (!isGetCreateUser || null == pageList || CollectionUtils.isEmpty(pageList.getEntities())) {
                return ResponseUtils.returnObjectSuccess(pageList);
            }

            List<ReleaseInfoVo> voList = pageList.getEntities();

            // 获取所有创建者用户ID
            Set<String> userIds = new HashSet<>();
            for (ReleaseInfoVo info : voList) {
                if (null == info || null == info.getCreateUserId()) {
                    continue;
                }
                userIds.add(String.valueOf(info.getCreateUserId()));
            }

            // 获取用户信息集合
            Map<String, UserSimpleVO> userMap = ResponseUtils.getResponseData(userApi.getUserSimple(userIds));
            if (null != userMap) {
                for (ReleaseInfoVo info : voList) {
                    if (null == info || null == info.getCreateUserId()) {
                        continue;
                    }
                    UserSimpleVO userVo = userMap.get(String.valueOf(info.getCreateUserId()));
                    info.setUser(userVo);
                }
            }

            return ResponseUtils.returnObjectSuccess(pageList);
        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("获取平台文章列表异常！", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 作者删除
	 * @param upInfo
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#deleteBykid(com.yryz.quanhu.resource.release.info.entity.ReleaseInfo)
	 */
	@Override
	public Response<Integer> deleteBykid(ReleaseInfo upInfo) {
		try {
            upInfo.setDelFlag(CommonConstants.DELETE_YES);

            // 校验记录是否存在
            ReleaseInfo info = draftService.selectByKid(upInfo.getKid());
            Assert.notNull(info, "文章资源不存在，kid：" + upInfo.getKid());

            Assert.isTrue(info.getCreateUserId().equals(upInfo.getLastUpdateUserId()), "操作用户非作者不能删除！");

            int result = draftService.updateByUkSelective(upInfo);
            Assert.isTrue(result > 0, "作者删除文章失败！");

//            try {
//                // 聚合资源下线
//                resourceApi.deleteResourceById(String.valueOf(upInfo.getKid()));
//            } catch (Exception e) {
//                logger.error("资源聚合、统计计数 接入异常！", e);
//            }
//
//            try {
//                // 接入统计计数
//                countApi.commitCount(BehaviorEnum.Release, upInfo.getCreateUserId(), null, -1L);
//            } catch (Exception e) {
//                logger.error("资源聚合、统计计数 接入异常！", e);
//            }

            return ResponseUtils.returnObjectSuccess(result);

        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("作者删除文章异常！", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 获取查询列表
	 * @param startDate
	 * @param endDate
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#getKidByCreatedate(java.lang.String, java.lang.String)
	 */
	@Override
	public Response<List<Long>> getKidByCreatedate(String startDate, String endDate) {
		try {
            ReleaseInfoDto dto = new ReleaseInfoDto();
            dto.setBeginDate(startDate);
            dto.setEndDate(endDate);
            List<Long> data = this.draftService.selectKidByCondition(dto);
            return ResponseUtils.returnObjectSuccess(data);
        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("getKidByCreatedate未知异常", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 批量ID查询
	 * @param kidList
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#selectByKids(java.util.Set)
	 */
	@Override
	public Response<List<ReleaseInfoVo>> selectByKids(Set<Long> kidList) {
		try {
            ReleaseInfoDto dto = new ReleaseInfoDto();
            dto.setKids(kidList.toArray(new Long[] {}));
            List<ReleaseInfoVo> list = this.draftService.selectByCondition(dto);
            return ResponseUtils.returnObjectSuccess(list);
        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("selectByKids 未知异常", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 记录查询[无分页]
	 * @param dto
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#selectByCondition(com.yryz.quanhu.resource.release.info.dto.ReleaseInfoDto)
	 */
	@Override
	public Response<List<ReleaseInfoVo>> selectByCondition(ReleaseInfoDto dto) {
		try {
            Assert.notNull(dto, "dto is null !");
            return ResponseUtils.returnListSuccess(this.draftService.selectByCondition(dto));
        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("selectByCondition 未知异常", e);
            return ResponseUtils.returnException(e);
        }
	}

	/**
	 * 编辑草稿箱
	 * @param record
	 * @return
	 * @see com.yryz.quanhu.resource.draft.api.DraftApi#edit(com.yryz.quanhu.resource.release.info.entity.ReleaseInfo)
	 */
	@Override
	public Response<ReleaseInfo> edit(ReleaseInfo record) {
		try {
            Assert.hasText(record.getContentSource(), "ContentSource is NULL !");

//            record.setClassifyId(ReleaseConstants.APP_DEFAULT_CLASSIFY_ID);
//            record.setDelFlag(CommonConstants.DELETE_NO);
//            record.setShelveFlag(CommonConstants.SHELVE_YES);

            Assert.isNull(record.getCoterieId(), "平台文章发布 没有：CoterieId");
            Assert.notNull(record.getKid(), "平台文章发布 没有：Kid");
            Assert.isTrue(null == record.getContentPrice() || record.getContentPrice() == 0L,
                    "平台文章发布 不能设置付费：ContentPrice");

            // 校验用户是否存在
            UserSimpleVO createUser = ResponseUtils.getResponseData(userApi.getUserSimple(record.getCreateUserId()));
            Assert.notNull(createUser, "发布者用户不存在！userId：" + record.getCreateUserId());

            ReleaseConfigVo cfgVo = releaseConfigService.getTemplate(ReleaseConstants.APP_DEFAULT_CLASSIFY_ID);
            Assert.notNull(cfgVo, "平台发布文章，发布模板不存在！classifyId：" + ReleaseConstants.APP_DEFAULT_CLASSIFY_ID);

            // 校验模板
            Assert.isTrue(draftService.releaseInfoCheck(record, cfgVo), "平台发布文章，参数校验不通过！");

            // 用户输入时至少有一个（富文本）元素不为空
            if (StringUtils.isEmpty(record.getContent()) && StringUtils.isEmpty(record.getImgUrl())
                    && StringUtils.isEmpty(record.getVideoUrl()) && StringUtils.isEmpty(record.getAudioUrl())) {
                throw QuanhuException.busiError("富文本元素(文字、图片、视频、音频)至少有一个不能为空！");
            }

            // kid 生成
//            record.setKid(ResponseUtils.getResponseData(idAPI.getSnowflakeId()));
            draftService.edit(record);

//            // 资源进聚合、进好友动态
//            this.commitResourceAndDynamic(record, createUser);
//            try {
//                // 接入统计计数
//                countApi.commitCount(BehaviorEnum.Release, record.getCreateUserId(), null, 1L);
//            } catch (Exception e) {
//                logger.error("统计计数 接入异常！", e);
//            }
//
//            // 对接积分事件
//            draftService.commitEvent(eventAPI, record);

            return ResponseUtils.returnObjectSuccess(record);

        } catch (QuanhuException e) {
            return ResponseUtils.returnException(e);
        } catch (Exception e) {
            logger.error("平台发布文章异常！", e);
            return ResponseUtils.returnException(e);
        }
	}
	
//	/**  
//	    * @Description: 资源聚合[首页聚合、好友动态聚合]
//	    * @author wangheng
//	    * @param @param releaseInfo
//	    * @param @param createUser
//	    * @return void
//	    * @throws  
//	    */
//	    public void commitResourceAndDynamic(ReleaseInfo releaseInfo, UserSimpleVO createUser) {
//	        try {
//	            ResourceTotal resourceTotal = new ResourceTotal();
//	            resourceTotal.setClassifyId(releaseInfo.getClassifyId().intValue());
//	            resourceTotal.setContent(releaseInfo.getContent());
//	            if (null != releaseInfo.getCoterieId() && 0L != releaseInfo.getCoterieId()) {
//	                resourceTotal.setCoterieId(String.valueOf(releaseInfo.getCoterieId()));
//	            }
//
//	            resourceTotal.setCreateDate(DateUtils.getString(Calendar.getInstance().getTime()));
//	            resourceTotal.setExtJson(JsonUtils.toFastJson(releaseInfo));
//	            resourceTotal.setModuleEnum(NumberUtils.toInt(ModuleContants.RELEASE));
//	            resourceTotal.setPublicState(ResourceEnum.PUBLIC_STATE_TRUE);
//	            resourceTotal.setResourceId(releaseInfo.getKid());
//
//	            // 设置达人标识 createUser
//	            resourceTotal.setTalentType(String.valueOf(createUser.getUserRole()));
//
//	            resourceTotal.setTitle(releaseInfo.getTitle());
//	            resourceTotal.setUserId(releaseInfo.getCreateUserId());
//	            resourceDymaicApi.commitResourceDymaic(resourceTotal);
//	        } catch (Exception e) {
//	            logger.error("资源聚合 接入异常！", e);
//	        }
//	    }

}
