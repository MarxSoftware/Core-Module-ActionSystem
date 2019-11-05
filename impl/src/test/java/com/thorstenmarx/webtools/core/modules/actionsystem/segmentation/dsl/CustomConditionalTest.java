package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.dsl;

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
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentStore.LocalUserSegmentStore;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import com.thorstenmarx.webtools.test.MockCacheLayer;
import com.thorstenmarx.webtools.test.MockedExecutor;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Set;
import java.util.UUID;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author thmarx
 */
public class CustomConditionalTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	LocalUserSegmentStore userSegmenteStore;
	
	private String conditional_ID;
	private String facebook_id;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();

		MBassador mbassador = new MBassador();
		executor = new MockedExecutor();
		
		analytics = new MockAnalyticsDB();
		

		service = new EntitiesSegmentService(entities());

		final StringBuilder conditional = new StringBuilder();
		conditional.append("var impl = {};\r\n");
		conditional.append("impl.users = [];\r\n");
		conditional.append("impl.match = function () {};\r\n");
		conditional.append("impl.handle = function (doc) { this.users.push(doc.document.getString('userid'));};\r\n");
		conditional.append("impl.matchs = function (userid) { return this.users.indexOf(userid) !== -1;};\r\n");
		
		conditional.append("var conditions = require('conditions.js');\r\n");
		conditional.append("var conditional = conditions.conditional(impl);\r\n");
		conditional.append("segment().site('testSite').and(conditional);\r\n");
		
		conditional_ID = createSegment(service, "Twitter Test", new TimeWindow(TimeWindow.UNIT.YEAR, 1), conditional.toString());

		System.out.println("service: " + service.all());
		
		cachelayer = new MockCacheLayer();
		userSegmenteStore = new LocalUserSegmentStore(cachelayer);
		
		actionSystem = new ActionSystemImpl(analytics, service, null, mbassador, userSegmenteStore, executor);
		actionSystem.start();
	}

	@AfterClass()
	public void tearDownClass() throws InterruptedException, Exception {
		actionSystem.close();
	}

	@BeforeMethod
	public void setUp() {
	}

	@AfterMethod
	public void tearDown() {
	}

	/**
	 * Test of open method, of class AnalyticsDb.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(invocationCount = 1)
	public void test_conditional_rule() throws Exception {

		System.out.println("test_conditional_rule");
		
		final String USER_ID = "user " + UUID.randomUUID().toString();

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
		
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		await(userSegmenteStore, USER_ID, 1);

		List<SegmentData> list = userSegmenteStore.get(USER_ID);
		assertThat(list).isNotEmpty();

		Set<String> segments = getRawSegments(list);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(conditional_ID);
	}
}
