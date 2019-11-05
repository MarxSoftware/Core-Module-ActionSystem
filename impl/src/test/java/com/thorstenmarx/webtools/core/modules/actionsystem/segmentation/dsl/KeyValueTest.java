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
import com.alibaba.fastjson.JSONArray;
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
import java.util.Arrays;
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
public class KeyValueTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	LocalUserSegmentStore userSegmenteStore;

	String segment_device;
	String segment_product;
	private String segment_product_multi;
	private String segment_product_multi_AND;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();

	
		MBassador mbassador = new MBassador();
		executor = new MockedExecutor();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		segment_product = createSegment(service, "Buyer", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().and(rule(KEYVALUE).key('c_products').values(['prod1']))");
		segment_product_multi = createSegment(service, "MultiBuyer", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().and(rule(KEYVALUE).key('c_products2').values(['prod1']))");
		segment_product_multi_AND = createSegment(service, "MultiBuyerAND", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().and(rule(KEYVALUE).key('c_products2').and().values(['prod1', 'prod2']))");
		segment_device = createSegment(service, "Linux/Windows", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().and(rule(KEYVALUE).key('os.device').values(['linux', 'windows']))");

		System.out.println("service: " + service.all());
		System.out.println("segment_product: " + segment_product);
		System.out.println("segment_product_multi: " + segment_product_multi);
		System.out.println("segment_product_multi_AND: " + segment_product_multi_AND);
		System.out.println("segment_device: " + segment_device);

		cachelayer = new MockCacheLayer();
		userSegmenteStore = new LocalUserSegmentStore(cachelayer);

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
	@Test()
	public void test_single_keyvalue_rule() throws Exception {

		System.out.println("testing event rule");

		// test event
		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", "peter2");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("site", "testSite");
		event.put("event", "order");
		event.put("c_products", "prod1");
		analytics.track(TestHelper.event(event, new JSONObject()));

		assertThat(userSegmenteStore.get("peter2")).isEmpty();

		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event, new JSONObject()));

		await(userSegmenteStore, "peter2", 1);

		List<SegmentData> metaData = userSegmenteStore.get("peter2");
		Set<String> segments = getRawSegments(metaData);
		assertThat(segments).isNotEmpty();
		assertThat(segments).contains(segment_product);
	}
	@Test
	public void test_multiple_keyvalue_rule() throws Exception {

		System.out.println("testing event rule");

		// test event
		JSONObject event = new JSONObject();
		event.put("_timestamp", System.currentTimeMillis());
		event.put("userid", "linuxuser");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("os.device", "linux");
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		event = new JSONObject();
		event.put("_timestamp", System.currentTimeMillis());
		event.put("userid", "windowsuser");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("os.device", "windows");
		analytics.track(TestHelper.event(event, new JSONObject()));


		await(userSegmenteStore, "linuxuser", 1);
		await(userSegmenteStore, "windowsuser", 1);

		List<SegmentData> metaData = userSegmenteStore.get("linuxuser");
		assertThat(metaData).isNotEmpty();
		Set<String> segments = getRawSegments(metaData);
		assertThat(segments).containsExactly(segment_device);
		
		metaData = userSegmenteStore.get("windowsuser");
		segments = getRawSegments(metaData);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(segment_device);
	}
	
	@Test()
	public void test_multi_multi_keyvalue_rule() throws Exception {

		System.out.println("testing event rule");

		final String USER_ID = "peter_multi";
		// test event
		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", USER_ID);
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("site", "testSite");
		event.put("event", "order");
		event.put("c_products2", new JSONArray(Arrays.asList("prod1", "prod2")));
		analytics.track(TestHelper.event(event, new JSONObject()));


		assertThat(userSegmenteStore.get(USER_ID)).isEmpty();

		analytics.track(TestHelper.event(event, new JSONObject()));
		

		await(userSegmenteStore, USER_ID, 2);

		List<SegmentData> metaData = userSegmenteStore.get(USER_ID);
		Set<String> segments = getRawSegments(metaData);
		assertThat(segments).isNotEmpty();
		assertThat(segments).contains(segment_product_multi, segment_product_multi_AND);
	}
	@Test(description = "Testet einen KEyValueRule mit AND verknüpften Values")
	public void test_multi_multi_keyvalue_rule_and() throws Exception {

		System.out.println("testing event rule");
		final String USER_ID = "peter_multi_and";
		// test event
		JSONObject event = new JSONObject();
//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put("userid", USER_ID);
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put("site", "testSite");
		event.put("event", "order");
		event.put("c_products2", new JSONArray(Arrays.asList("prod1", "prod2")));
		analytics.track(TestHelper.event(event, new JSONObject()));


		assertThat(userSegmenteStore.get(USER_ID)).isEmpty();

		JSONObject event2 = (JSONObject) event.clone();
		event2.put(Fields._UUID.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event2, new JSONObject()));

		await(userSegmenteStore, USER_ID, 2);

		List<SegmentData> metaData = userSegmenteStore.get(USER_ID);
		Set<String> segments = getRawSegments(metaData);
		assertThat(segments).isNotEmpty();
		assertThat(segments).contains(segment_product_multi, segment_product_multi_AND);
	}
}
