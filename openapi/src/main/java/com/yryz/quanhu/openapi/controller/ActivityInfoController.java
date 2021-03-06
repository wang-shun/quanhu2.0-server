package com.yryz.quanhu.openapi.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.yryz.common.annotation.UserBehaviorValidation;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.utils.PatternUtils;
import com.yryz.quanhu.openapi.ApplicationOpenApi;
import com.yryz.quanhu.other.activity.api.ActivityInfoApi;
import com.yryz.quanhu.other.activity.vo.ActivityInfoAppListVo;
import com.yryz.quanhu.other.activity.vo.ActivityInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dell on 2018/1/22.
 */
@Api(description ="活动主体相关接口")
@RestController
public class ActivityInfoController {
    @Reference(check = false, timeout = 30000)
    private ActivityInfoApi activityInfoApi;

    @UserBehaviorValidation(login = true)
    @ApiOperation("我参加/报名的活动列表(token)")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/activity/info/myList")
    public  Response<PageList<ActivityInfoAppListVo>>  myList(Integer currentPage, Integer pageSize, @RequestHeader("userId") String userId, HttpServletRequest request) {
       Assert.notNull(userId, "userId is null");
       return activityInfoApi.myList(currentPage,pageSize,Long.valueOf(userId));
    }

    @UserBehaviorValidation
    @ApiOperation("返回所有的上架活动列表 type:1当前活动 2:往期活动3.推荐活动列表")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/activity/info/list")
    public  Response<PageList<ActivityInfoAppListVo>> appList(Integer currentPage, Integer pageSize, Integer type,HttpServletRequest request) {
        Assert.isTrue(PatternUtils.matcher(type, "1|2|3"), "未知类型！");
        return activityInfoApi.appList(currentPage,pageSize,type);
    }

    @UserBehaviorValidation
    @ApiOperation("返回所有的上架活动固定列表 1当前活动(2条) 2:往期活动(5条)3.推荐活动列表(2条)")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/activity/info/fixedList")
    public  Response<PageList<ActivityInfoAppListVo>> fixedList(Integer type,HttpServletRequest request) {
        Assert.isTrue(PatternUtils.matcher(type, "1|2|3"), "未知类型！");
        return activityInfoApi.fixedList(type);
    }

    @UserBehaviorValidation
    @ApiOperation("获取活动基本信息")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/activity/info/activityInfo")
    public  Response<ActivityInfoVo> getActivityInfoVo(Long kid, Integer type, HttpServletRequest request) {
        Assert.isTrue(PatternUtils.matcher(type, "1|2|3"), "未知类型！");
        Assert.notNull(kid, "kid is null");
        return activityInfoApi.get(kid,type);
    }
}
