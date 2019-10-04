package com.thorstenmarx.webtools.core.modules.actionsystem;

/*-
 * #%L
 * webtools-analytics
 * %%
 * Copyright (C) 2016 - 2018 Thorsten Marx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.alibaba.fastjson.JSONObject;
import com.thorstenmarx.webtools.api.analytics.Fields;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author marx
 */
public class TestHelper {
	
	public static  JSONObject event_data (final String USER_ID) {
		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", USER_ID);
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "fp_klaus");
		event.put("page", "testPage");
		event.put("site", "testSite");
		event.put("event", "pageview");
		event.put(Fields.Referrer.combine("header"), "https://heise.de/?utm_source=twitter&utm_medium=tweet&utm_campaign=test");
		
		return event;
	}
	
	public static Map<String, Map<String, Object>> event (final JSONObject data, final JSONObject meta) {
		Map<String, Map<String, Object>> event = new HashMap<>();
		
		event.put("data", data);
		event.put("meta", meta);
		
		return event;
	}
	public static Map<String, Map<String, Object>> event (final Map<String, Object> data, final Map<String, Object> meta) {
		Map<String, Map<String, Object>> event = new HashMap<>();
		
		event.put("data", data);
		event.put("meta", meta);
		
		return event;
	}
}
