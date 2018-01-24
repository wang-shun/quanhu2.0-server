package com.yryz.common.constant;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * <p>
 * Created on 2017/11/1 15:12
 * Created by lifan
 */
public class CommonConstants {
	/**
	 * redis用户数据源
	 */
	public static final String REDIS_SOURCE_USER="USER";
	/**
	 * redis认证数据源
	 */
	public static final String REDIS_SOURCE_AUTH="AUTH";
	
	/**
	 * 默认的列表长度
	 */
	public static int DEFAULT_SIZE = 20;
	
	/**
	 * 应用ID
	 */
	public static final String APP_ID="appId";
	/**
	 * 应用安全码
	 */
	public static final String APP_SECRET="appSecret";
	/**
	 *  设备ID
	 */
	public static final String DEVICE_ID="devId";
	
	/**
	 * 请求来源 1-IOS 2-Android 3-PC
	 */
	public static final String DEV_TYPE = "devType";
	
	/**
	 * token
	 */
	public static final String TOKEN = "token";
	
	/**
	 * custId
	 */
	public static final String CUST_ID = "custId";
	
	/**
	 * web 端dev类型
	 */
	public static final String DEV_TYPE_ANROID = "2";
	public static final String DEV_TYPE_IOS = "1";
	public static final String DEV_TYPE_PC = "3";

    /**
     * 上架
     */
    public static final byte SHELVE_YES = 10;

    /**
     * 未删除
     */
    public static final byte DELETE_NO = 10;

    /**
     * 下架
     */
    public static final byte SHELVE_NO = 11;

    /**
     * 删除
     */
    public static final byte DELETE_YES = 11;


	/**
	 * 推荐
	 */
	public static final byte recommend_YES = 11;

	/**
	 * 非推荐
	 */
	public static final byte recommend_NO = 10;


}
