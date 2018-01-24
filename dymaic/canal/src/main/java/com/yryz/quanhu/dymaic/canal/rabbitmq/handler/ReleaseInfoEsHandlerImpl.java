package com.yryz.quanhu.dymaic.canal.rabbitmq.handler;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import com.yryz.quanhu.dymaic.canal.constant.CommonConstant;
import com.yryz.quanhu.dymaic.canal.entity.CanalMsgContent;
//import com.yryz.quanhu.dymaic.dao.elasticsearch.ReleaseInfoRepository;

@Component
public class ReleaseInfoEsHandlerImpl implements SyncHandler {
	@Resource
	private SyncExecutor syncExecutor;
	
	@Resource
	private ElasticsearchTemplate elasticsearchTemplate;
	
//	@Resource
//	private ReleaseInfoRepository releaseInfoRepository;
	
	@PostConstruct
	private void register() {
		syncExecutor.register(this);
	}
	
	@Override
	public Boolean watch(CanalMsgContent msg) {
		if (CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& CommonConstant.QuanHuDb.TABLE_RELEASE_INFO.equals(msg.getTableName())) {
			return true;
		}
		return false;
	}

	@Override
	public void handler(CanalMsgContent msg) {
//		boolean exists=elasticsearchTemplate.indexExists(ReleaseInfo.class);
//		if(!exists){
//			elasticsearchTemplate.createIndex(ReleaseInfo.class);
//		}
//		ReleaseInfo uinfoBefore = EntityParser.parse(msg.getDataBefore(),ReleaseInfo.class);
//		ReleaseInfo uinfoAfter = EntityParser.parse(msg.getDataAfter(),ReleaseInfo.class);
//
//		if (CommonConstant.EventType.OPT_UPDATE.equals(msg.getEventType())) {
//			Optional<ReleaseInfo> uinfo = releaseInfoRepository.findById(uinfoBefore.getKid());
//			if (uinfo.isPresent()) {
//				ReleaseInfo userInfo = EntityParser.parse(uinfo.get(), msg.getDataAfter(),ReleaseInfo.class);
//				releaseInfoRepository.save(userInfo);
//			} else {
//				// 先收到了update消息，后收到insert消息
//				releaseInfoRepository.save(uinfoAfter);
//			}
//		} else if (CommonConstant.EventType.OPT_DELETE.equals(msg.getEventType())) {
//			releaseInfoRepository.deleteById(uinfoBefore.getKid());
//		} else if (CommonConstant.EventType.OPT_INSERT.equals(msg.getEventType())) {
//			// 先执行了update则不执行insert
//			Optional<ReleaseInfo> uinfo = releaseInfoRepository.findById(uinfoAfter.getKid());
//			if (!uinfo.isPresent()) {
//				releaseInfoRepository.save(uinfoAfter);
//			}
//		}
	}

}
