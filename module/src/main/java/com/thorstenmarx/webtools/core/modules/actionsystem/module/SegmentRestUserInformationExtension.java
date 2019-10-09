package com.thorstenmarx.webtools.core.modules.actionsystem.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thorstenmarx.modules.api.annotation.Extension;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.extensions.RestUserInformationExtension;
import com.thorstenmarx.webtools.core.modules.actionsystem.CacheKey;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author marx
 */
@Extension(RestUserInformationExtension.class)
public class SegmentRestUserInformationExtension extends RestUserInformationExtension {

	@Inject
	private CacheLayer cachelayer;
	
	@Override
	public JSONObject getUserInformation(String userid) {
		final String cache_key = CacheKey.key(userid, SegmentData.KEY);
		
		JSONObject segments = new JSONObject();
		
		List<SegmentData> segmentList = cachelayer.list(cache_key, SegmentData.class);
		JSONArray segmentArray = new JSONArray();
		segmentList.forEach(segmentArray::add);
		
		segments.put("segments", segmentArray);
		
		return segments;
	}

	@Override
	public String getName() {
		return "actionsSystem";
	}

	@Override
	public boolean hasUserInformation(final String userid) {
		final String cache_key = CacheKey.key(userid, SegmentData.KEY);
		return cachelayer.exists(cache_key);
	}

	@Override
	public void init() {
	}
	
}
