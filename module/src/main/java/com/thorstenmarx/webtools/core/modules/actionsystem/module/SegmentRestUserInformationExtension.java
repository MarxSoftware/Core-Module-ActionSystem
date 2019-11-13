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
		JSONObject segments = new JSONObject();
		
		List<SegmentData> segmentList = CoreModuleActionSystemExtensionImpl.userSegmentStore.get(userid);
		JSONArray segmentArray = new JSONArray();
		segmentList.forEach((sd) -> {
			JSONObject segment = new JSONObject();
			segment.put("id", sd.getSegment().id);
			segment.put("name", sd.getSegment().name);
			segment.put("wpid", sd.getSegment().wpid);
			
			segmentArray.add(segment);
		});
		
		segments.put("segments", segmentArray);
		
		return segments;
	}

	@Override
	public String getName() {
		return "actionSystem";
	}

	@Override
	public boolean hasUserInformation(final String userid) {
		return !CoreModuleActionSystemExtensionImpl.userSegmentStore.get(userid).isEmpty();
	}

	@Override
	public void init() {
	}
	
}
