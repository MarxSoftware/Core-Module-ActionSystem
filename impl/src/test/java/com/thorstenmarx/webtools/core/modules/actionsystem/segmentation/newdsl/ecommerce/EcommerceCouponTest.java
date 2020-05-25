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
import com.thorstenmarx.modules.api.ServiceRegistry;
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
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author thmarx
 */
public class EcommerceCouponTest extends AbstractTest {

	AnalyticsDB analytics;
	SegmentService service;
	private UserSegmentGenerator userSegmentGenerator;

	String coupon_lover_id;
	String no_coupon_id;
	
	ServiceRegistry registry;

	@BeforeClass
	public void setUpClass() throws IOException, InvalidSegmentException {
		long timestamp = System.currentTimeMillis();


		registry = new ServiceRegistry();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

//		coupon_lover_id = createSegment(service, "Coupon lover", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().site('testSite').and(rule(ECOMMERCE_COUPON).count(3))");
//		no_coupon_id = createSegment(service, "Non Coupon", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().site('testSite').and(rule(ECOMMERCE_COUPON).count(0).exact())");
		coupon_lover_id = createSegment(service, "Coupon lover", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/ecom/coupon_1.json"), "testSite");
		no_coupon_id = createSegment(service, "Non Coupon", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/ecom/coupon_2.json"), "testSite");

		System.out.println("service: " + service.all());

		userSegmentGenerator = new UserSegmentGenerator(analytics, new JsonDsl(registry), service);
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
	public void test_ecommerce_discount_rule() throws Exception {

		System.out.println("testing ecommerce discount rule");

		JSONObject event = getEvent("peter2", "visit", 0);
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		List<SegmentData> getList = userSegmentGenerator.generate("peter2");
		assertThat(getList).isNotEmpty();
		Set<String> segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(no_coupon_id);
		
		
		event = getEvent("peter2", "ecommerce_order", 2);
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		getList = userSegmentGenerator.generate("peter2");
		assertThat(getList).isEmpty();
		
		event = getEvent("peter2", "ecommerce_order", "1");
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		getList = userSegmentGenerator.generate("peter2");
		assertThat(getList).isNotEmpty();
		segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(coupon_lover_id);
		
	}

	private JSONObject getEvent(final String userid, final String eventName, final int couponCount) {
		// test event
		JSONObject event = new JSONObject();
		//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("userid", userid);
		event.put("site", "testSite");
		event.put("event", eventName);
		event.put("order_coupons_count", couponCount);
		return event;
	}
	
	private JSONObject getEvent(final String userid, final String eventName, final String couponCount) {
		// test event
		JSONObject event = new JSONObject();
		//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("userid", userid);
		event.put("site", "testSite");
		event.put("event", eventName);
		event.put("order_coupons_count", couponCount);
		return event;
	}
}
