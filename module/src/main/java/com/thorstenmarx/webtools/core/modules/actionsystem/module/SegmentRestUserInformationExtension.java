package com.thorstenmarx.webtools.core.modules.actionsystem.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thorstenmarx.modules.api.annotation.Extension;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.extensions.RestUserInformationExtension;
import com.thorstenmarx.webtools.core.modules.actionsystem.CacheKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/**
 *
 * @author marx
 */
@Extension(RestUserInformationExtension.class)
public class SegmentRestUserInformationExtension extends RestUserInformationExtension {

	@Inject
	private CacheLayer cachelayer;

	private final String SEGMENT_DATA_KEY = "segmentdata_%s";

	@Override
	public JSONObject getUserInformation(String userid) {
		JSONObject segments = new JSONObject();

//		List<SegmentData> segmentList = CoreModuleActionSystemExtensionImpl.userSegmentStore.get(userid);
		final String cacheKey = String.format(SEGMENT_DATA_KEY, userid);

		ArrayList<SegmentData> segmentList = null;
		if (cachelayer.exists(cacheKey)) {
			segmentList = cachelayer.get(cacheKey, ArrayList.class).get();
		} else {
			segmentList = (ArrayList<SegmentData>) CoreModuleActionSystemExtensionImpl.userSegmentGenerator.generate(userid);
			cachelayer.add(cacheKey, segmentList, 30, TimeUnit.SECONDS);
		}
		
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
		return true;
	}

	@Override
	public void init() {
	}

}
