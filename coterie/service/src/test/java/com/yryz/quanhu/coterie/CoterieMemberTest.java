package com.yryz.quanhu.coterie;

import com.alibaba.dubbo.config.annotation.Reference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yryz.common.constant.ModuleContants;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.Response;
import com.yryz.common.utils.JsonUtils;
import com.yryz.quanhu.coterie.member.service.CoterieMemberAPI;
import com.yryz.quanhu.coterie.member.service.impl.CoterieMemberOrderNotifyServiceImpl;
import com.yryz.quanhu.order.sdk.dto.OutputOrder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CoterieMemberTest {
    @Reference
    private CoterieMemberAPI coterieMemberAPI;

    @Autowired
    CoterieMemberOrderNotifyServiceImpl service;

    //用户
    private static Long userId = 377914625728512L;

    //私圈
//    private static Long coterieId = 381091760791552L;
    private static Long coterieId = 377931302281216L;


    //成员
//    private static Long memberId = 752036215052845056L;  //没有钱的
    private static Long memberId = 381098110967808L; // 有钱的
//    private static Long memberId = 367896194736128L; // 没有钱的


    private static String reason_waitting = "【测试】【待审】 " + System.currentTimeMillis();
    private static String reason_join = "【测试】【不审】 " + System.currentTimeMillis();

    @Test
    public void test010_Join() {
//        Response response = coterieMemberAPI.join(730941139577331712L,5536534415L,"我要加入,不审核的");
        Response response = coterieMemberAPI.join(memberId, coterieId, null);
        Response response1 = coterieMemberAPI.join(memberId, coterieId, null);
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test020_audit() {

        try {
            Response response = coterieMemberAPI.audit(userId, memberId, coterieId, null);
            System.out.println(JsonUtils.toFastJson(response));
        } catch (QuanhuException e) {
            e.getMsg();
            e.getErrorMsg();
            System.out.println(JsonUtils.toFastJson(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订单回调执行
     */
    @Test
    public void test021_auditPay() {
        OutputOrder outputOrder = new OutputOrder();

//        {"userId":737251236811898880,"coterieId":233665981858,"reason":"我要加入,要审核的","coterieName":"think","icon":"icon","owner":"727909974996672512","amount":10000}
        outputOrder.setBizContent("{\"userId\":381098110967808,\"coterieId\":367803483840512,\"reason\":\"我要加入,要审核的\",\"coterieName\":\"think\",\"icon\":\"icon\",\"owner\":\"727909974996672512\",\"amount\":10000}");

        outputOrder.setOrderId(20180207164695L);
        outputOrder.setPayType(10);
        outputOrder.setResourceId(367803483840512L);
        outputOrder.setCost(100L);
        outputOrder.setModuleEnum(ModuleContants.COTERIE);
        outputOrder.setCreateDate(new Date());
        outputOrder.setCoterieId(367803483840512L);
        outputOrder.setCreateUserId(381098110967808L);
        service.notify(outputOrder);
    }

    @Test
    public void test030_banSpeak() {
        Response response = coterieMemberAPI.banSpeak(userId, memberId, coterieId, 1);
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test040_permission() {
        Response response = coterieMemberAPI.permission(userId, coterieId);
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test050_newMemberNum() {
        Response response = coterieMemberAPI.queryNewMemberNum(coterieId);
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test060_memberApplyList() {
        Response response = coterieMemberAPI.queryMemberApplyList(coterieId, 1, 10);
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test070_memberList() {
        Response response = coterieMemberAPI.queryMemberList(coterieId, 1, 10);
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test080_kick() {
        Response response = coterieMemberAPI.kick(userId, memberId, coterieId, "就要踢你");
        System.out.println(JsonUtils.toFastJson(response));
    }

    @Test
    public void test090_quit() {
        Response response = coterieMemberAPI.quit(memberId, coterieId);
        System.out.println(JsonUtils.toFastJson(response));
    }


    @Test
    public void test090_isBanSpeak() {
//        Response response = coterieMemberAPI.isBanSpeak(memberId,coterieId);
        Response response = coterieMemberAPI.isBanSpeak(userId, coterieId);
        System.out.println(JsonUtils.toFastJson(response));
    }
}
