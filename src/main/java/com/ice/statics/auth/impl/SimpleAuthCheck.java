package com.ice.statics.auth.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ice.statics.auth.AuthCheck;

import ice.tool.PropertiesProvider;

@Service("simpleAuthCheck")
public class SimpleAuthCheck implements AuthCheck {

	private PropertiesProvider pp = PropertiesProvider.getInstance("/sys_config.properties");
	
	@Override
	public boolean check(Map<String, String[]> pm) {
		String app_code =  pm.get("app_code") == null ? "" : pm.get("app_code")[0];
		String app_pwd = pm.get("app_pwd") == null ? "" : pm.get("app_pwd")[0];

		String validPwd = pp.getValue("app." + app_code);
		if(validPwd == null || !app_pwd.equals(validPwd)) {
			return false;
		}
		return true;
	}
	
}
