package com.yryz.quanhu.resource.topic.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yryz.common.constant.CommonConstants;
import com.yryz.common.constant.ExceptionEnum;
import com.yryz.common.constant.ModuleContants;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.message.MessageConstant;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseConstant;
import com.yryz.common.utils.DateUtils;
import com.yryz.quanhu.behavior.count.api.CountApi;
import com.yryz.quanhu.behavior.read.api.ReadApi;
import com.yryz.quanhu.resource.api.ResourceDymaicApi;
import com.yryz.quanhu.resource.questionsAnswers.service.APIservice;
import com.yryz.quanhu.resource.questionsAnswers.service.SendMessageService;
import com.yryz.quanhu.resource.questionsAnswers.vo.MessageBusinessVo;
import com.yryz.quanhu.resource.topic.dao.TopicDao;
import com.yryz.quanhu.resource.topic.dao.TopicPostDao;
import com.yryz.quanhu.resource.topic.dto.TopicPostDto;
import com.yryz.quanhu.resource.topic.entity.Topic;
import com.yryz.quanhu.resource.topic.entity.TopicPost;
import com.yryz.quanhu.resource.topic.entity.TopicPostExample;
import com.yryz.quanhu.resource.topic.entity.TopicPostWithBLOBs;
import com.yryz.quanhu.resource.topic.service.TopicPost4AdminService;
import com.yryz.quanhu.resource.topic.service.TopicService;
import com.yryz.quanhu.resource.topic.vo.BehaviorVo;
import com.yryz.quanhu.resource.topic.vo.TopicAndPostVo;
import com.yryz.quanhu.resource.topic.vo.TopicPostVo;
import com.yryz.quanhu.resource.vo.ResourceTotal;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TopicPost4AdminServiceImpl implements TopicPost4AdminService {

    @Autowired
    private TopicPostDao topicPostDao;

    @Autowired
    private TopicDao topicDao;

    @Autowired
    private APIservice apIservice;

    @Reference
    private ReadApi readApi;

    @Autowired
    private SendMessageService sendMessageService;


    /**
     * 查询帖子详情
     *
     * @param kid
     * @param userId
     * @return
     */
    @Override
    public TopicPostVo getDetail(Long kid, Long userId) {
        TopicAndPostVo topicAndPostVo = new TopicAndPostVo();
        /**
         * 检验参数
         */
        if (null == kid) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }

        TopicPostExample example = new TopicPostExample();
        TopicPostExample.Criteria criteria = example.createCriteria();
        criteria.andKidEqualTo(kid);
        List<TopicPostWithBLOBs> topicPostWithBLOBsList = this.topicPostDao.selectByExampleWithBLOBs(example);
        if (null == topicPostWithBLOBsList || topicPostWithBLOBsList.isEmpty()) {
            //throw QuanhuException.busiError("查询的帖子不存在");
            return null;
        }
        TopicPostWithBLOBs topicPostWithBLOBs = topicPostWithBLOBsList.get(0);
        Long createUserId = topicPostWithBLOBs.getCreateUserId();
        TopicPostVo vo = new TopicPostVo();
        BeanUtils.copyProperties(topicPostWithBLOBs, vo);
        if (null != createUserId) {
            vo.setUser(apIservice.getUser(createUserId));
        }
        vo.setModuleEnum(ModuleContants.TOPIC_POST);

        //虚拟阅读数
        readApi.read(kid, topicPostWithBLOBs.getCreateUserId());
        return vo;
    }

    /**
     * 帖子列表查询
     *
     * @param dto
     * @return
     */
    @Override
    public PageList<TopicPostVo> queryList(TopicPostDto dto) {
        PageList<TopicPostVo> data = new PageList<>();
        Integer pageNum = dto.getCurrentPage() == null ? 1 : dto.getCurrentPage();
        Integer pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        Integer pageStartIndex = (pageNum - 1) * pageSize;
        TopicPostExample example = new TopicPostExample();
        TopicPostExample.Criteria criteria = example.createCriteria();
        criteria.andDelFlagEqualTo(CommonConstants.DELETE_NO);
        example.setPageStartIndex(pageStartIndex);
        example.setPageSize(pageSize);
        example.setOrderByClause("create_date desc");

        if (com.yryz.common.utils.StringUtils.isNotBlank(dto.getStartTime()) && com.yryz.common.utils.StringUtils.isNotBlank(dto.getEndTime())) {
            criteria.andCreateDateBetween(DateUtil.parse(dto.getStartTime()), DateUtils.parseDate(dto.getEndTime()));
        }
        if (dto.getShelveFlag() != null) {
            criteria.andShelveFlagEqualTo(dto.getShelveFlag());
        }
        if (com.yryz.common.utils.StringUtils.isNotBlank(dto.getContent())) {
            criteria.andContentLike("%" + dto.getTitle() + "%");
        }

        List<TopicPostWithBLOBs> topicPosts = this.topicPostDao.selectByExampleWithBLOBs(example);
        Long count = this.topicPostDao.countByExample(example);
        List<TopicPostVo> list = new ArrayList<>();
        for (TopicPostWithBLOBs topicPost : topicPosts) {
            TopicPostVo vo = new TopicPostVo();
            BeanUtils.copyProperties(topicPost, vo);
            Long createUserId = topicPost.getCreateUserId();
            if (null != createUserId) {
                vo.setUser(apIservice.getUser(createUserId));
            }

           Topic topic= this.topicDao.selectByPrimaryKey(topicPost.getTopicId());
            if(null!=topic){
               vo.setTitle(topic.getTitle());
            }
            vo.setModuleEnum(ModuleContants.TOPIC_POST);
            list.add(vo);
        }
        data.setCount(count);
        data.setCurrentPage(pageNum);
        data.setPageSize(pageSize);
        data.setEntities(list);
        return data;
    }

    @Override
    public Integer shelve(Long kid, Byte shelveFlag) {
        TopicPostWithBLOBs topicPost=new TopicPostWithBLOBs();
        topicPost.setKid(kid);
        topicPost.setShelveFlag(shelveFlag);
        return this.topicPostDao.updateByPrimaryKeySelective(topicPost);
    }

}
