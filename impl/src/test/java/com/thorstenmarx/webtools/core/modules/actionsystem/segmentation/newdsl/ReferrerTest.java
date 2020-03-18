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
import com.thorstenmarx.webtools.api.actions.InvalidSegmentException;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.JsonDsl;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Set;
import java.util.UUID;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author thmarx
 */
public class ReferrerTest extends AbstractTest {

	AnalyticsDB analytics;

	SegmentService service;
	UserSegmentGenerator userSegmentGenerator;

	private String search_id;
	private String notsearch_id;

	@BeforeClass
	public void setUpClass() throws IOException, InvalidSegmentException {
		long timestamp = System.currentTimeMillis();

		MBassador mbassador = new MBassador();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		Segment tester = new Segment();
		tester.setName("Search");
		tester.setActive(true);
		tester.setSite("testSite");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
//		String sb = "segment().and(rule(REFERRER).medium('SEARCH'))";
		String sb = loadContent("src/test/resources/segments/newdsl/referrer_1.json");
		tester.setContent(sb);
		service.add(tester);
		search_id = tester.getId();

		tester = new Segment();
		tester.setName("Not Search");
		tester.setActive(true);
		tester.setSite("testSite");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
//		sb = "segment().not(rule(REFERRER).medium('SEARCH'))";
		sb = loadContent("src/test/resources/segments/newdsl/referrer_2.json");
		tester.setContent(sb);
		service.add(tester);
		notsearch_id = tester.getId();

		System.out.println("service: " + service.all());

		userSegmentGenerator = new UserSegmentGenerator(analytics, new JsonDsl(), service);
	}

	/**
	 * Test of open method, of class AnalyticsDb.
	 *
	 * @throws java.lang.Exception
	 */
	@Test()
	public void test_referrer_rule() throws Exception {

		System.out.println("testing referrer rule");

		final String USER_ID = "user" + UUID.randomUUID().toString();

		JSONObject event = new JSONObject();
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());

		analytics.track(TestHelper.event(event, new JSONObject()));

		List<SegmentData> data = userSegmentGenerator.generate(USER_ID);

		Set<String> segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(notsearch_id);

		event = new JSONObject();
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");
		event.put(Fields.Referrer.combine("medium"), "SEARCH");
		event.put(Fields.Referrer.combine("source"), "Google");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event, new JSONObject()));

		data = userSegmentGenerator.generate(USER_ID);
		segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(search_id);
	}
}
