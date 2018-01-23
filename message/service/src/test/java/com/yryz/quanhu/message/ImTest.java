package com.yryz.quanhu.message;

import com.alibaba.fastjson.JSON;
import com.yryz.common.response.Response;
import com.yryz.quanhu.message.im.api.ImAPI;
import com.yryz.quanhu.message.im.entity.BlackAndMuteListVo;
import com.yryz.quanhu.message.im.entity.ImRelation;
import com.yryz.quanhu.message.push.api.PushAPI;
import com.yryz.quanhu.message.push.entity.PushReqVo;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author zhangkun
 * @version 1.0
 * @date 2018/1/19
 * @description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ImTest {

    @Autowired
    ImAPI imAPI;

    @Test
    public void setSpecialRelationTest() {
        ImRelation imRelation = new ImRelation();
        imRelation.setUserId("626942183000989696");
        imRelation.setTargetUserId("626942183ifjeofjeo");
        imRelation.setRelationType("1");
        imRelation.setRelationValue("1");
        Response<Boolean> response = imAPI.setSpecialRelation(imRelation);
        System.out.println("setSpecialRelation: " + JSON.toJSONString(response));
    }

    @Test
    public void blackAndMuteListTest() {
        ImRelation imRelation = new ImRelation();
        imRelation.setUserId("626942183000989696");
        Response<BlackAndMuteListVo> blackAndMuteList = imAPI.listBlackAndMuteList(imRelation);
        System.out.println("blackAndMuteList: " + JSON.toJSONString(blackAndMuteList));
    }
}