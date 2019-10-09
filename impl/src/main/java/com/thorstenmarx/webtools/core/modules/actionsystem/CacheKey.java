package com.thorstenmarx.webtools.core.modules.actionsystem;

/**
 *
 * @author marx
 */
public class CacheKey {
	
	public static String key (final String user, final String key) {
		return String.format("%s|%s", user, key);
	}
}
