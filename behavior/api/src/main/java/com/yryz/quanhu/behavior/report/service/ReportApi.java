package com.yryz.quanhu.behavior.report.service;

import com.yryz.common.response.Response;
import com.yryz.quanhu.behavior.report.entity.Report;

import java.util.Map;

/**
 * @Author:sun
 * @version:2.0
 * @Description:举报
 * @Date:Created in 16:15 2018/1/20
 */
public interface ReportApi {

    /**
     * desc:添加举报{前端}
     * @param:report
     * @return:map
     **/
    Response<Map<String,Integer>> accretion(Report report);


}