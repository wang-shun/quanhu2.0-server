package com.yryz.quanhu.openapi.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.annotation.NotLogin;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.quanhu.behavior.comment.dto.CommentFrontDTO;
import com.yryz.quanhu.behavior.comment.entity.Comment;
import com.yryz.quanhu.behavior.comment.service.CommentApi;
import com.yryz.quanhu.behavior.comment.vo.CommentVO;
import com.yryz.quanhu.behavior.report.entity.Report;
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
 * @Description:评论
 * @Date:Created in 19:29 2018/1/23
 */
@Api(tags = "评论")
@RestController
public class CommentController {

    @Reference
    private CommentApi commentApi;

    @NotLogin
    @ApiOperation("用户评论")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @PostMapping(value = "/services/app/{version}/comment/accretion")
    public Response<Map<String, Integer>> accretion(@RequestBody Comment comment, @RequestHeader Long userId, HttpServletRequest request) {
        comment.setCreateUserId(userId);
        return commentApi.accretion(comment);
    }

    @NotLogin
    @ApiOperation("用户评论列表")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @GetMapping(value = "/services/app/{version}/comment/list")
    public Response<PageList<CommentVO>> queryComments(CommentFrontDTO commentFrontDTO){
        return commentApi.queryComments(commentFrontDTO);
    }

    @NotLogin
    @ApiOperation("用户删除评论")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
    @DeleteMapping(value = "/services/app/{version}/comment/clean")
    public Response<Map<String, Integer>> delComment(@RequestBody Comment comment,@RequestHeader Long userId){
        comment.setCreateUserId(userId);
        return commentApi.delComment(comment);
    }

}