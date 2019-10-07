package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation;

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
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.actions.model.rules.EventRule;
import com.thorstenmarx.webtools.api.actions.model.rules.PageViewRule;
import com.thorstenmarx.webtools.api.actions.model.rules.ScoreRule;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.datalayer.DataLayer;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import com.thorstenmarx.webtools.test.MockDataLayer;
import com.thorstenmarx.webtools.test.MockedExecutor;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.engio.mbassy.bus.MBassador;
import org.awaitility.Awaitility;

/**
 *
 * @author thmarx
 */
public class SegmentationTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	DataLayer datalayer;
	private MockedExecutor executor;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();

		
		MBassador mbassador = new MBassador();

		executor = new MockedExecutor();
		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		Segment tester = new Segment();
		tester.setId("testSeg");
		tester.setName("Tester");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));

		PageViewRule pvr = new PageViewRule();
		pvr.count(1);
		pvr.site("testSite");
		pvr.page("testPage");

		tester.rules().add(pvr);
		service.add(tester);

		tester = new Segment();
		tester.setId("testSeg2");
		tester.setName("Tester2");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		pvr = new PageViewRule();
		pvr.count(2);
		pvr.site("testSite2");
		pvr.page("testPage2");

		tester.rules().add(pvr);
		service.add(tester);

		tester = new Segment();
		tester.setId("demoSeg");
		tester.setName("DEMO");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		ScoreRule sr = new ScoreRule();
		sr.name("demo");
		sr.score(100);
		tester.rules().add(sr);
		service.add(tester);

		tester = new Segment();
		tester.setId("seqBuy");
		tester.setName("Buyer");
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		EventRule er = new EventRule();
		er.event("order");
		er.site("testSite");
		er.count(2);
		tester.rules().add(er);
		service.add(tester);

		System.out.println("service: " + service.all());

		datalayer = new MockDataLayer();

		actionSystem = new ActionSystemImpl(analytics, service, null, mbassador, datalayer, executor);
		actionSystem.start();
	}

	@AfterClass
	public void tearDownClass() throws InterruptedException, Exception {
		actionSystem.close();
		executor.shutdown();
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
	@Test(enabled = false)
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
		
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		await(datalayer, "klaus", 1);

		List<SegmentData> data = datalayer.list("klaus", SegmentData.KEY, SegmentData.class).get();
		assertThat(data).isNotNull();

		Set<String> segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly("testSeg");
		assertThat(segments.contains("testSeg2")).isFalse();

		data = datalayer.list("klaus", SegmentData.KEY, SegmentData.class).get();
		assertThat(data).isNotNull();
		segments = getRawSegments(data);
		assertThat(segments).containsExactly("testSeg");

	}

	@Test(enabled = false)
	public void test_score_rule() throws Exception {

		System.out.println("testing score rules");

		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "peter");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "fp_peter");
		event.put("score", Arrays.asList(new String[]{"demo:50"}));
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		Thread.sleep(2000);

		
		Optional<List<SegmentData>> list = datalayer.list("peter", SegmentData.KEY, SegmentData.class);
		assertThat(list.isPresent()).isFalse();

		event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "peter");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("fingerprint", "fp_peer");
		event.put("score", Arrays.asList(new String[]{"demo:100"}));
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> datalayer.get("peter", SegmentData.KEY, SegmentData.class).isPresent());
		await(datalayer, "peter", 1);

		List<SegmentData> result = datalayer.list("peter", SegmentData.KEY, SegmentData.class).get();
		Set<String> segments = getRawSegments(result);
		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly("demoSeg");

	}

	@Test(enabled = false)
	public void test_event_rule() throws Exception {

		System.out.println("testing event rule");

		// test event
		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "peter2");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("site", "testSite");
		event.put("fingerprint", "fp_peter2");
		event.put("event", "order");
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		
		assertThat(datalayer.exists("peter2", SegmentData.KEY)).isFalse();

		analytics.track(TestHelper.event(event, new JSONObject()));

		Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> datalayer.get("peter2", SegmentData.KEY, SegmentData.class).isPresent());
		await(datalayer, "peter2", 1);

		List<SegmentData> metaData = datalayer.list("peter2", SegmentData.KEY, SegmentData.class).get();
		Set<String> segments = getRawSegments(metaData);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly("seqBuy");
	}
}
