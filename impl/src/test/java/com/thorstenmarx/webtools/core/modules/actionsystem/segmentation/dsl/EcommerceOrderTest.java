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
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.GraalDSL;
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
public class EcommerceOrderTest extends AbstractTest {

	AnalyticsDB analytics;
	SegmentService service;
	private UserSegmentGenerator userSegmentGenerator;

	String buyer_id;
	String not_buyer_id;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();


		MBassador mbassador = new MBassador();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		buyer_id = createSegment(service, "Buyer", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().site('testSite').and(rule(ECOMMERCE_ORDER).count(1))");
		not_buyer_id = createSegment(service, "No Buyer", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().site('testSite').and(rule(ECOMMERCE_ORDER).count(0).exact())");

		System.out.println("service: " + service.all());

		userSegmentGenerator = new UserSegmentGenerator(analytics, new GraalDSL(null, mbassador), service);
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
		assertThat(getList).isNotEmpty();
		Set<String> segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(not_buyer_id);
		
		
		event = getEvent("peter2", "ecommerce_order");
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		getList = userSegmentGenerator.generate("peter2");
		assertThat(getList).isNotEmpty();
		segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(buyer_id);
		
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
