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
public class CategoryTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	LocalUserSegmentStore userSegmenteStore;
	
	private String cat_1;
	private String notsearch_id;
	private String cat_2;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();


		MBassador mbassador = new MBassador();
		executor = new MockedExecutor();
		
		analytics = new MockAnalyticsDB();
		

		service = new EntitiesSegmentService(entities());

		
		
		AdvancedSegment tester = new AdvancedSegment();
		tester.setName("CAT2");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		String sb = "segment().and(rule(CATEGORY).path('/CAT1/CAT2').field('c_categories').count(2))";
		tester.setDsl(sb);
		service.add(tester);
		cat_1 = tester.getId();
		
		tester = new AdvancedSegment();
		tester.setName("CAT1");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		sb = "segment().and(rule(CATEGORY).path('/CAT1').field('c_categories').count(2))";
		tester.setDsl(sb);
		service.add(tester);
		cat_2 = tester.getId();
		
		
		System.out.println("service: " + service.all());
		
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
	@Test(invocationCount = 1)
	public void test_category_rule() throws Exception {

		System.out.println("testing category rule");
		
		final String USER_ID = "user" + UUID.randomUUID().toString();

		JSONObject event = new JSONObject();
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");
		
		analytics.track(TestHelper.event(event, new JSONObject()));

		Thread.sleep(2000l);
		List<SegmentData> list = userSegmenteStore.get(USER_ID);
		assertThat(list).isEmpty();
		
		event = new JSONObject();
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");
		event.put("c_categories", Arrays.asList(new String[]{"/CAT1", "/CAT1/CAT2"}));
		
		analytics.track(TestHelper.event(event, new JSONObject()));
						
		await(userSegmenteStore, USER_ID, 2);

		
		list = userSegmenteStore.get(USER_ID);
		assertThat(list).isNotEmpty();
		Set<String> segments = getRawSegments(list);
		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(cat_1, cat_2);
	}
}
