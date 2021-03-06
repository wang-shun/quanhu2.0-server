package com.yryz.quanhu.dymaic.canal.rabbitmq.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import com.yryz.common.entity.CanalChangeInfo;
import com.yryz.common.entity.CanalMsgContent;
import com.yryz.common.utils.CanalEntityParser;
import com.yryz.quanhu.dymaic.canal.constant.CommonConstant;
import com.yryz.quanhu.dymaic.canal.dao.ResourceInfoRepository;
import com.yryz.quanhu.dymaic.canal.entity.ReleaseInfo;
import com.yryz.quanhu.dymaic.canal.entity.ResourceHeat;
import com.yryz.quanhu.dymaic.canal.entity.ResourceInfo;
import com.yryz.quanhu.dymaic.canal.entity.TopicInfo;
import com.yryz.quanhu.dymaic.canal.entity.TopicPostInfo;
import com.yryz.quanhu.dymaic.canal.rabbitmq.ElasticsearchSyncConsumer;

@Component
public class ResourceInfoEsHandlerImpl implements SyncHandler{
	private static Logger logger = LoggerFactory.getLogger(ResourceInfoEsHandlerImpl.class);
	@Resource
	private SyncExecutor syncExecutor;
	
	@Resource
	private ElasticsearchTemplate elasticsearchTemplate;
	
	@Resource
	private ResourceInfoRepository resourceInfoRepository;
	
	@PostConstruct
	private void register() {
		syncExecutor.register(this);
	}
	
	@Override
	public Boolean watch(CanalMsgContent msg) {
		if (CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& (CommonConstant.QuanHuDb.TABLE_TOPIC.equals(msg.getTableName())
				|| CommonConstant.QuanHuDb.TABLE_TOPIC_POST.equals(msg.getTableName())
				|| CommonConstant.QuanHuDb.TABLE_RELEASE_INFO.equals(msg.getTableName()))
				|| CommonConstant.QuanHuDb.TABLE_MONGODB_RESOURCE_HEAT.equals(msg.getTableName())) {
			return true;
		}
		return false;
	}

	@Override
	public void handler(CanalMsgContent msg) {
		if (CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& CommonConstant.QuanHuDb.TABLE_TOPIC.equals(msg.getTableName())) {
			doTopicInfo(msg);
		}
		if(CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& CommonConstant.QuanHuDb.TABLE_TOPIC_POST.equals(msg.getTableName())){
			doTopicPostInfo(msg);
		}
		if(CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& CommonConstant.QuanHuDb.TABLE_RELEASE_INFO.equals(msg.getTableName())){
			doReleaseInfo(msg);
		}
		if(CommonConstant.QuanHuDb.DB_NAME.equals(msg.getDbName())
				&& CommonConstant.QuanHuDb.TABLE_MONGODB_RESOURCE_HEAT.equals(msg.getTableName())){
			doHeatInfo(msg);
		}
	}
	
	/**
	 * 话题
	 * @param msg
	 */
	private void doTopicInfo(CanalMsgContent msg){
		TopicInfo uinfoBefore = CanalEntityParser.parse(msg.getDataBefore(),TopicInfo.class);
		TopicInfo uinfoAfter = CanalEntityParser.parse(msg.getDataAfter(),TopicInfo.class);
		if (CommonConstant.EventType.OPT_UPDATE.equals(msg.getEventType())) {
			Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(uinfoBefore.getKid());
			if(uinfo.isPresent()){
				ResourceInfo resource=uinfo.get();
				TopicInfo topicInfo = CanalEntityParser.parse(resource.getTopicInfo(), msg.getDataAfter(),TopicInfo.class);
				resource.setKid(topicInfo.getKid());
				resource.setTopicInfo(topicInfo);
				resource.setResourceType(1);//话题
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resourceInfoRepository.save(resource);
			}else{
				ResourceInfo resource=new ResourceInfo();
				resource.setKid(uinfoAfter.getKid());
				resource.setTopicInfo(uinfoAfter);
				resource.setResourceType(1);//话题
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resource.setLastHeat(0L);
				resourceInfoRepository.save(resource);
			}
		}
		else if (CommonConstant.EventType.OPT_DELETE.equals(msg.getEventType())) {
			resourceInfoRepository.deleteById(uinfoBefore.getKid());
		}
		else if (CommonConstant.EventType.OPT_INSERT.equals(msg.getEventType())) {
			// 先执行了update则不执行insert
			Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(uinfoAfter.getKid());
			if (!uinfo.isPresent()) {
				ResourceInfo resource=new ResourceInfo();
				resource.setKid(uinfoAfter.getKid());
				resource.setTopicInfo(uinfoAfter);
				resource.setResourceType(1);//话题
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resource.setLastHeat(0L);
				resourceInfoRepository.save(resource);
			}
		}
	}
	
