package com.yryz.quanhu.openapi.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yryz.common.entity.RequestHeader;
import com.yryz.common.response.Response;
import com.yryz.common.utils.WebUtil;
import com.yryz.quanhu.message.commonsafe.api.CommonSafeApi;
import com.yryz.quanhu.message.commonsafe.constants.CommonServiceType;
import com.yryz.quanhu.message.commonsafe.dto.VerifyCodeDTO;
import com.yryz.quanhu.openapi.ApplicationOpenApi;
import com.yryz.quanhu.user.dto.SmsVerifyCodeDTO;
import com.yryz.quanhu.user.service.AccountApi;
import com.yryz.quanhu.user.vo.SmsVerifyCodeVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@Api(description = "公共组件接口")
@RestController
public class ComponentController {
	@Reference
	private AccountApi accountApi;
	@Reference
	private CommonSafeApi commonSafeApi;

	@ApiOperation("短信验证码发送")
	@ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
	@PostMapping(value = "/{version}/component/sendVerifyCode")
	public Response<SmsVerifyCodeVO> sendVerifyCode(@RequestBody SmsVerifyCodeDTO codeDTO, HttpServletRequest request) {
		RequestHeader header = WebUtil.getHeader(request);
		codeDTO.setAppId(header.getAppId());
		return accountApi.sendVerifyCode(codeDTO);
	}

	@ApiOperation("验证码校验（只校验不删除）")
	@ApiImplicitParam(name = "version", paramType = "path", allowableValues = ApplicationOpenApi.CURRENT_VERSION, required = true)
	@GetMapping(value = "/{version}/component/checkVerifyCode")
	public Response<Integer> checkVerifyCode(@RequestBody SmsVerifyCodeDTO codeDTO, HttpServletRequest request) {
		RequestHeader header = WebUtil.getHeader(request);
		codeDTO.setAppId(header.getAppId());
		return commonSafeApi.checkVerifyCode(new VerifyCodeDTO(NumberUtils.toInt(codeDTO.getCode()),
				CommonServiceType.PHONE_VERIFYCODE_SEND.getName(), codeDTO.getPhone(), header.getAppId(),
				codeDTO.getVeriCode(), false));
	}
}
