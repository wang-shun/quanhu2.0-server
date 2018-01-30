package com.yryz.quanhu.openapi.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.annotation.NotLogin;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.quanhu.coterie.member.constants.MemberConstant;
import com.yryz.quanhu.coterie.member.service.CoterieMemberAPI;
import com.yryz.quanhu.coterie.member.vo.*;
import com.yryz.quanhu.openapi.ApplicationOpenApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chengyunfei
 * @date 2018/1/25.
 */
@Api(description = "私圈成员相关接口")
@RestController
public class CoterieMemberController {
    @Reference(check = false, timeout = 30000)
    private CoterieMemberAPI coterieMemberAPI;

    @ApiOperation("用户申请加入私圈")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/coterie/member/join")
    public Response<CoterieMemberVoForJoin> join(@RequestHeader("userId") Long userId, Long coterieId, String reason) {
        return coterieMemberAPI.join(userId, coterieId, reason);
    }

    @ApiOperation("圈主踢出私圈成员")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/coterie/member/kick")
    public Response<String> kick(@RequestHeader("userId") Long userId, Long memberId, Long coterieId, String reason) {

        return coterieMemberAPI.kick(userId, memberId, coterieId, reason);
    }

    @ApiOperation("圈粉退出私圈")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/coterie/member/quit")
    public Response<String> quit(@RequestHeader("userId") Long userId, Long coterieId) {
        return coterieMemberAPI.quit(userId, coterieId);
    }

    @ApiOperation("设置禁言/取消禁言")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/coterie/member/banSpeak")
    public Response banSpeak(@RequestHeader("userId") Long userId, Long memberId, Long coterieId, Integer type) {

        return coterieMemberAPI.banSpeak(userId, memberId, coterieId, type);
    }

    @ApiOperation("获取用户与私圈的权限关系")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/coterie/member/permission")
    public Response permission(@RequestHeader("userId") Long userId, Long coterieId) {

        CoterieMemberVoForPermission permissionResult = new CoterieMemberVoForPermission();
        permissionResult.setPermission(ResponseUtils.getResponseData(coterieMemberAPI.permission(userId, coterieId)));

        return new Response<>(permissionResult);

    }

    @ApiOperation("成员申请加入私圈的审批")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/coterie/member/audit")
    public Response audit(@RequestHeader("userId") Long userId, Long memberId, Long coterieId) {

        coterieMemberAPI.audit(userId, memberId, coterieId, MemberConstant.MemberStatus.PASS.getStatus());

        return new Response();
    }

    @ApiOperation("私圈新审请的成员数量")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/coterie/member/newMemberNum")
    public Response<CoterieMemberVoForNewMemberCount> newMemberNum(Long coterieId) {

        CoterieMemberVoForNewMemberCount newMemberCount = new CoterieMemberVoForNewMemberCount();
        newMemberCount.setCount(ResponseUtils.getResponseData(coterieMemberAPI.queryNewMemberNum(coterieId)));

        return new Response<>(newMemberCount);
    }

    @ApiOperation("申请加入私圈列表")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/coterie/member/applyList")
    public Response applyList(Long coterieId, Integer pageNo, Integer pageSize) {

        Response<PageList<CoterieMemberApplyVo>> result = coterieMemberAPI.queryMemberApplyList(coterieId, pageNo, pageSize);
        return result;
    }

    @ApiOperation("私圈成员列表")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/coterie/member/list")
    public Response list(Long coterieId, Integer pageNo, Integer pageSize) {

        return coterieMemberAPI.queryMemberList(coterieId, pageNo, pageSize);
    }
}