	/**
	 * 帖子
	 * @param msg
	 */
	private void doTopicPostInfo(CanalMsgContent msg){
		TopicPostInfo uinfoBefore = CanalEntityParser.parse(msg.getDataBefore(),TopicPostInfo.class);
		TopicPostInfo uinfoAfter = CanalEntityParser.parse(msg.getDataAfter(),TopicPostInfo.class);
		if (CommonConstant.EventType.OPT_UPDATE.equals(msg.getEventType())) {
			Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(uinfoBefore.getKid());
			if(uinfo.isPresent()){
				ResourceInfo resource=uinfo.get();
				TopicPostInfo topicPostInfo = CanalEntityParser.parse(resource.getTopicPostInfo(), msg.getDataAfter(),TopicPostInfo.class);
				resource.setKid(topicPostInfo.getKid());
				resource.setTopicPostInfo(topicPostInfo);
				resource.setResourceType(2);//帖子
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resourceInfoRepository.save(resource);
			}else{
				ResourceInfo resource=new ResourceInfo();
				resource.setKid(uinfoAfter.getKid());
				resource.setTopicPostInfo(uinfoAfter);
				resource.setResourceType(2);//帖子
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resource.setLastHeat(0L);
				resourceInfoRepository.save(resource);
			}
		}
		else if (CommonConstant.EventType.OPT_DELETE.equals(msg.getEventType())) {
			resourceInfoRepository.deleteById(uinfoBefore.getKid());
		}
		else if (CommonConstant.EventType.OPT_INSERT.equals(msg.getEventType())) {
			// 先执行了update则不执行insert
			Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(uinfoAfter.getKid());
			if (!uinfo.isPresent()) {
				ResourceInfo resource=new ResourceInfo();
				resource.setKid(uinfoAfter.getKid());
				resource.setTopicPostInfo(uinfoAfter);
				resource.setResourceType(2);//帖子
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resource.setLastHeat(0L);
				resourceInfoRepository.save(resource);
			}
		}
	}
	
	/**
	 * 文章
	 */
	private void doReleaseInfo(CanalMsgContent msg){
		ReleaseInfo uinfoBefore = CanalEntityParser.parse(msg.getDataBefore(),ReleaseInfo.class);
		ReleaseInfo uinfoAfter = CanalEntityParser.parse(msg.getDataAfter(),ReleaseInfo.class);
		if (CommonConstant.EventType.OPT_UPDATE.equals(msg.getEventType())) {
			Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(uinfoBefore.getKid());
			if(uinfo.isPresent()){
				ResourceInfo resource=uinfo.get();
				ReleaseInfo releaseInfo = CanalEntityParser.parse(resource.getReleaseInfo(), msg.getDataAfter(),ReleaseInfo.class);
				resource.setKid(releaseInfo.getKid());
				resource.setReleaseInfo(releaseInfo);
				resource.setResourceType(3);//文章
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resourceInfoRepository.save(resource);
			}else{
				ResourceInfo resource=new ResourceInfo();
				resource.setKid(uinfoAfter.getKid());
				resource.setReleaseInfo(uinfoAfter);
				resource.setResourceType(3);//文章
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resource.setLastHeat(0L);
				resourceInfoRepository.save(resource);
			}
		}
		else if (CommonConstant.EventType.OPT_DELETE.equals(msg.getEventType())) {
			resourceInfoRepository.deleteById(uinfoBefore.getKid());
		}
		else if (CommonConstant.EventType.OPT_INSERT.equals(msg.getEventType())) {
			// 先执行了update则不执行insert
			Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(uinfoAfter.getKid());
			if (!uinfo.isPresent()) {
				ResourceInfo resource=new ResourceInfo();
				resource.setKid(uinfoAfter.getKid());
				resource.setReleaseInfo(uinfoAfter);
				resource.setResourceType(3);//文章
				resource.setCreateDate(uinfoAfter.getCreateDate());
				resource.setLastHeat(0L);
				resourceInfoRepository.save(resource);
			}
		}
	}
	
	/**
	 * 资源热度值处理
	 */
	private void doHeatInfo(CanalMsgContent msg){
		ResourceHeat infoAfter = CanalEntityParser.parse(msg.getDataAfter(),ResourceHeat.class);
		if(infoAfter==null || infoAfter.getKid()==null || infoAfter.getLastHeat()==null){
			logger.warn("canal message：资源热度 资源kid和lastHeat");
			return;
		}
		Optional<ResourceInfo> uinfo = resourceInfoRepository.findById(infoAfter.getKid());
		if(uinfo.isPresent()){
			ResourceInfo resource=uinfo.get();
			resource.setLastHeat(infoAfter.getLastHeat());
			resourceInfoRepository.save(resource);
		}
	}
	
}
