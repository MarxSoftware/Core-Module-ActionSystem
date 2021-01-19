package de.marx_software.webtools.core.modules.actionsystem.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thorstenmarx.modules.api.annotation.Extension;
import de.marx_software.webtools.api.CoreModuleContext;
import de.marx_software.webtools.api.cache.CacheLayer;
import de.marx_software.webtools.api.datalayer.SegmentData;
import de.marx_software.webtools.api.extensions.RestUserInformationExtension;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author marx
 */
@Extension(RestUserInformationExtension.class)
public class SegmentRestUserInformationExtension extends RestUserInformationExtension {

	private CacheLayer cachelayer;

	private final String SEGMENT_DATA_KEY = "segmentdata_%s";

	@Override
	public JSONObject getUserInformation(final String userid, final String site) {
		JSONObject segments = new JSONObject();

//		List<SegmentData> segmentList = CoreModuleActionSystemExtensionImpl.userSegmentStore.get(userid);
		final String cacheKey = String.format(SEGMENT_DATA_KEY, userid);

		ArrayList<SegmentData> segmentList = null;
		if (cachelayer.exists(cacheKey)) {
			segmentList = cachelayer.get(cacheKey, ArrayList.class).get();
		} else {
			segmentList = (ArrayList<SegmentData>) CoreModuleActionSystemModuleLifeCycle.userSegmentGenerator.generate(userid, site);
			cachelayer.add(cacheKey, segmentList, 4, TimeUnit.SECONDS);
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
	public boolean hasUserInformation(final String userid, final String site) {
		return true;
	}

	@Override
	public void init() {
		cachelayer = getContext().serviceRegistry().single(CacheLayer.class).get();
	}

}
