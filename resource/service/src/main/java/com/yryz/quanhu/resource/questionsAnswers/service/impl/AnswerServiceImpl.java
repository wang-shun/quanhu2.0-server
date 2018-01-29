package com.yryz.quanhu.resource.questionsAnswers.service.impl;

import com.yryz.common.constant.CommonConstants;
import com.yryz.common.constant.ExceptionEnum;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.message.MessageConstant;
import com.yryz.quanhu.order.sdk.OrderSDK;
import com.yryz.quanhu.order.sdk.constant.OrderEnum;
import com.yryz.quanhu.resource.enums.ResourceTypeEnum;
import com.yryz.quanhu.resource.questionsAnswers.constants.QuestionAnswerConstants;
import com.yryz.quanhu.resource.questionsAnswers.dao.AnswerDao;
import com.yryz.quanhu.resource.questionsAnswers.dto.AnswerDto;
import com.yryz.quanhu.resource.questionsAnswers.entity.Answer;
import com.yryz.quanhu.resource.questionsAnswers.entity.AnswerExample;
import com.yryz.quanhu.resource.questionsAnswers.entity.AnswerWithBLOBs;
import com.yryz.quanhu.resource.questionsAnswers.entity.Question;
import com.yryz.quanhu.resource.questionsAnswers.service.APIservice;
import com.yryz.quanhu.resource.questionsAnswers.service.AnswerService;
import com.yryz.quanhu.resource.questionsAnswers.service.SendMessageService;
import com.yryz.quanhu.resource.questionsAnswers.service.QuestionService;
import com.yryz.quanhu.resource.questionsAnswers.vo.AnswerVo;
import com.yryz.quanhu.resource.questionsAnswers.vo.MessageBusinessVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private APIservice apIservice;

    @Autowired
    OrderSDK orderSDK;


    @Autowired
    private SendMessageService questionMessageService;

    @Override
    public AnswerVo saveAnswer(AnswerDto answerdto) {
        /**
         * 校验参数
         */
        Long questionId = answerdto.getQuestionId();
        Long coterieId = answerdto.getCoterieId();
        if (null == questionId || null == coterieId) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }

        Question questionCheck=this.questionService.queryAvailableQuestionByKid(questionId);
        if(null==questionCheck){
            throw  QuanhuException.busiError("提问不存在");
        }

        if(null !=questionCheck.getAnswerdFlag() && QuestionAnswerConstants.AnswerdFlag.NOt_ANSWERED.compareTo(questionCheck.getAnswerdFlag())==-1){
            throw  QuanhuException.busiError("圈主已处理过该问题，不能再回答");
        }
        AnswerWithBLOBs answerWithBLOBs = new AnswerWithBLOBs();
        BeanUtils.copyProperties(answerdto, answerWithBLOBs);
        answerWithBLOBs.setKid(apIservice.getKid());
        answerWithBLOBs.setCreateDate(new Date());
        answerWithBLOBs.setDelFlag(CommonConstants.DELETE_NO);
        answerWithBLOBs.setRevision(0);
        answerWithBLOBs.setCityCode("");
        answerWithBLOBs.setGps("");
        answerWithBLOBs.setOperatorId("");
        answerWithBLOBs.setOrderFlag(QuestionAnswerConstants.OrderType.Not_paid);
        answerWithBLOBs.setOrderId("");
        answerWithBLOBs.setShelveFlag(CommonConstants.SHELVE_YES);
        answerWithBLOBs.setAnswerType(QuestionAnswerConstants.questionType.ONE_TO_ONE);
        /**
         *校验圈主是否禁言，圈粉是否删除
         */
        this.answerDao.insertSelective(answerWithBLOBs);

        //更新问题的回答状态
        Question question=new Question();
        question.setKid(questionId);
        question.setAnswerdFlag(QuestionAnswerConstants.AnswerdFlag.ANSWERED);
        this.questionService.updateByPrimaryKeySelective(question);
        AnswerVo answerVo = new AnswerVo();
        BeanUtils.copyProperties(answerWithBLOBs, answerVo);

        //向圈主支付回答的费用
        if(null!=questionCheck.getChargeAmount()){
            if(questionCheck.getChargeAmount().longValue()>0){
              Long orderId=  orderSDK.executeOrder(OrderEnum.ANSWER_ORDER,answerdto.getCreateUserId(),questionCheck.getChargeAmount());
              if(null!=orderId){
                  question.setOrderFlag(QuestionAnswerConstants.OrderType.paid);
                  question.setOrderId(String.valueOf(orderId));
                  this.questionService.updateByPrimaryKeySelective(question);
              }
            }
        }
        MessageBusinessVo messageBusinessVo =new MessageBusinessVo();
        messageBusinessVo.setCoterieId(String.valueOf(answerWithBLOBs.getCoterieId()));
        messageBusinessVo.setIsAnonymity(null);
        messageBusinessVo.setKid(answerWithBLOBs.getKid());
        messageBusinessVo.setModuleEnum(ResourceTypeEnum.ANSWER);
        messageBusinessVo.setTosendUserId(questionCheck.getCreateUserId());
        messageBusinessVo.setTitle(answerWithBLOBs.getContent());
        messageBusinessVo.setAmount(questionCheck.getChargeAmount());
        questionMessageService.sendNotify4Question(messageBusinessVo, MessageConstant.QUESTION_PAYED);
        return answerVo;
    }

    @Override
    public Integer deleteAnswer(AnswerDto answerdto) {
        /**
         * 校验参数
         */
        Long kid = answerdto.getKid();
        Byte shelveFlag = answerdto.getShelveFlag();
        if (null == kid || null == shelveFlag) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }
        Answer answer = new Answer();
        BeanUtils.copyProperties(answerdto, answer);
        return this.answerDao.updateByPrimaryKey(answer);
    }

    /**
     * 查询查询回答详情
     *
     * @param kid
     * @return
     */
    @Override
    public AnswerVo getDetailByQuestionId(Long kid) {
        /**
         *校验传入的参数
         */
        if (null == kid) {
            throw new QuanhuException(ExceptionEnum.PARAM_MISSING);
        }
        AnswerExample example=new AnswerExample();
        AnswerExample.Criteria criteria=example.createCriteria();
        criteria.andQuestionIdEqualTo(kid);
        criteria.andDelFlagEqualTo(CommonConstants.DELETE_NO);
        criteria.andShelveFlagEqualTo(CommonConstants.SHELVE_YES);
        List<AnswerWithBLOBs> answerWithBLOBsList = this.answerDao.selectByExampleWithBLOBs(example);
        if (null==answerWithBLOBsList || answerWithBLOBsList.isEmpty()) {
            //throw QuanhuException.busiError("查询的回答不存在");
            return null;
        }
        AnswerWithBLOBs answerWithBLOBs=answerWithBLOBsList.get(0);
        AnswerVo answerVo = new AnswerVo();
        BeanUtils.copyProperties(answerWithBLOBs, answerVo);
        Long createUserId = answerWithBLOBs.getCreateUserId();
        if (null != createUserId) {
            answerVo.setUser(apIservice.getUser(createUserId));
        }
        answerVo.setModuleEnum(ResourceTypeEnum.ANSWER);
        return answerVo;
    }

    @Override
    public AnswerVo queryAnswerVoByquestionId(Long kid) {
        AnswerExample example = new AnswerExample();
        AnswerExample.Criteria criteria = example.createCriteria();
        criteria.andQuestionIdEqualTo(kid);
        List<AnswerWithBLOBs> answerWithBLOBsList = this.answerDao.selectByExampleWithBLOBs(example);
        if (!answerWithBLOBsList.isEmpty()) {
            AnswerWithBLOBs answerWithBLOBs = answerWithBLOBsList.get(0);
            AnswerVo answerVo = new AnswerVo();
            BeanUtils.copyProperties(answerWithBLOBs, answerVo);
            Long createUserId = answerWithBLOBs.getCreateUserId();
            if (null != createUserId) {
                answerVo.setUser(apIservice.getUser(createUserId));
            }
            return answerVo;
        }
        return null;
    }


}
