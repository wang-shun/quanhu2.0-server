package com.yryz.quanhu.resource.questionsAnswers.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.message.InteractiveBody;
import com.yryz.common.message.MessageConstant;
import com.yryz.common.message.MessageVo;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseConstant;
import com.yryz.common.utils.DateUtils;
import com.yryz.common.utils.IdGen;
import com.yryz.quanhu.coterie.coterie.vo.CoterieInfo;
import com.yryz.quanhu.message.message.api.MessageAPI;
import com.yryz.quanhu.order.sdk.constant.FeeDetail;
import com.yryz.quanhu.resource.questionsAnswers.constants.QuestionAnswerConstants;
import com.yryz.quanhu.resource.questionsAnswers.service.APIservice;
import com.yryz.quanhu.resource.questionsAnswers.service.SendMessageService;
import com.yryz.quanhu.resource.questionsAnswers.vo.MessageBusinessVo;
import com.yryz.quanhu.user.vo.UserSimpleVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

@Service
public class SendMessageServiceImpl implements SendMessageService {


    private static final Logger logger = LoggerFactory.getLogger(SendMessageServiceImpl.class);

    @Autowired
    private APIservice apIservice;

    @Reference
    private MessageAPI messageAPI;

    /**
     * 发送问答的通知消息
     *
     * @param
     * @param messageConstant
     * @return
     */
    @Override
    @Transactional
    public Boolean sendNotify4Question(MessageBusinessVo messageBusinessVo, MessageConstant messageConstant, Boolean persistent) {
        Long kid = messageBusinessVo.getKid();
        Long tosendUserId = messageBusinessVo.getTosendUserId();
        Long fromUserId = messageBusinessVo.getFromUserId();
        String title = messageBusinessVo.getTitle();
        title = StringUtils.length(title) > 20 ? title.substring(0, 20) : title;
        Byte isAnonymity = messageBusinessVo.getIsAnonymity();
        String coterieId = messageBusinessVo.getCoterieId();
        Long amount = messageBusinessVo.getAmount();
        if(amount==null){
            amount=0L;
        }else{
            amount=amount/100;
        }
        String imgUrl = messageBusinessVo.getImgUrl();
        String moduleEnum = messageBusinessVo.getModuleEnum();

        UserSimpleVO fromUser = this.apIservice.getUser(fromUserId);
        if (null == fromUser) {
            return false;
        }

        CoterieInfo coterieInfo = null;
        if (coterieId != null) {
            coterieInfo = apIservice.getCoterieinfo(Long.valueOf(coterieId));
        }

        /**
         * 组装InteractiveBody对象
         */
        InteractiveBody interactiveBody = new InteractiveBody();
        interactiveBody.setBodyTitle(title);
        interactiveBody.setBodyImg(imgUrl == null ? "" : imgUrl);
        interactiveBody.setCoterieId(coterieId);
        if (coterieInfo != null) {
            interactiveBody.setCoterieName(coterieInfo.getName());
        }
        interactiveBody.setUserImg(QuestionAnswerConstants.anonymityType.YES.equals(isAnonymity) ? null : fromUser.getUserImg());
        interactiveBody.setUserNickName(QuestionAnswerConstants.anonymityType.YES.equals(isAnonymity)
                ? QuestionAnswerConstants.ANONYMOUS_USER_NAME : fromUser.getUserNickName());
        interactiveBody.setUserId(String.valueOf(fromUser.getUserId()));
        interactiveBody.setCoterieId(coterieId);

        /**
         * 组装MessageVo对象
         */
        MessageVo messageVo = new MessageVo();
        messageVo.setMessageId(IdGen.uuid());
        messageVo.setResourceId(String.valueOf(kid));
        messageVo.setCoterieId(String.valueOf(coterieId));
        messageVo.setActionCode(messageConstant.getMessageActionCode());
        messageVo.setCreateTime(DateUtils.getDateTime());
        messageVo.setLabel(messageConstant.getLabel());
        messageVo.setType(messageConstant.getType());
        messageVo.setTitle(messageConstant.getTitle());

        /**
         * 处理消息的content
         */
        String content = messageConstant.getContent();
        if (StringUtils.isNotEmpty(fromUser.getUserNickName())) {
            content = content.replace("{someone}", fromUser.getUserNickName());
        }


        if (StringUtils.isNotBlank(title)) {
            content = content.replace("{title}", title);
        }

        if (coterieInfo != null) {
            content = content.replace("{coterieName}", coterieInfo.getName());
        }

        if (amount != null) {
            DecimalFormat df = new DecimalFormat("#0.00");
            if (messageConstant == MessageConstant.ANSWER_PAYED) {
                logger.info("处理推送消息里的抽成后的金额开始");
                FeeDetail feeDetail = new FeeDetail("", 90L, 1, "");
                logger.info("原始金额是 : " + amount + ", 抽成规则是 : " + feeDetail.getFee());
                BigDecimal cost = BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(feeDetail.getFee()))
                        .divide(BigDecimal.valueOf(100L));
                logger.info("抽成后金额是 : " + String.valueOf(cost));
                String money = df.format(cost);
                content = content.replace("{money}", money);
            } else {
                content = content.replace("{money}", df.format(amount));
            }
        }
        messageVo.setContent(content);
        messageVo.setToCust(String.valueOf(tosendUserId));
        messageVo.setViewCode(messageConstant.getMessageViewCode());
        messageVo.setBody(interactiveBody);
        messageVo.setModuleEnum(moduleEnum);
        Response<Boolean> data = messageAPI.sendMessage(messageVo, persistent);
        if (ResponseConstant.SUCCESS.getCode().equals(data.getCode())) {
            return data.getData();
        }
        return false;
    }

}
