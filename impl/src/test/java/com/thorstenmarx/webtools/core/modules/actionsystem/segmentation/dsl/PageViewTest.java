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
import com.thorstenmarx.webtools.core.modules.actionsystem.CacheKey;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentStore;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import com.thorstenmarx.webtools.test.MockCacheLayer;
import com.thorstenmarx.webtools.test.MockDataLayer;
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
public class PageViewTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	UserSegmentStore userSegmenteStore;
	
	private String testSeg_id;
	private String testSeg2_id;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();

		MBassador mbassador = new MBassador();
		executor = new MockedExecutor();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		AdvancedSegment tester = new AdvancedSegment();
		tester.setName("Tester");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		String sb = "segment().site('testSite').and(rule(PAGEVIEW).page('testPage').count(1))";
		tester.setDsl(sb);
		service.add(tester);
		
		testSeg_id = tester.getId();

		tester = new AdvancedSegment();
		tester.setName("Tester2");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		sb = "segment().site('testSite2').and(rule(PAGEVIEW).page('testPage2').count(2))";
		tester.setDsl(sb);
		service.add(tester);
		
		testSeg2_id = tester.getId();

		System.out.println("service: " + service.all());

		cachelayer = new MockCacheLayer();
		userSegmenteStore = new UserSegmentStore(cachelayer);

		actionSystem = new ActionSystemImpl(analytics, service, null, mbassador, userSegmenteStore, executor);
		actionSystem.start();
	}

	@AfterClass
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
	@Test
	public void test_pageview_rule() throws Exception {

		System.out.println("testing pageview rule");

		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "klaus");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "fp_klaus");
		event.put("page", "testPage");
		event.put("site", "testSite");
		event.put("event", "pageview");

		analytics.track(TestHelper.event(event, new JSONObject()));

		await(userSegmenteStore, "klaus", 1);

		List<SegmentData> data = userSegmenteStore.get("klaus");
		assertThat(data).isNotEmpty();

		
		Set<String> segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(testSeg_id);
		assertThat(segments.contains(testSeg2_id)).isFalse();

		data = userSegmenteStore.get("klaus");
		segments = getRawSegments(data);
		assertThat(segments).containsExactly(testSeg_id);

	}
}
