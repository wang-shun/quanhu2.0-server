/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * 
 * Created on 2018年1月5日
 * Id: AuthConfigVO.java, 2018年1月5日 下午4:00:11 Administrator
 */
package com.yryz.common.config;

import java.io.Serializable;

import org.springframework.context.annotation.Configuration;

/**
 * 认证配置
 * @author danshiyu
 * @version 1.0
 * @date 2018年1月5日 下午4:00:11
 */
@SuppressWarnings("serial")
@Configuration
public class AuthConfig implements Serializable{
	/**
	 * web端token过期时间/小时
	 */
	private Integer webTokenExpire = 2;
	/**
	 * app端短期token过期时间/小时
	 */
	private Integer tokenExpire = 2;
	/**
	 * token每次刷新有效次数，过期时间是token过期时间的一半
	 */
	private Integer tokenRefreshTimes = 3;
	/**
	 * app端长期token过期时间/天
	 */
	private Integer refreshExpire = 30;
	
	/**
	 * refreshToken过期时间后延条件的时间点，即距离refreshExpire过期前的时间范围/小时
	 */
	private Integer refreshTokenDelayExpireTime = 24;
	
	/**
	 * 保存临时旧token的过期时间/小时
	 */
	private Integer tempTokenExpireTime = 4;
	public Integer getWebTokenExpire() {
		return webTokenExpire;
	}
	public void setWebTokenExpire(Integer webTokenExpire) {
		this.webTokenExpire = webTokenExpire;
	}
	public Integer getTokenExpire() {
		return tokenExpire;
	}
	public void setTokenExpire(Integer tokenExpire) {
		this.tokenExpire = tokenExpire;
	}
	public Integer getRefreshExpire() {
		return refreshExpire;
	}
	public void setRefreshExpire(Integer refreshExpire) {
		this.refreshExpire = refreshExpire;
	}
	public Integer getRefreshTokenDelayExpireTime() {
		return refreshTokenDelayExpireTime;
	}
	public void setRefreshTokenDelayExpireTime(Integer refreshTokenDelayExpireTime) {
		this.refreshTokenDelayExpireTime = refreshTokenDelayExpireTime;
	}
	public Integer getTokenRefreshTimes() {
		return tokenRefreshTimes;
	}
	public void setTokenRefreshTimes(Integer tokenRefreshTimes) {
		this.tokenRefreshTimes = tokenRefreshTimes;
	}
	public Integer getTempTokenExpireTime() {
		return tempTokenExpireTime;
	}
	public void setTempTokenExpireTime(Integer tempTokenExpireTime) {
		this.tempTokenExpireTime = tempTokenExpireTime;
	}
	/**
	 * 
	 * @exception 
	 */
	public AuthConfig() {
		super();
	}
	/**
	 * @param webTokenExpire
	 * @param tokenExpire
	 * @param refreshExpire
	 * @exception 
	 */
	public AuthConfig(Integer webTokenExpire, Integer tokenExpire, Integer refreshExpire) {
		super();
		this.webTokenExpire = webTokenExpire;
		this.tokenExpire = tokenExpire;
		this.refreshExpire = refreshExpire;
	}
	@Override
	public String toString() {
		return "AuthConfig [webTokenExpire=" + webTokenExpire + ", tokenExpire=" + tokenExpire + ", refreshExpire="
				+ refreshExpire + "]";
	}

}
