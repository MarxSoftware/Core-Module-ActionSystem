package de.marx_software.webtools.core.modules.actionsystem;

import de.marx_software.webtools.api.datalayer.SegmentData;

/**
 *
 * @author marx
 */
public class CacheKey {
	
	public static String key (final String user, final String key, final SegmentData.Segment segment) {
		return String.format("%s###%s###%s", user, key, segment.id);
	}
	public static String[] split (final String identifier) {
		return identifier.split("###");
	}
}
