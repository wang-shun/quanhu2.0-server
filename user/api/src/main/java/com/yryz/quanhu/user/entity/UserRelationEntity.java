package com.yryz.quanhu.user.entity;

import com.yryz.common.entity.GenericEntity;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * <p>
 * Created on 2018/1/18 13:40
 * Created by huangxy
 */
public class UserRelationEntity extends GenericEntity {
    /**
     * 用户源
     */
    private String sourceUserId;
    /**
     * 用户目标
     */
    private String targetUserId;
    /**
     * 关系状态
     */
    private int relationStatus;

    /**
     * 删除标记
     */
    private int delFlag;
    /**
     * 版本号
     */
    private int version;


    public int getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(int relationStatus) {
        this.relationStatus = relationStatus;
    }

    public int getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(int delFlag) {
        this.delFlag = delFlag;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}
