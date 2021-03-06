package com.yryz.quanhu.resource.topic.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.constant.CommonConstants;
import com.yryz.common.constant.ExceptionEnum;
import com.yryz.common.constant.ModuleContants;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseConstant;
import com.yryz.common.utils.StringUtils;
import com.yryz.quanhu.behavior.count.api.CountApi;
import com.yryz.quanhu.behavior.count.contants.BehaviorEnum;
import com.yryz.quanhu.behavior.read.api.ReadApi;
import com.yryz.quanhu.resource.questionsAnswers.service.APIservice;
import com.yryz.quanhu.resource.topic.dao.TopicDao;
import com.yryz.quanhu.resource.topic.dao.TopicPostDao;
import com.yryz.quanhu.resource.topic.dto.TopicDto;
import com.yryz.quanhu.resource.topic.entity.Topic;
import com.yryz.quanhu.resource.topic.entity.TopicExample;
import com.yryz.quanhu.resource.topic.entity.TopicPost;
import com.yryz.quanhu.resource.topic.service.TopicPostService;
import com.yryz.quanhu.resource.topic.service.TopicService;
import com.yryz.quanhu.resource.topic.vo.TopicVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TopicServiceImpl implements TopicService {


    @Autowired
    private TopicDao topicDao;

    @Autowired
    private TopicPostDao topicPostDao;

    @Autowired
    private APIservice apIservice;

    @Reference
    private ReadApi readApi;

    @Reference
    private CountApi countApi;

    @Autowired
    private TopicPostService topicPostService;

    /**
     * 发布话题
     * @param topicDto
     * @return
     */
    @Override
    @Transactional
    public TopicVo saveTopic(TopicDto topicDto) {
        /**
         * 校验参数
         */
        String content = topicDto.getContent();
        String title = topicDto.getTitle();
        if (StringUtils.isBlank(content) || StringUtils.isBlank(title)) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }
        Topic topic = new Topic();
        BeanUtils.copyProperties(topicDto, topic);
        topic.setKid(apIservice.getKid());
        topic.setDelFlag(CommonConstants.DELETE_NO);
        topic.setShelveFlag(CommonConstants.SHELVE_YES);
        topic.setRecommend(CommonConstants.recommend_NO);
        topic.setCityCode("");
        topic.setGps("");
        topic.setRevision(0);
        topic.setCreateDate(new Date());
        Integer result = this.topicDao.insertSelective(topic);
        if (result > 0) {
            TopicVo vo = new TopicVo();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }
        return null;
    }

    /**
     * 查询话题的详情
     *
     * @param kid
     * @param userId
     * @return
     */
    @Override
    public TopicVo queryDetail(Long kid, Long userId) {
        /**
         * 校验参数
         */
        if (null == kid) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }

        TopicExample example=new TopicExample();
        TopicExample.Criteria criteria=example.createCriteria();
        criteria.andKidEqualTo(kid);
        List<Topic> topics = this.topicDao.selectByExample(example);
        if (null == topics || topics.isEmpty()) {
            return null;
        }
        Topic topic=topics.get(0);
        TopicVo topicVo = new TopicVo();
        BeanUtils.copyProperties(topic, topicVo);
        Long createUserId = topic.getCreateUserId();
        if (null != createUserId) {
            topicVo.setUser(apIservice.getUser(createUserId));
        }
        Long replyCount=this.topicPostService.countPostByTopicId(topicVo.getKid());
        topicVo.setReplyCount(replyCount);
        topicVo.setModuleEnum(ModuleContants.TOPIC);

        //虚拟阅读数
        readApi.read(kid,topic.getCreateUserId());

        return topicVo;
    }

    /**
     * 查询话题的列表
     *
     * @param dto
     * @return
     */
    @Override
    public PageList<TopicVo> queryTopicList(TopicDto dto) {
        PageList<TopicVo> pageList = new PageList<>();
        Integer pageNum = dto.getCurrentPage() == null ? 1 : dto.getCurrentPage();
        Integer pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        Integer pageStartIndex = (pageNum - 1) * pageSize;
        String orderBy = StringUtils.isBlank(dto.getOrderBy()) ? "recommend desc ,create_date desc " : dto.getOrderBy();
        TopicExample example = new TopicExample();
        TopicExample.Criteria criteria=example.createCriteria();
        criteria.andDelFlagEqualTo(CommonConstants.DELETE_NO);
        criteria.andShelveFlagEqualTo(CommonConstants.SHELVE_YES);
        if(dto.getRecommend()!=null){
            criteria.andRecommendEqualTo(dto.getRecommend());
        }
        example.setPageStartIndex(pageStartIndex);
        example.setPageSize(pageSize);
        example.setOrderByClause(orderBy);
        List<Topic> list = this.topicDao.selectByExampleWithBLOBs(example);
        List<TopicVo> topicVos = new ArrayList<>();
        for (Topic topic : list) {
            TopicVo vo = new TopicVo();
            BeanUtils.copyProperties(topic, vo);
            Long createUserId = topic.getCreateUserId();
            if (createUserId != null) {
                vo.setUser(apIservice.getUser(createUserId));
            }
            vo.setReplyCount(0L);
            Response<Map<String,Long>> data=countApi.getCount(BehaviorEnum.TALK.getCode(),  vo.getKid(),null);
            if(ResponseConstant.SUCCESS.getCode().equals(data.getCode())){
                Map<String,Long> map=data.getData();
                vo.setStatistics(map);
             }
            vo.setModuleEnum(ModuleContants.TOPIC);
            topicVos.add(vo);
        }
        pageList.setEntities(topicVos);
        pageList.setPageSize(pageSize);
        pageList.setCurrentPage(pageNum);
        pageList.setCount(0L);
        return pageList;
    }


    /**
     * 话题标记删除
     *
     * @param kid
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public Integer deleteTopic(Long kid, Long userId) {
        /**
         * 校验参数
         */
        if (null == kid || null == userId) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }

        Topic topic = this.topicDao.selectByPrimaryKey(kid);
        if (null == topic) {
            throw QuanhuException.busiError("","删除的话题不存在","删除的话题不存在");
        }
        if (topic.getCreateUserId().compareTo(userId) != 0) {
            throw new QuanhuException(ExceptionEnum.USER_NO_RIGHT_TODELETE);
        }
        Topic topicParam = new Topic();
        topicParam.setKid(kid);
        topicParam.setDelFlag(CommonConstants.DELETE_YES);
        int result= this.topicDao.updateByPrimaryKeySelective(topicParam);

        /**
         * 话题下架，同时下架话题下的帖子
         */
        TopicPost topicPost=new TopicPost();
        topicPost.setTopicId(kid);
        topicPost.setDelFlag(CommonConstants.DELETE_YES);
        this.topicPostDao.deleteByTipocId(topicPost);
        return  result;
    }

	@Override
	public List<Long> getKidByCreatedate(String startDate, String endDate) {
		return topicDao.selectKidByCreatedate(startDate, endDate);
	}

	@Override
	public List<Topic> getByKids(List<Long> kidList) {
		return topicDao.selectByKids(kidList);
	}
}
