package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.newdsl;

/*-
 * #%L
 * webtools-actions
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
import com.thorstenmarx.webtools.api.TimeWindow;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.core.modules.actionsystem.NEWDSLUserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.GraalDSL;
import com.thorstenmarx.webtools.core.modules.actionsystem.newdsl.JsonDsl;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author thmarx
 */
public class ScoreTest extends AbstractTest {

	AnalyticsDB analytics;
	SegmentService service;
	NEWDSLUserSegmentGenerator userSegmentGenerator;
	
	private String demoSeg_id;

	@BeforeClass
	public void setUpClass() throws IOException {
		long timestamp = System.currentTimeMillis();

		MBassador mbassador = new MBassador();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		AdvancedSegment tester = new AdvancedSegment();
		tester.setName("DEMO");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
//		String sb = "segment().and(rule(SCORE).name('demo').score(100))";
		String sb = loadContent("src/test/resources/segments/newdsl/score.json");
		tester.setDsl(sb);
		service.add(tester);
		demoSeg_id = tester.getId();

		System.out.println("service: " + service.all());

		userSegmentGenerator = new NEWDSLUserSegmentGenerator(analytics, new JsonDsl(), service);
	}


	@Test
	public void test_score_rule() throws Exception {

		System.out.println("testing score rules");

		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put(Fields.Site.value(), "testSite");
		event.put("userid", "peter");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "fp_peter");
		event.put("score", Arrays.asList(new String[]{"demo:50"}));

		analytics.track(TestHelper.event(event, new JSONObject()));

//		Thread.sleep(5000);
		event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put(Fields.Site.value(), "testSite");
		event.put("userid", "peter");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "fp_peer");
		event.put("score", Arrays.asList(new String[]{"demo:100"}));

		analytics.track(TestHelper.event(event, new JSONObject()));


		List<SegmentData> data = userSegmentGenerator.generate("peter");
		Set<String> segments = getRawSegments(data);
		assertThat(segments).contains(demoSeg_id);

	}

}
