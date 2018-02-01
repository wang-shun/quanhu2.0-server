package com.yryz.quanhu.behavior.transmit.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.constant.CommonConstants;
import com.yryz.common.constant.ExceptionEnum;
import com.yryz.common.constant.ModuleContants;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.message.InteractiveBody;
import com.yryz.common.message.MessageConstant;
import com.yryz.common.message.MessageVo;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.utils.DateUtils;
import com.yryz.common.utils.IdGen;
import com.yryz.common.utils.JsonUtils;
import com.yryz.common.utils.StringUtils;
import com.yryz.quanhu.behavior.count.api.CountApi;
import com.yryz.quanhu.behavior.count.contants.BehaviorEnum;
import com.yryz.quanhu.behavior.transmit.dao.TransmitMongoDao;
import com.yryz.quanhu.behavior.transmit.dto.TransmitInfoDto;
import com.yryz.quanhu.behavior.transmit.entity.TransmitInfo;
import com.yryz.quanhu.behavior.transmit.service.TransmitService;
import com.yryz.quanhu.behavior.transmit.vo.TransmitInfoVo;
import com.yryz.quanhu.coterie.coterie.service.CoterieApi;
import com.yryz.quanhu.coterie.coterie.vo.CoterieInfo;
import com.yryz.quanhu.message.message.api.MessageAPI;
import com.yryz.quanhu.resource.api.ResourceApi;
import com.yryz.quanhu.resource.api.ResourceDymaicApi;
import com.yryz.quanhu.resource.enums.ResourceEnum;
import com.yryz.quanhu.resource.vo.ResourceTotal;
import com.yryz.quanhu.resource.vo.ResourceVo;
import com.yryz.quanhu.support.id.api.IdAPI;
import com.yryz.quanhu.user.service.UserApi;
import com.yryz.quanhu.user.vo.UserSimpleVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.jdbc.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransmitServiceImpl implements TransmitService {

    private static final Logger logger = LoggerFactory.getLogger(TransmitServiceImpl.class);

    @Reference(check = false, timeout = 30000)
    IdAPI idAPI;

    @Autowired
    TransmitMongoDao transmitMongoDao;

    @Reference(check = false, timeout = 30000)
    CountApi countApi;

    @Reference(check = false, timeout = 30000)
    ResourceApi resourceApi;

    @Reference(check = false, timeout = 30000)
    ResourceDymaicApi resourceDymaicApi;

    @Reference(check = false, timeout = 30000)
    UserApi userApi;

    @Reference(check = false, timeout = 30000)
    CoterieApi coterieApi;

    @Reference(check = false, timeout = 30000)
    MessageAPI messageAPI;

    /**
     * 转发
     * @param   transmitInfo
     * */
    public void single(TransmitInfo transmitInfo) {
        String extJson = "";
        Response<ResourceVo> result = null;
        Long userId = null;
        ResourceVo resourceVo = null;
        if(ModuleContants.COTERIE.equals(String.valueOf(transmitInfo.getModuleEnum()))) {
            Response<CoterieInfo> coterieInfoResponse = coterieApi.queryCoterieInfo(transmitInfo.getResourceId());
            if(!coterieInfoResponse.success()) {
                throw new QuanhuException(ExceptionEnum.SysException);
            }
            CoterieInfo coterieInfo = coterieInfoResponse.getData();
            if(coterieInfo == null || !Integer.valueOf(CommonConstants.SHELVE_YES).equals(coterieInfo.getShelveFlag()) ) {
                throw QuanhuException.busiError("私圈不存在或者已下架");
            }
            extJson = JsonUtils.toFastJson(coterieInfo);
        } else {
            try {
                result = resourceApi.getResourcesById(transmitInfo.getResourceId().toString());
                if(!result.success()) {
                    throw QuanhuException.busiError("资源不存在或者已删除");
                }
            } catch (Exception e) {
                logger.error("获取资源失败", e);
                throw QuanhuException.busiError("资源不存在或者已删除");
            }
            resourceVo = result.getData();
            if(resourceVo == null || ResourceEnum.DEL_FLAG_TRUE.equals(resourceVo.getDelFlag())) {
                throw QuanhuException.busiError("资源不存在或者已删除");
            }
            extJson = resourceVo.getExtJson();
            userId = resourceVo.getUserId();
        }
        //发送动态
        this.sendDymaic(transmitInfo, extJson);
        Response<Long> idResult = idAPI.getSnowflakeId();
        if(!idResult.success()) {
            throw new QuanhuException(ExceptionEnum.SysException);
        }
        transmitInfo.setKid(idResult.getData());
        transmitInfo.setCreateDate(new Date());
        transmitInfo.setCreateDateLong(transmitInfo.getCreateDate().getTime());
        //保存转发记录
        transmitMongoDao.save(transmitInfo);
        //发送消息
        this.sendMessage(userId, transmitInfo, resourceVo);
        try {
            //递增转发数
            countApi.commitCount(BehaviorEnum.Transmit, transmitInfo.getParentId(), null, 1L);
        } catch (Exception e) {
            logger.error("递增转发数 失败", e);
        }
    }

    /**
     * 转发列表
     * @param   transmitInfoDto
     * @return
     * */
    public PageList<TransmitInfoVo> list(TransmitInfoDto transmitInfoDto) {
        List<TransmitInfoVo> resultList = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(transmitInfoDto.getParentId()));
        query.addCriteria(Criteria.where("moduleEnum").is(transmitInfoDto.getModuleEnum()));
        long count = transmitMongoDao.count(query);
        if(count > 0) {
            query.with(new Sort(Sort.Direction.DESC, "createDateLong"));
            List<TransmitInfo> list = transmitMongoDao.findPage(transmitInfoDto.getCurrentPage(), transmitInfoDto.getPageSize(), query);
            if(!CollectionUtils.isEmpty(list)) {
                resultList = list.stream().map(transmitInfo -> {
                    TransmitInfoVo transmitInfoVo = new TransmitInfoVo();
                    BeanUtils.copyProperties(transmitInfo, transmitInfoVo);
                    return transmitInfoVo;
                }).collect(Collectors.toList());
                //设置用户
                this.setUserInfo(resultList);
            }
        }
        PageList<TransmitInfoVo> pageList = new PageList<>();
        pageList.setCurrentPage(transmitInfoDto.getCurrentPage());
        pageList.setPageSize(transmitInfoDto.getPageSize());
        pageList.setEntities(resultList);
        pageList.setCount(count);
        return pageList;
    }

    private void sendDymaic(TransmitInfo transmitInfo, String extJson) {
        ResourceTotal resourceTotal = new ResourceTotal();
        resourceTotal.setUserId(transmitInfo.getCreateUserId());
        resourceTotal.setModuleEnum(Integer.valueOf(ModuleContants.TRANSMIT));
        resourceTotal.setResourceId(transmitInfo.getResourceId());
        resourceTotal.setExtJson(extJson);
        resourceTotal.setTransmitNote(transmitInfo.getContent());
        resourceTotal.setTransmitType(transmitInfo.getModuleEnum());

        try {
            resourceDymaicApi.commitResourceDymaic(resourceTotal);
        } catch (Exception e) {
            logger.error("发送动态 失败", e);
        }
    }

    private void setUserInfo(List<TransmitInfoVo> list) {
        Set<String> set = new HashSet<>();
        list.stream()
                .filter(transmitInfo -> transmitInfo.getCreateUserId() != null)
                .forEach(transmitInfo -> set.add(transmitInfo.getCreateUserId().toString()));
        Response<Map<String, UserSimpleVO>> result = userApi.getUserSimple(set);
        if(result.success()) {
            Map<String, UserSimpleVO> simple = result.getData();
            list.stream()
                    .filter(transmitInfo -> transmitInfo.getCreateUserId() != null)
                    .forEach(transmitInfo -> transmitInfo.setUser(simple.get(transmitInfo.getCreateUserId().toString())));
        }
    }

    private void sendMessage(Long userId, TransmitInfo transmitInfo, ResourceVo resourceVo) {
        try {
            Response<UserSimpleVO> userSimple = userApi.getUserSimple(userId);
            if(!userSimple.success() || userSimple.getData() == null) {
                throw new QuanhuException(ExceptionEnum.SysException);
            }
            UserSimpleVO user = userSimple.getData();
            MessageConstant constant = MessageConstant.TRANSMIT_CONTENT_POST;
            MessageVo messageVo = new MessageVo();
            messageVo.setMessageId(IdGen.uuid());
            messageVo.setActionCode(constant.getMessageActionCode());
            String content = null;
            boolean isPush = true;
            if(ModuleContants.RELEASE.equals(transmitInfo.getModuleEnum()) ) {
                content = user.getUserNickName()+"转发了您发布的内容。";
            } else if(ModuleContants.TOPIC_POST.equals(transmitInfo.getModuleEnum()) ) {
                content = user.getUserNickName()+"转发了您发布的帖子。";
            } else {
                //如果parentId与resourceId不相等，属于动态
                if(!transmitInfo.getParentId().equals(transmitInfo.getResourceId()) ) {
                    content = user.getUserNickName()+"转发了您的动态。";
                    isPush = false;
                }
            }
            messageVo.setContent(content);
            messageVo.setCreateTime(DateUtils.getDateTime());
            messageVo.setLabel(constant.getLabel());
            messageVo.setType(constant.getType());
            messageVo.setTitle(constant.getTitle());
            messageVo.setToCust(userId.toString());
            messageVo.setViewCode(constant.getMessageViewCode());
            messageVo.setResourceId(resourceVo.getResourceId());
            messageVo.setModuleEnum(resourceVo.getModuleEnum());
            InteractiveBody body = new InteractiveBody();
            if(resourceVo != null) {
                String title = resourceVo.getTitle();
                if(ModuleContants.TOPIC_POST.equals(transmitInfo.getModuleEnum()) ) {
                    if(!StringUtils.isEmpty(title) && title.length() > 20 ) {
                        title = title.substring(0, 20);
                    }
                }
                body.setBodyTitle(title);
                //TODO:缺少首张图获取
            }
            messageVo.setBody(body);
            messageAPI.sendMessage(messageVo, isPush);
        } catch (Exception e) {
            logger.error("发送消息 失败", e);
        }
    }

}
