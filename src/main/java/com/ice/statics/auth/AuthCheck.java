package com.ice.statics.auth;

import java.util.Map;

public interface AuthCheck {
	
	public boolean check(Map<String, String[]> pm);
}
