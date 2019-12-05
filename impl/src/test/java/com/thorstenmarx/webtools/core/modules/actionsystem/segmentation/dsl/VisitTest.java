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
public class VisitTest extends AbstractTest {

	AnalyticsDB analytics;
	ActionSystemImpl actionSystem;
	SegmentService service;
	MockedExecutor executor;
	CacheLayer cachelayer;
	LocalUserSegmentStore userSegmenteStore;
	
	private String testSeg_id;
	private String testSeg2_id;
	private UserSegmentGenerator userSegmentGenerator;

	@BeforeClass
	public void setUpClass() {
		MBassador mbassador = new MBassador();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

		AdvancedSegment tester = new AdvancedSegment();
		tester.setName("Tester");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		String sb = "segment().site('testSite').and(rule(VISIT).count(1))";
		tester.setDsl(sb);
		service.add(tester);
		
		testSeg_id = tester.getId();

		tester = new AdvancedSegment();
		tester.setName("Tester2");
		tester.setActive(true);
		tester.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		sb = "segment().site('testSite').and(rule(VISIT).count(2))";
		tester.setDsl(sb);
		service.add(tester);
		
		testSeg2_id = tester.getId();

		System.out.println("service: " + service.all());

//		cachelayer = new MockCacheLayer();
//		userSegmenteStore = new LocalUserSegmentStore(cachelayer);

//		actionSystem = new ActionSystemImpl(analytics, service, null, mbassador, userSegmenteStore, executor);
//		actionSystem.start();
		
		userSegmentGenerator = new UserSegmentGenerator(analytics, new GraalDSL(null, mbassador), service);
	}

	@AfterClass
	public void tearDownClass() throws InterruptedException, Exception {
//		actionSystem.close();
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
	public void test_visit_rule() throws Exception {

		System.out.println("testing pageview rule");

		JSONObject event = createEvent();
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event, new JSONObject()));

		
		List<SegmentData> segmentData = userSegmentGenerator.generate("klaus");
		Set<String> segments = getRawSegments(segmentData);
		
		assertThat(segments).isNotNull();
		assertThat(segments).containsExactly(testSeg_id);
		assertThat(segments.contains(testSeg2_id)).isFalse();

		event = createEvent();
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event, new JSONObject()));
		
		segmentData = userSegmentGenerator.generate("klaus");
		segments = getRawSegments(segmentData);
		
		assertThat(segments).isNotNull();
		assertThat(segments).containsExactlyInAnyOrder(testSeg_id, testSeg2_id);
		
	}

	private JSONObject createEvent() {
		JSONObject event = new JSONObject();
		//		event.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		event.put("_timestamp", System.currentTimeMillis());
		event.put("ua", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:38.0) Gecko/20100101 Firefox/38.0");
		event.put(Fields.UserId.value(), "klaus");
		event.put("fingerprint", "fp_klaus");
		event.put("page", "testPage");
		event.put("site", "testSite");
		event.put("event", "pageview");
		return event;
	}
}
