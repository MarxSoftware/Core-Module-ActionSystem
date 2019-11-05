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
import com.thorstenmarx.webtools.api.actions.ActionEvent;
import com.thorstenmarx.webtools.api.actions.ActionException;
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentStore.LocalUserSegmentStore;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import com.thorstenmarx.webtools.test.MockCacheLayer;
import com.thorstenmarx.webtools.test.MockedExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import net.engio.mbassy.bus.MBassador;
import org.easymock.EasyMock;

/**
 *
 * @author thmarx
 */
public class ActionTest extends AbstractTest {

	AnalyticsDB analytics;
	CacheLayer cachelayer;
	LocalUserSegmentStore userSegmenteStore;
	ActionSystemImpl actionSystem;
	SegmentService service;
	private MBassador mbassador;
	MockedExecutor executor;

	@BeforeClass
	public void setUpClass() throws ActionException {

		mbassador = EasyMock.createNiceMock(MBassador.class);

		long timestamp = System.currentTimeMillis();

		executor = new MockedExecutor();
		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		cachelayer = new MockCacheLayer();
		userSegmenteStore = new LocalUserSegmentStore(cachelayer);

		System.out.println("service: " + service.all());

		actionSystem = new ActionSystemImpl(analytics, service, null, mbassador, userSegmenteStore, executor);
//		actionSystem.start();

		String sb = "eventAction('testEvent').site('testSite').window(years(1)).and(rule(EVENT).event('order').count(2))";
		actionSystem.addAction("a1", "testEvent", sb);
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
	public void test_action() throws Exception {

		System.out.println("testing event rule");

		ActionEvent actionEvent = ActionEvent.builder().setUserid("peter2").setEvent("testEvent").build();
		EasyMock.expect(mbassador.publishAsync(actionEvent)).andAnswer(() -> null).atLeastOnce();
		mbassador.subscribe(actionSystem);
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(mbassador);

		actionSystem.start();

		// test event
		JSONObject event = TestHelper.event_data("peter2");
		event.put("event", "order");
		analytics.track(TestHelper.event(event, new JSONObject()));

		Thread.sleep(2000);

		event = TestHelper.event_data("peter2");
		event.put("event", "order");
		analytics.track(TestHelper.event(event, new JSONObject()));

		Thread.sleep(5000l);

		EasyMock.verify(mbassador);
	}
}
