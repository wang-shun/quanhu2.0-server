package com.yryz.quanhu.dymaic.canal.rabbitmq.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.yryz.common.entity.CanalMsgContent;
import com.yryz.common.utils.CanalEntityParser;
import com.yryz.quanhu.dymaic.canal.constant.CommonConstant;
import com.yryz.quanhu.dymaic.canal.dao.UserRepository;
import com.yryz.quanhu.dymaic.canal.entity.UserInfo;

/**
 * 利用MQ处理canal消息处理
 *
 * @author jk
 */
@Component
public class UserInfoEsHandlerImpl implements SyncHandler {
	private static Logger logger = LoggerFactory.getLogger(UserInfoEsHandlerImpl.class);
	@Resource
	private SyncExecutor syncExecutor;

	@Resource
	private UserRepository userRepository;
	
	@Resource
	private ElasticsearchTemplate elasticsearchTemplate;

	@PostConstruct
	private void register() {
		syncExecutor.register(this);
	}

	@Override
	public Boolean watch(CanalMsgContent msg) {
		if (CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& CommonConstant.QuanHuDb.TABLE_USER.equals(msg.getTableName())) {
			return true;
		}
		return false;
	}

	@Override
	public void handler(CanalMsgContent msg) {
//		boolean exists=elasticsearchTemplate.indexExists(UserInfo.class);
//		if(!exists){
//			elasticsearchTemplate.createIndex(UserInfo.class);
//		}
		UserInfo uinfoBefore = CanalEntityParser.parse(msg.getDataBefore(),UserInfo.class);
		UserInfo uinfoAfter = CanalEntityParser.parse(msg.getDataAfter(),UserInfo.class);
		if (CommonConstant.EventType.OPT_UPDATE.equals(msg.getEventType())) {
			Optional<UserInfo> uinfo = userRepository.findById(uinfoBefore.getUserId());
			if (uinfo.isPresent()) {
				UserInfo userInfo = CanalEntityParser.parse(uinfo.get(), msg.getDataAfter(),UserInfo.class);
				userRepository.save(userInfo);
			} else {
				// 先收到了update消息，后收到insert消息
				userRepository.save(uinfoAfter);
			}
		} else if (CommonConstant.EventType.OPT_DELETE.equals(msg.getEventType())) {
			userRepository.deleteById(uinfoBefore.getUserId());
		} else if (CommonConstant.EventType.OPT_INSERT.equals(msg.getEventType())) {
			// 先执行了update则不执行insert
			Optional<UserInfo> uinfo = userRepository.findById(uinfoAfter.getUserId());
			if (!uinfo.isPresent()) {
				userRepository.save(uinfoAfter);
			}
		}
	}

}
