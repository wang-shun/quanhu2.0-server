package com.yryz.quanhu.message.message.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.message.MessageVo;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.*;
import com.yryz.common.utils.StringUtils;
import com.yryz.framework.core.cache.RedisTemplateBuilder;
import com.yryz.quanhu.message.message.constants.MessageContants;
import com.yryz.quanhu.message.message.constants.MessageMap;
import com.yryz.quanhu.message.message.dto.MessageAdminDto;
import com.yryz.quanhu.message.message.mongo.MessageAdminMongo;
import com.yryz.quanhu.message.message.mq.MessageSender;
import com.yryz.quanhu.message.message.service.MessageAdminService;
import com.yryz.quanhu.message.message.service.MessageService;
import com.yryz.quanhu.message.message.vo.MessageAdminVo;
import com.yryz.quanhu.message.push.api.PushAPI;
import com.yryz.quanhu.message.push.entity.PushReceived;
import com.yryz.quanhu.message.push.entity.PushReqVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.*;
import org.apache.commons.lang3.math.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 *
 * @Description:
 * @Date: Created in 2018 2018/1/31 10:20
 * @Author: pn
 */
@Service
public class MessageAdminServiceImpl implements MessageAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAdminServiceImpl.class);

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    @Autowired
    private MessageAdminMongo messageAdminMongo;

    @Reference(check = false, timeout = 30000, retries = 0)
    private PushAPI pushAPI;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageSender messageSender;

    /*@Resource
    private RedisTemplate<String, ScheduledFuture> redisTemplate;*/

    @Autowired
    private RedisTemplateBuilder redisTemplateBuilder;

    private static final String QH_MESSAGE_ADMIN_SCHEDULE = "qh:message:admin:schedule:";


    @Override
    public PageList<MessageAdminVo> listAdmin(MessageAdminDto messageAdminDto) {
        PageList<MessageAdminVo> pageList = messageAdminMongo.listAdmin(messageAdminDto);
        List<MessageAdminVo> entities = pageList.getEntities();
        if (CollectionUtils.isNotEmpty(entities)) {
            List<String> msgIds = Lists.newArrayList();
            entities.forEach(messageAdminVo -> {
                if (org.apache.commons.lang.StringUtils.isNotBlank(messageAdminVo.getJpId())) {
                    msgIds.add(messageAdminVo.getJpId());
                }
            });
            if (CollectionUtils.isNotEmpty(msgIds)) {
                List<PushReceived> pushReceivedList = ResponseUtils.getResponseData(pushAPI.getReceived(msgIds));
                if (CollectionUtils.isNotEmpty(pushReceivedList)) {
                    entities.forEach(messageAdminVo -> {
                        if (org.apache.commons.lang.StringUtils.isNotBlank(messageAdminVo.getJpId())) {
                            PushReceived pushReceived = getPushReceived(pushReceivedList, messageAdminVo.getJpId());
                            if (pushReceived != null) {
                                int receivedCount = 0;
                                receivedCount += pushReceived.getAndroid_received();
                                receivedCount += pushReceived.getIos_msg_received();
                                messageAdminVo.setReceivedNumber(Long.valueOf(receivedCount));
                                int pushUserSize = CollectionUtils.isNotEmpty(messageAdminVo.getPushUserIds()) ? messageAdminVo.getPushUserIds().size() : 0;
                                if (pushUserSize > 0) {
                                    if (receivedCount <= pushUserSize) {
                                        NumberFormat numberFormat = NumberFormat.getInstance();
                                        // 设置精确到小数点后2位
                                        numberFormat.setMaximumFractionDigits(2);
                                        String result = numberFormat.format((float) receivedCount / (float) pushUserSize * 100);
                                        messageAdminVo.setReceivedRate(result + "%");
                                    } else {
                                        LOGGER.warn("receivedRate_fix");
                                        messageAdminVo.setReceivedRate("100%");
                                    }
                                }

                            }
                        }

                    });
                }

            }

        }

        return pageList;
    }

    private PushReceived getPushReceived(List<PushReceived> pushReceivedList, String messageId) {
        if (CollectionUtils.isNotEmpty(pushReceivedList)) {
            for (PushReceived pushReceived : pushReceivedList) {
                if (pushReceived != null && org.apache.commons.lang.StringUtils.equals(pushReceived.getMsg_id(), messageId)) {
                    return pushReceived;
                }
            }
        }
        return null;
    }

    @Override
    public MessageAdminVo push(MessageAdminVo messageAdminVo) {
        if (messageAdminVo.getPushType() == null) {
            throw QuanhuException.busiError("pushType不能为空！");
        }

        String msg = "";
        messageAdminVo.setMessageId(IdGen.uuid());
        try {
            if (messageAdminVo.getPushType().equals(MessageContants.PUSH_TYPE_START)) {
                messageAdminVo.setPushStatus(MessageContants.PUSH_STATUS_ING);
                MessageAdminVo adminVo = messageAdminMongo.save(messageAdminVo);
                msg = JSON.toJSONString(messageAdminVo);
                messageSender.send(msg);
                return adminVo;
            }
            return messageAdminMongo.save(messageAdminVo);
        } catch (Exception e) {
            LOGGER.error("发送消息失败! " + msg, e);
            throw QuanhuException.busiError("发送消息失败!" + e);
        }
    }

    @Override
    public void handleMessage(MessageAdminVo messageAdminVo) {
        try {
            if (messageAdminVo != null && messageAdminVo.getPushType().equals(MessageContants.PUSH_TYPE_START)) {
                //立即推送,判断是否是持久化消息，写入mongo,推极光
                messageHandle(messageAdminVo);

            } else if (messageAdminVo != null && messageAdminVo.getPushType().equals(MessageContants.PUSH_TYPE_TIMING)) {
                //定时推送
                timePushMessage(messageAdminVo);
            }
        } catch (Exception e) {
            LOGGER.error("==========处理管理后台推送消息异常！============", e);
            updatePushStatus(messageAdminVo, MessageContants.PUSH_STATUS_FAILURE);
            throw QuanhuException.busiError("处理管理后台推送消息异常" + e);
        }
    }

    private void messageHandle(MessageAdminVo messageAdminVo) {
        if (messageAdminVo.getPersistentType().equals(MessageContants.NOT_PERSISTENT)) {
            //不持久化：直接推送极光
            pushMessage(messageAdminVo);

        } else if (messageAdminVo.getPersistentType().equals(MessageContants.PERSISTENT)) {
            //持久化：先插库，再推极光
            List<String> pushUserIds = messageAdminVo.getPushUserIds();
            if (CollectionUtils.isNotEmpty(pushUserIds)) {
                MessageVo messageVo = new MessageVo();
                BeanUtils.copyProperties(messageVo, messageAdminVo);
                pushUserIds.stream().forEach(
                        userId -> {
                            messageVo.setToCust(userId);
                            messageVo.setCreateTime(DateUtils.getDateTime());
                            messageService.sendMessage(messageVo, false);
                        }
                );

                pushMessage(messageAdminVo);
            }
        }
    }

    private void pushMessage(MessageAdminVo messageAdminVo) {
        MessageVo messageVo = new MessageVo();
        BeanUtils.copyProperties(messageVo, messageAdminVo);
        PushReqVo reqVo = new PushReqVo();
        reqVo.setPushType(PushReqVo.CommonPushType.BY_ALIASS);
        reqVo.setCustIds(messageAdminVo.getPushUserIds());
        reqVo.setNotification(messageAdminVo.getTitle());
        reqVo.setMsg(JsonUtils.toFastJson(messageVo));
        LOGGER.info("=================开始推送管理后台消息！=================");
        try {
            Response<Boolean> booleanResponse = pushAPI.commonSendAlias(reqVo);
            if (!booleanResponse.success()) {

                LOGGER.info("=================推送管理后台消息失败！=================");
                updatePushStatus(messageAdminVo, MessageContants.PUSH_STATUS_FAILURE);
            } else {

                LOGGER.info("=================推送管理后台消息成功！=================");
                updatePushStatus(messageAdminVo, MessageContants.PUSH_STATUS_END);
            }
        } catch (Exception e) {

            LOGGER.error("=================推送管理后台消息异常！=================", e);
            updatePushStatus(messageAdminVo, MessageContants.PUSH_STATUS_FAILURE);
            throw QuanhuException.busiError("处理管理后台推送消息异常" + e);
        }
    }

    private void updatePushStatus(MessageAdminVo messageAdminVo, Integer pushStatus) {
        messageAdminVo.setPushStatus(pushStatus);
        messageAdminMongo.update(messageAdminVo);
    }

    private void timePushMessage(MessageAdminVo messageAdminVo) {
        if (StringUtils.isNotBlank(messageAdminVo.getPushDate())) {
            Long millisecond = DateUtils.getMillisecond(DateUtils.getDateTime(), messageAdminVo.getPushDate());

            if (millisecond != null) {
                ScheduledFuture<?> schedule = null;
                try {
                    schedule = executorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            messageHandle(messageAdminVo);
                        }
                    }, millisecond, TimeUnit.MILLISECONDS);
                    MessageMap.MAP.put(QH_MESSAGE_ADMIN_SCHEDULE + messageAdminVo.getMessageId(), schedule);
                   /* RedisTemplate<String, ScheduledFuture> redisTemplate = redisTemplateBuilder.buildRedisTemplate(ScheduledFuture.class);
                    redisTemplate.opsForValue().set(QH_MESSAGE_ADMIN_SCHEDULE + messageAdminVo.getMessageId(), schedule, millisecond, TimeUnit.MILLISECONDS);*/
                } catch (Exception e) {
                    LOGGER.error("设置定时任务异常,开始取消定时任务", e);

                    if (schedule != null) {
                        boolean cancel = schedule.cancel(true);
                        if (!cancel) {
                            LOGGER.error("设置定时任务异常,取消定时任务错误！", e);
                            throw QuanhuException.busiError("设置定时任务异常,取消定时任务错误！");
                        }
                    }

                    LOGGER.error("设置定时任务异常,取消定时任务完成！", e);
                    throw QuanhuException.busiError("设置定时任务异常，已取消定时任务！");
                }
            }
        }
    }

    @Override
    public MessageAdminVo update(MessageAdminVo messageAdminVo) {
        //管理后台编辑功能：先取消之前的定时任务，然后在发往队列
        /*RedisTemplate<String, ScheduledFuture> redisTemplate = redisTemplateBuilder.buildRedisTemplate(ScheduledFuture.class);
        ScheduledFuture<?> scheduledFuture = redisTemplate.opsForValue().get(QH_MESSAGE_ADMIN_SCHEDULE + messageAdminVo.getMessageId());*/
        /*ScheduledFuture scheduledFuture = MessageMap.MAP.get(QH_MESSAGE_ADMIN_SCHEDULE + messageAdminVo.getMessageId());
        boolean cancel = false;
        if (scheduledFuture != null) {
            cancel = scheduledFuture.cancel(true);
            if (cancel) {
                if (deleteMessage(messageAdminVo)) return true;

                String msg = JSON.toJSONString(messageAdminVo);
                messageSender.send(msg);
                messageAdminMongo.update(messageAdminVo);
                return true;
            }
            LOGGER.error("===========取消定时任务出错！===========messageId-->" + QH_MESSAGE_ADMIN_SCHEDULE + messageAdminVo.getMessageId());
            throw QuanhuException.busiError("取消定时任务出错！messageId-->" + QH_MESSAGE_ADMIN_SCHEDULE + messageAdminVo.getMessageId());
        }

        return deleteMessage(messageAdminVo);*/
        return messageAdminMongo.update(messageAdminVo);
    }

    private boolean deleteMessage(MessageAdminVo messageAdminVo) {
        Integer delFlag = messageAdminVo.getDelFlag();
        if (delFlag != null && delFlag.equals(MessageContants.DEL_FLAG_DELETE)) {
            messageAdminMongo.update(messageAdminVo);
            return true;
        }
        return false;
    }

    @Override
    public MessageAdminVo findOne(MessageAdminDto messageAdminDto) {
        return messageAdminMongo.findOne(messageAdminDto);
    }

    @Override
    public List<MessageAdminVo> startCheck() {
        return messageAdminMongo.startCheck();
    }
}
