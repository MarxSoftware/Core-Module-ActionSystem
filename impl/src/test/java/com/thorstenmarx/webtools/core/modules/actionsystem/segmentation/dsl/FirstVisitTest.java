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
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import com.thorstenmarx.webtools.core.modules.actionsystem.CacheKey;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentStore;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.DSLSegment;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.FirstVisitRule;
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
public class FirstVisitTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	UserSegmentStore userSegmenteStore;
	
	private String firstVisit_id;
	private String notfirstvisit_id;

	@BeforeClass
	public void setUpClass() {
		long timestamp = System.currentTimeMillis();

		MBassador mbassador = new MBassador();
		executor = new MockedExecutor();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		AdvancedSegment tester = new AdvancedSegment();
		tester.setName("FirstVisit");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		String sb = "segment().site('testSite').and(rule(FIRSTVISIT))";
		tester.setDsl(sb);
		service.add(tester);

		firstVisit_id = tester.getId();

		tester = new AdvancedSegment();
		tester.setName("Not FirstVisit");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		sb = "segment().site('testSite').not(rule(FIRSTVISIT))";
		tester.setDsl(sb);
		service.add(tester);

		notfirstvisit_id = tester.getId();

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
	@Test(invocationCount = 1)
	public void test_firstvisit_rule() throws Exception {

		System.out.println("testing firstvisit rule");

		final String USER_ID = "user" + UUID.randomUUID().toString();

		JSONObject event = new JSONObject();
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());

		analytics.track(TestHelper.event(event, new JSONObject()));

		await(userSegmenteStore, USER_ID, 1);

		List<SegmentData> data = userSegmenteStore.get(USER_ID);
		assertThat(data).isNotEmpty();

		Set<String> segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(firstVisit_id);

		event = new JSONObject();
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");

		analytics.track(TestHelper.event(event, new JSONObject()));

		await(userSegmenteStore, USER_ID, 1);

		data = userSegmenteStore.get(USER_ID);
		assertThat(data).isNotEmpty();
		segments = getRawSegments(data);

		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(notfirstvisit_id);
	}

	@Test(invocationCount = 2)
	public void simpleTest() {
		DSLSegment not_firstvisit = new DSLSegment();
		not_firstvisit.site("testSite");
		not_firstvisit.not(new FirstVisitRule());
		DSLSegment firstvisit = new DSLSegment();
		firstvisit.site("testSite");
		firstvisit.and(new FirstVisitRule());

		final String USER_ID = "user " + UUID.randomUUID().toString();

		JSONObject event = new JSONObject();
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");

		ShardDocument doc = new ShardDocument("s1", event);
		not_firstvisit.handle(doc);
		not_firstvisit.match();
		firstvisit.handle(doc);
		firstvisit.match();

		assertThat(not_firstvisit.matchs(USER_ID)).isFalse();
		assertThat(firstvisit.matchs(USER_ID)).isTrue();

		not_firstvisit = new DSLSegment();
		not_firstvisit.not(new FirstVisitRule());
		not_firstvisit.site("testSite");
		firstvisit = new DSLSegment();
		firstvisit.and(new FirstVisitRule());
		firstvisit.site("testSite");

		event = new JSONObject();
		event.put(Fields.UserId.value(), USER_ID);
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Site.value(), "testSite");

		ShardDocument doc2 = new ShardDocument("s1", event);
		not_firstvisit.handle(doc);
		not_firstvisit.handle(doc2);
		not_firstvisit.match();
		firstvisit.handle(doc);
		firstvisit.handle(doc2);
		firstvisit.match();

		assertThat(not_firstvisit.matchs(USER_ID)).isTrue();
		assertThat(firstvisit.matchs(USER_ID)).isFalse();
	}
}
