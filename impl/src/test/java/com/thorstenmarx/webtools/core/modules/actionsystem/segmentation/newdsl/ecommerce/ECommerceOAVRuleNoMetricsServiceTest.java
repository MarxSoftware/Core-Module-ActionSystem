package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.newdsl.ecommerce;

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
import com.thorstenmarx.modules.api.DefaultServiceRegistry;
import com.thorstenmarx.webtools.api.TimeWindow;
import com.thorstenmarx.webtools.api.actions.InvalidSegmentException;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.JsonDsl;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Set;

/**
 *
 * @author thmarx
 */
public class ECommerceOAVRuleNoMetricsServiceTest extends AbstractTest {

	AnalyticsDB analytics;
	SegmentService service;
	private UserSegmentGenerator userSegmentGenerator;

	String big_spender;
	String not_big_spender_id;

	@BeforeClass
	public void setUpClass() throws IOException, InvalidSegmentException {
		long timestamp = System.currentTimeMillis();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		big_spender = createSegment(service, "Big Spender", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/ecom/big_spender.json"), "testSite");
		not_big_spender_id = createSegment(service, "No Big Spender", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/ecom/not_big_spender.json"), "testSite");

		System.out.println("service: " + service.all());

		DefaultServiceRegistry registry = new DefaultServiceRegistry();
		userSegmentGenerator = new UserSegmentGenerator(analytics, new JsonDsl(registry), service);

		System.out.println("generate base data");
		for (int i = 0; i < 4; i++) {
			JSONObject event = getEvent("klaus" + i, "ecommerce_order");
			event.put("c_order_total", "50.0");
			event.put("c_order_id", "order-" + i);
			analytics.track(TestHelper.event(event, new JSONObject()));
		}
	}

	@AfterClass
	public void tearDownClass() throws InterruptedException, Exception {
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
	public void test_ecommerce_order_rule() throws Exception {

		System.out.println("testing event rule");

		JSONObject event = getEvent("peter2", "visit");
		analytics.track(TestHelper.event(event, new JSONObject()));

		List<SegmentData> getList = userSegmentGenerator.generate("peter2");
		assertThat(getList).isEmpty();

		event = getEvent("peter2", "ecommerce_order");
		event.put("c_order_total", "100.0");
		event.put("c_order_id", "p1");
		analytics.track(TestHelper.event(event, new JSONObject()));

		getList = userSegmentGenerator.generate("peter2");
		assertThat(getList).isEmpty();
		Set<String> segments = getRawSegments(getList);
		assertThat(segments).isEmpty();
	}

	@Test
	public void test_two_orders() throws Exception {

		System.out.println("testing event rule");

		JSONObject event = getEvent("peter3", "visit");
		analytics.track(TestHelper.event(event, new JSONObject()));

		List<SegmentData> getList = userSegmentGenerator.generate("peter3");
		assertThat(getList).isEmpty();

		event = getEvent("peter3", "ecommerce_order");
		event.put("c_order_total", "50.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		event = getEvent("peter3", "ecommerce_order");
		event.put("c_order_total", "50.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		getList = userSegmentGenerator.generate("peter3");
		assertThat(getList).isEmpty();
	}

	@Test
	public void test_two_orders_2() throws Exception {

		System.out.println("testing event rule");

		final String USER_ID = "user" + System.nanoTime();

		JSONObject event = getEvent(USER_ID, "visit");
		analytics.track(TestHelper.event(event, new JSONObject()));

		List<SegmentData> getList = userSegmentGenerator.generate(USER_ID);
		assertThat(getList).isEmpty();

		event = getEvent(USER_ID, "ecommerce_order");
		event.put("c_order_total", "50.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		event = getEvent(USER_ID, "ecommerce_order");
		event.put("c_order_total", "100.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		getList = userSegmentGenerator.generate(USER_ID);
		assertThat(getList).isEmpty();
	}

	@Test
	public void test_two_orders_3() throws Exception {

		System.out.println("testing event rule");

		final String USER_ID = "user" + System.nanoTime();

		JSONObject event = getEvent(USER_ID, "visit");
		analytics.track(TestHelper.event(event, new JSONObject()));

		List<SegmentData> getList = userSegmentGenerator.generate(USER_ID);
		assertThat(getList).isEmpty();

		event = getEvent(USER_ID, "ecommerce_order");
		event.put("c_order_total", "50.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		event = getEvent(USER_ID, "ecommerce_order");
		event.put("c_order_total", "200.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		getList = userSegmentGenerator.generate(USER_ID);
		assertThat(getList).isEmpty();
	}
	
		@Test
	public void test_no_big_spender() throws Exception {

		System.out.println("testing event rule");

		final String USER_ID = "user" + System.nanoTime();

		JSONObject event = getEvent(USER_ID, "visit");
		analytics.track(TestHelper.event(event, new JSONObject()));

		List<SegmentData> getList = userSegmentGenerator.generate(USER_ID);
		assertThat(getList).isEmpty();

		event = getEvent(USER_ID, "ecommerce_order");
		event.put("c_order_total", "10.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		getList = userSegmentGenerator.generate(USER_ID);
		if (true) {
			return;
		}
		assertThat(getList).isNotEmpty();
		Set<String> segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(not_big_spender_id);
	

		event = getEvent(USER_ID, "ecommerce_order");
		event.put("c_order_total", "200.0");
		event.put("c_order_id", "p" + System.nanoTime());
		analytics.track(TestHelper.event(event, new JSONObject()));

		getList = userSegmentGenerator.generate(USER_ID);
		assertThat(getList).isNotEmpty();
		segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(big_spender);
	}

	private JSONObject getEvent(final String userid, final String eventName) {
		// test event
		JSONObject event = new JSONObject();
		//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("userid", userid);
		event.put("site", "testSite");
		event.put("event", eventName);
		return event;
	}
}
