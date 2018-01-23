package com.yryz.quanhu.openapi.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.Annotation.NotLogin;
import com.yryz.common.response.Response;
import com.yryz.quanhu.behavior.report.entity.Report;
import com.yryz.quanhu.behavior.report.service.ReportApi;
import com.yryz.quanhu.openapi.ApplicationOpenApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author:sun
 * @version:2.0
 * @Description:举报
 * @Date:Created in 15:08 2018/1/22
 */
@Api(tags = "举报")
@RestController
public class ReportConrtoller {

    @Reference
    private ReportApi reportApi;

    @NotLogin
    @ApiOperation("用户举报")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/report/accretion")
    public Response<Map<String, Integer>> accretion(@RequestBody Report report,@RequestHeader Long userId, HttpServletRequest request) {
        report.setReportUserId(userId);
        return reportApi.accretion(report);
    }

}
