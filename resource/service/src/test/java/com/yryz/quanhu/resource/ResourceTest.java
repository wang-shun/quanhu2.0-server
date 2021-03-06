/**
 * Copyright (c) 2018-2019 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * 
 * Created on 2018年1月17日
 * Id: ResourceTest.java, 2018年1月17日 下午1:16:32 yehao
 */
package com.yryz.quanhu.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.GsonUtils;
import com.yryz.common.utils.IdGen;
import com.yryz.quanhu.resource.api.ResourceApi;
import com.yryz.quanhu.resource.dao.canal.ResourceCanalDao;
import com.yryz.quanhu.resource.entity.ResourceModel;
import com.yryz.quanhu.resource.enums.ResourceEnum;
import com.yryz.quanhu.resource.hotspot.service.CalculationService;
import com.yryz.quanhu.resource.service.ResourceService;
import com.yryz.quanhu.resource.vo.ResourceVo;

/**
 * @author yehao
 * @version 2.0
 * @date 2018年1月17日 下午1:16:32
 * @Description 资源管理单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ResourceTest {
	
	@Reference
	private ResourceApi resourceApi;
	
	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private CalculationService calculationService;
	
	@Autowired
	private ResourceCanalDao resourceCanalDao;
	
	@Test
	public void commitResource(){
		List<ResourceVo> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			ResourceVo resource = new ResourceVo();
//			resource.setAudio("yehao-audio" + i);
//			resource.setCityCode("100101");
//			resource.setCompleteTime(System.currentTimeMillis());
//			resource.setContent("yehao-content" + i);
//			resource.setCoterieId("coterieId");
//			resource.setCreateTime(System.currentTimeMillis());
//			resource.setCustId("yehao-test-id");
//			resource.setExtjson("{}");
//			resource.setGps("113.11,34.22");
//			resource.setHeat(0L);
//			resource.setModuleEnum("1000-1120");
//			resource.setPics("yehao-pics" + i);
//			resource.setPrice(0l);
//			resource.setResourceId(IdGen.uuid());
//			resource.setResourceTag("yehao-resourceTag");
//			resource.setResourceType(ResourceTypeEnum.RELEASE);
//			resource.setSummary("yehao-summary");
//			resource.setTalentType(ResourceEnum.TALENT_TYPE_FALSE);
//			resource.setThumbnail("yehao-thumbnail");
//			resource.setTitle("yehao-title");
//			resource.setUpdateTime(System.currentTimeMillis());
//			resource.setVideo("yehao-video" + i);
//			resource.setVideoPic("yehao-videoPic" + i);
			System.out.println("resourceId:" + resource.getResourceId());
			list.add(resource);
		}
		resourceApi.commitResource(list);
	}
	
	@Test
	public void updateResource(){
		List<ResourceVo> list = new ArrayList<>();
			ResourceVo resource = new ResourceVo();
//			resource.setResourceId("02d5cf323c3641779eeb2fff65b291b0");
//			resource.setAudio("yehao-audio" + "-update");
//			resource.setCityCode("100101");
//			resource.setCompleteTime(System.currentTimeMillis());
			resource.setContent("yehao-content" + "-update");
			resource.setCoterieId("coterieId");
			list.add(resource);
		resourceApi.commitResource(list);
	}
	
	@Test
	public void getReources(){
		ResourceVo resource = new ResourceVo();
//		resource.setCustId("yehao-test-id");
//		resource.setTitle("title");
//		resource.setModuleEnum(ResourceTypeEnum.RELEASE + "," + ResourceTypeEnum.TOPIC );
		resource.setCoterieId("1");
		Response<List<ResourceVo>> resonse = resourceApi.getResources(resource, "", 0, 10, "2018-2-1 1:1:1", "2018-2-17 14:1:1");
		System.out.println(GsonUtils.parseJson(resonse.getData()));
	}
	
	@Test
	public void getMap(){
		Set<String> ids = new HashSet<>();
		ids.add("367280144269312");
		Map<String, ResourceModel> map = resourceService.getResources(ids);
		System.out.println(GsonUtils.parseJson(map));
		System.out.println("");
	}
	
	@Test
	public void getResource(){
		ResourceVo resourceVo = resourceApi.getResourcesById("1000211").getData();
		System.out.println(GsonUtils.parseJson(resourceVo));
	}
	
	@Test
	public void createResource(){
		resourceService.createRecommend();
	}
	
	@Test
	public void appRecommend(){
		List<ResourceVo> list = resourceApi.appRecommend(0, 10).getData();
		System.out.println(GsonUtils.parseJson(list));
	}
	
	@Test
	public void calculation(){
		calculationService.calculation();
	}
	
	@Test
	public void attenuation(){
		calculationService.attenuation();
	}
	
	@Test
	public void sendToCanal(){
		resourceCanalDao.sendToCannel("100001", 0L);
	}

}