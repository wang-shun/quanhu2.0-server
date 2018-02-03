package com.yryz.quanhu.resource.questionsAnswers.service.impl;

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
import com.yryz.quanhu.coterie.coterie.vo.CoterieInfo;
import com.yryz.quanhu.coterie.member.constants.MemberConstant;
import com.yryz.quanhu.coterie.member.service.CoterieMemberAPI;
import com.yryz.quanhu.message.message.api.MessageAPI;
import com.yryz.quanhu.order.enums.AccountEnum;
import com.yryz.quanhu.order.sdk.OrderSDK;
import com.yryz.quanhu.order.sdk.constant.OrderEnum;
import com.yryz.quanhu.order.sdk.dto.InputOrder;
import com.yryz.quanhu.resource.api.ResourceDymaicApi;
import com.yryz.quanhu.resource.questionsAnswers.constants.QuestionAnswerConstants;
import com.yryz.quanhu.resource.questionsAnswers.dao.QuestionDao;
import com.yryz.quanhu.resource.questionsAnswers.dto.QuestionDto;
import com.yryz.quanhu.resource.questionsAnswers.entity.Question;
import com.yryz.quanhu.resource.questionsAnswers.entity.QuestionExample;
import com.yryz.quanhu.resource.questionsAnswers.service.*;
import com.yryz.quanhu.resource.questionsAnswers.vo.*;
import com.yryz.quanhu.resource.topic.vo.BehaviorVo;
import com.yryz.quanhu.resource.vo.ResourceTotal;
import com.yryz.quanhu.score.enums.EventEnum;
import com.yryz.quanhu.score.service.EventAPI;
import com.yryz.quanhu.score.vo.EventInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wanght
 * @date 2018-02-1-22
 */
@Service
public class Question4AdminServiceImpl implements Question4AdminService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private APIservice apIservice;

    @Autowired
    private AnswerService answerService;

    @Reference
    private ReadApi readApi;

    @Autowired
    private OrderSDK orderSDK;

    @Reference
    private CountApi countApi;

    @Reference
    private MessageAPI messageAPI;

    @Reference
    private CoterieMemberAPI coterieMemberAPI;

    @Reference
    private EventAPI eventAPI;

    @Reference
    private ResourceDymaicApi resourceDymaicApi;

    @Autowired
    private SendMessageService questionMessageService;

    /** 查询问答列表
     * @param dto
     * @return
     */
    @Override
    public PageList<QuestionAdminVo> queryQuestionAnswerList(QuestionDto dto) {
        PageList<QuestionAdminVo> questionAdminVoList = new PageList<>();


        Integer pageNum = dto.getCurrentPage() == null ? 1 : dto.getCurrentPage();
        Integer pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        Integer pageStartIndex = (pageNum - 1) * pageSize;
        QuestionExample example = new QuestionExample();
        example.setPageStartIndex(pageStartIndex);
        example.setPageSize(pageSize);

        QuestionExample.Criteria criteria = example.createCriteria();
        Long coteriaId = dto.getCoterieId();
        if(coteriaId!=null) {
            criteria.andCoterieIdEqualTo(coteriaId);
        }
        criteria.andDelFlagEqualTo(CommonConstants.DELETE_NO);

        Long createUserId = dto.getCreateUserId();
        if(null!=createUserId) {
            criteria.andCreateUserIdEqualTo(createUserId);
        }
        example.setOrderByClause("create_date desc");
        Long count=this.questionDao.countByExample(example);
        List<Question> list = this.questionDao.selectByExampleWithBLOBs(example);
        List<QuestionAdminVo> questionAdminVos = new ArrayList<>();
        for (Question question : list) {
            QuestionAdminVo questionAdminVo = new QuestionAdminVo();
            BeanUtils.copyProperties(question, questionAdminVo);
            Long questionCreateUserId = question.getCreateUserId();
            if (null != questionCreateUserId) {
                questionAdminVo.setUser(apIservice.getUser(questionCreateUserId));
            }

            /**
             * 根据questionId 查询回答
             */
            if (QuestionAnswerConstants.AnswerdFlag.ANSWERED.compareTo(question.getAnswerdFlag()) == 0) {
                AnswerVo answerVo =
                        this.answerService.queryAnswerVoByquestionId(question.getKid());
                questionAdminVo.setAnswerDate(answerVo.getCreateDate());
            }
            CoterieInfo coterieInfo= apIservice.getCoterieinfo(question.getCoterieId());
            if(null!=null){
                questionAdminVo.setCoterieName(coterieInfo.getName());
            }
            questionAdminVos.add(questionAdminVo);
        }

        questionAdminVoList.setCount(count);
        questionAdminVoList.setCurrentPage(pageNum);
        questionAdminVoList.setPageSize(pageSize);
        questionAdminVoList.setEntities(questionAdminVos);
        return questionAdminVoList;
    }

    /**
     * 查询有用的提问
     *
     * @param kid
     * @return
     */
    @Override
    public Question queryAvailableQuestionByKid(Long kid) {
        QuestionExample example = new QuestionExample();
        QuestionExample.Criteria criteria = example.createCriteria();
        criteria.andKidEqualTo(kid);
        criteria.andShelveFlagEqualTo(CommonConstants.SHELVE_YES);
        criteria.andDelFlagEqualTo(CommonConstants.DELETE_NO);
        criteria.andIsValidEqualTo(QuestionAnswerConstants.validType.YES);
        List<Question> questions = this.questionDao.selectByExample(example);
        if (null != questions && !questions.isEmpty()) {
            return questions.get(0);
        }
        return null;
    }


    /**
     * 问题下架
     * @param kid
     * @return
     */
    @Override
    public Integer shalveDown(Long kid) {
        Question question=new Question();
        question.setKid(kid);
        question.setShelveFlag(CommonConstants.SHELVE_NO);
        return  this.questionDao.updateByPrimaryKeySelective(question);
    }


    private Boolean checkIdentity(Long userId, Long citeriaId, MemberConstant.Permission permission) {
        //圈主10 成员20 路人未审请30 路人待审核40
        Response<Integer> data = coterieMemberAPI.permission(userId, citeriaId);
        if (ResponseConstant.SUCCESS.getCode().equals(data.getCode())) {
            if (null != data.getData()) {
                if (permission.getStatus().compareTo(data.getData()) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

}