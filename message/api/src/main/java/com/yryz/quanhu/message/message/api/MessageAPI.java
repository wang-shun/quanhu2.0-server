package com.yryz.quanhu.message.message.api;

import com.yryz.common.response.Response;
import com.yryz.quanhu.message.message.dto.MessageDto;
import com.yryz.quanhu.message.message.vo.MessageStatusVo;
import com.yryz.quanhu.message.message.vo.MessageVo;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 *
 * @Description:
 * @Date: Created in 2018 2018/1/20 17:53
 * @Author: pn
 */
public interface MessageAPI {

    /**
     * 获取未读消息
     *
     * @param userId
     * @return
     */
    Response<Map<String, String>> getUnread(Long userId);

    /**
     * 清理未读消息
     *
     * @param messageDto
     * @return
     */
    Response<MessageStatusVo> clearUnread(MessageDto messageDto);

    /**
     * 获取消息列表
     *
     * @param messageDto
     * @return
     */
    Response<List<MessageVo>> getList(MessageDto messageDto);

    /**
     * 获取消息总览
     *
     * @param userId
     * @return
     */
    Response<Map<Integer,MessageVo>> getMessageCommon(Long userId);
}