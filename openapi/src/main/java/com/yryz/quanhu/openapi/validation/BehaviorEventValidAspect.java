package com.yryz.quanhu.openapi.validation;

import com.yryz.common.annotation.UserBehaviorArgs;
import com.yryz.common.annotation.UserBehaviorValidation;
import com.yryz.quanhu.openapi.validation.filter.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * <p>
 * Created on 2018/1/24 11:39
 * Created by huangxy
 *
 *
 *
 */
@Aspect
@Component
public class BehaviorEventValidAspect {

    private static final Logger logger = LoggerFactory.getLogger(BehaviorEventValidAspect.class);

    @Autowired
    private BehaviorArgsBuild behaviorArgsBuild;

    /**
     * 相关验证过滤器实现
     */
    @Autowired
    private UserBlacklistValidFilter userBlacklistValidFilter;
    @Autowired
    private UserIllegalWordsFilter userIllegalWordsFilter;
    @Autowired
    private UserLoginValidFilter userLoginValidFilter;
    @Autowired
    private UserCoterieMuteValidFilter userCoterieMuteValidFilter;
    @Autowired
    private UserMuteValidFilter userMuteValidFilter;
    @Autowired
    private UserCoterieMemberValidFilter userCoterieMemberValidFilter;

    /**
     * 定义切面 扫描注解切面
     */
    @Pointcut("@annotation(com.yryz.common.annotation.UserBehaviorArgs) || @annotation(com.yryz.common.annotation.UserBehaviorValidation)")
    public void behaviorValid(){

    }
    /**
     * 方法执行前校验
     * @param joinPoint
     */
    @Before("behaviorValid()")
    public void beforeValid(JoinPoint joinPoint){

        logger.info("[用户数据权限认证]-------------★start★-------------");

        //获取注解参数
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        //校验规则
        UserBehaviorValidation validation = method.getDeclaredAnnotation(UserBehaviorValidation.class);
        if(validation==null){
            return;
        }

        logger.info("[用户说明]------------★["+validation.event()+"]★-------------");

        //取值方式{注解中都用占位符标识，这需要通过切面方法参数中获取指定值}
        UserBehaviorArgs args = method.getDeclaredAnnotation(UserBehaviorArgs.class);

        //获取必要参数
        Object [] joinPointArgs = joinPoint.getArgs();

        //装载filterChain 循环验证
        BehaviorValidFilterChain filterChain = new BehaviorValidFilterChain();

        //初始化相关公共参数
        Map<String,Object> context = this.initFilterContext(args,joinPointArgs);
        filterChain.getContext().putAll(context);

        //后续相关过滤器实现可以从切面参数中获取自定义参数，
        filterChain.setJoinPoint(joinPoint);
        filterChain.setUserBehaviorArgs(args);
        filterChain.setUserBehaviorValidation(validation);

        //是否校验登录
        if (validation.login()){
            filterChain.addFilter(userLoginValidFilter);
        }
        //是否校验平台禁言
        if(validation.mute()){
            filterChain.addFilter(userMuteValidFilter);
        }
        //是否校验黑名单
        if(validation.blacklist()){
            filterChain.addFilter(userBlacklistValidFilter);
        }
        //是否校验私圈成员
        if(validation.isCoterieMember()){
            filterChain.addFilter(userCoterieMemberValidFilter);
        }
        //是否校验私圈禁言
        if(validation.isCoterieMute()){
            filterChain.addFilter(userCoterieMuteValidFilter);
        }
        //是否校验敏感词
        if(validation.illegalWords()){
            filterChain.addFilter(userIllegalWordsFilter);
        }
        //执行
        filterChain.execute();

        logger.info("[用户数据权限认证]-------------★finish★-------------");
    }


    private Map<String,Object> initFilterContext(UserBehaviorArgs args, Object [] joinPointArgs){
        Map<String,Object> map = new HashMap<>();

        String loginUserKey = args==null?UserBehaviorArgs.DEFAULT_KEY_LOGIN_USER:args.loginUserId();
        String loginTokenKey = args==null?UserBehaviorArgs.DEFAULT_KEY_LOGIN_TOKEN:args.loginToken();

        //初始化登录信息
        map.put("loginUserId",behaviorArgsBuild.getParameterValue(loginUserKey,joinPointArgs));
        map.put("loginToken",behaviorArgsBuild.getParameterValue(loginTokenKey,joinPointArgs));

        if(args!=null){
            map.put("sourceUserId",behaviorArgsBuild.getParameterValue(args.sourceUserId(),joinPointArgs));
            map.put("sourceId",behaviorArgsBuild.getParameterValue(args.sourceId(),joinPointArgs));
            map.put("coterieId",behaviorArgsBuild.getParameterValue(args.coterieId(),joinPointArgs));
        }
        return map;
    }
}
