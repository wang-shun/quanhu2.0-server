package com.yryz.quanhu.coterie.member.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chengyunfei
 */
public class CoterieMemberVoForPermission implements Serializable {

    /**
     * 圈主10 成员20 路人未审请30 路人待审核40
     */
    private Byte permission;


    public Byte getPermission() {
        return permission;
    }

    public void setPermission(Byte permission) {
        this.permission = permission;
    }
}


