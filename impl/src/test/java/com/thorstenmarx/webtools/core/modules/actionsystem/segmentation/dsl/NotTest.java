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
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
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
import java.util.concurrent.TimeUnit;
import net.engio.mbassy.bus.MBassador;
import org.awaitility.Awaitility;

/**
 *
 * @author thmarx
 */
public class NotTest extends AbstractTest{

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	LocalUserSegmentStore userSegmenteStore;
	
	private String notvisited_id;

	@BeforeClass
	public void setUpClass() {
		
	}

	@AfterClass
	public void tearDownClass() throws InterruptedException, Exception {
		
	}

	@BeforeMethod
	public void setUp() {
		long timestamp = System.currentTimeMillis();

		
		MBassador mbassador = new MBassador();
		executor = new MockedExecutor();

		analytics = new MockAnalyticsDB();
		

		service = new EntitiesSegmentService(entities());

		
		
		AdvancedSegment tester = new AdvancedSegment();
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		tester.setActive(true);
		tester.setName("Not Visited");
		String sb = "segment().site('asite_not').and(not(rule(PAGEVIEW).page('apage_not')))";
		tester.setDsl(sb);
		service.add(tester);

		notvisited_id = tester.getId();
		
		System.out.println("service: " + service.all());
		
		cachelayer = new MockCacheLayer();
		userSegmenteStore = new LocalUserSegmentStore(cachelayer);
		
		actionSystem = new ActionSystemImpl(analytics, service, null, mbassador, userSegmenteStore, executor);
		actionSystem.start();
	}

	@AfterMethod
	public void tearDown() {
		actionSystem.close();
	}



	
	@Test
	public void test_not_pageview() throws Exception {

		System.out.println("testing not pageview");

		
		JSONObject event = new JSONObject();
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "notKlaus");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "not_klaus");
		event.put("page", "apage1_not");
		event.put("site", "asite_not");
		event.put("event", "pageview");
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		await(userSegmenteStore, "notKlaus", 1);

		List<SegmentData> data = userSegmenteStore.get("notKlaus");
		assertThat(data).isNotEmpty();

		Set<String> segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).contains(notvisited_id);
		
		event.put("page", "apage_not");
		event.put("site", "asite_not");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		Awaitility.await().atMost(1000, TimeUnit.SECONDS).until(() ->
				userSegmenteStore.get("notKlaus").isEmpty()
		);
	}
	
	@Test
	public void test_not_site_view() throws Exception {

		System.out.println("testing not site view");
		

		
		JSONObject event = new JSONObject();
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "notKlaus");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "not_klaus");
		event.put("page", "apage1_not");
		event.put("site", "asite1_not");
		event.put("event", "pageview");
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		Thread.sleep(2000);
		
		assertThat(userSegmenteStore.get("notKlaus")).isEmpty();
	}
}
