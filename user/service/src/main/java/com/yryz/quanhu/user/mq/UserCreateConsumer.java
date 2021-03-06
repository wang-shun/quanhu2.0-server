package com.yryz.quanhu.user.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yryz.common.constant.AppConstants;
import com.yryz.common.context.Context;
import com.yryz.common.utils.JsonUtils;
import com.yryz.common.utils.StringUtils;
import com.yryz.quanhu.resource.hotspot.api.HotSpotApi;
import com.yryz.quanhu.user.dto.RegisterDTO;
import com.yryz.quanhu.user.entity.UserOperateInfo;
import com.yryz.quanhu.user.manager.EventManager;
import com.yryz.quanhu.user.manager.ImManager;
import com.yryz.quanhu.user.manager.MessageManager;
import com.yryz.quanhu.user.service.UserOperateService;

/**
 * 用户附属信息创建以及连带业务执行
 * @author danshiyu
 *
 */
@Service
public class UserCreateConsumer {
	private static final Logger logger = LoggerFactory.getLogger(UserCreateConsumer.class);
	@Autowired
	private ImManager imManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private UserOperateService operateService;
	@Autowired
	private MessageManager messageManager;
	@Reference
	private HotSpotApi hotApi;
	
    @Value("${user.user.mq.create.queue}")
    private String mqQueue;
    
	/**
	 * QueueBinding: exchange和queue的绑定
	 * Queue:队列声明
	 * Exchange:声明exchange
	 * key:routing-key
	 * @param data
	 */
	@RabbitListener(bindings = @QueueBinding(
			value= @Queue(value="${user.user.mq.create.queue}",durable="true"),
			exchange=@Exchange(value=MqConstants.USER_DIRECT_EXCHANGE,ignoreDeclarationExceptions="true",type=ExchangeTypes.DIRECT),
			key="${user.user.mq.create.queue}")
	)
	public void handleMessage(String data){
		if(logger.isDebugEnabled()){
			logger.debug("[user_reg_create]:params:{}",data);
		}
		RegisterDTO registerDTO = JsonUtils.fromJson(data, new TypeReference<RegisterDTO>() {
		});
		if(registerDTO == null || registerDTO.getRegLogDTO() == null || registerDTO.getRegLogDTO().getUserId() == null){
			return;
		}
		//用户注册信息创建
		createRegInfo(registerDTO);
		String appId = registerDTO.getRegLogDTO().getAppId();
		//创建im账号
		imManager.addImUser(registerDTO.getRegLogDTO().getUserId(), registerDTO.getRegLogDTO().getAppId());
		
		//圈乎用户才加积分和发消息
		if(StringUtils.isNotBlank(appId) && StringUtils.equals(appId, Context.getProperty(AppConstants.APP_ID))){
			//非管理后台用户才加积分和发送消息
			if(!StringUtils.startsWith(registerDTO.getRegLogDTO().getRegType(), "admin")){
				//注册加积分，邀请好友加积分
				eventManager.register(registerDTO.getRegLogDTO().getUserId().toString(), registerDTO.getUserPhone(), registerDTO.getRegLogDTO().getRegType(), registerDTO.getUserRegInviterCode());
				//注册消息发送
				messageManager.register(registerDTO.getRegLogDTO().getUserId().toString(),registerDTO.getRegLogDTO().getAppId());
			}
			//初始化用户热度
			initHot(registerDTO.getRegLogDTO().getUserId());
		}
		
	}
	
	/**
	 * 用户注册信息创建，更新邀请人数
	 * @param registerDTO
	 */
	public void createRegInfo(RegisterDTO registerDTO){
		try {
			// 创建运营信息
			operateService.save(
					new UserOperateInfo(registerDTO.getRegLogDTO().getUserId(), registerDTO.getUserChannel(), registerDTO.getUserRegInviterCode()));
			operateService.saveRegLog(registerDTO.getRegLogDTO());
			if(StringUtils.isNotBlank(registerDTO.getUserRegInviterCode())){
				//更新邀请人数
				operateService.updateInviterNum(registerDTO.getUserRegInviterCode());
			}
			logger.info("[user_reg_create]:params:{},result:success",JsonUtils.toFastJson(registerDTO));
		} catch (Exception e) {
			logger.error("[user_reg_create]",e);
			logger.info("[user_reg_create]:params:{},result:{}",JsonUtils.toFastJson(registerDTO),e.getMessage());
		}
	}
	
	/**
	 * 用户热度初始化
	 * @param userId
	 */
	public void initHot(Long userId){
		try {
			hotApi.saveHeat("2", userId.toString());
		}catch (Exception e) {
			logger.error("[user_hot_init]",e);
			logger.info("[user_hot_init]:params:{userId:{},type:2},result:{}",userId,e.getMessage());
		}
	}
}
