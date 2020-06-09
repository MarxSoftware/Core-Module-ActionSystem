package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.newdsl;

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
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.JsonDsl;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
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
public class CampaignRuleTest extends AbstractTest {

	AnalyticsDB analytics;
	SegmentService service;
	UserSegmentGenerator userSegmenteGenerator;
	
	private String twitter_id;
	private String facebook_id;

	@BeforeClass
	public void setUpClass() throws IOException, InvalidSegmentException {
		long timestamp = System.currentTimeMillis();


		MBassador mbassador = new MBassador();
		
		analytics = new MockAnalyticsDB();
		

		service = new EntitiesSegmentService(entities());

//		twitter_id = createSegment(service, "Twitter Test", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().and(rule(CAMPAIGN).source('twitter').medium('tweet').campaign('test'))");
		twitter_id = createSegment(service, "Twitter Test", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/campaign_1.json"), "testSite");
		
//		facebook_id = createSegment(service, "Facebook demo", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().and(rule(CAMPAIGN).source('facebook').medium('post').campaign('demo'))");
		facebook_id = createSegment(service, "Facebook demo", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/campaign_2.json"), "testSite");

		System.out.println("service: " + service.all());
		
		userSegmenteGenerator = new UserSegmentGenerator(analytics, new JsonDsl(new DefaultServiceRegistry()), service);
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
	@Test(invocationCount = 1, enabled = true)
	public void test_campaign_rule() throws Exception {

		System.out.println("test_campaign_rule");
		
		final String USER_ID = "user " + UUID.randomUUID().toString();

		
		analytics.track(TestHelper.event(TestHelper.event_data(USER_ID), new JSONObject()));
		
		
		
		List<SegmentData> data = userSegmenteGenerator.generate(USER_ID);
		assertThat(data).isNotEmpty();

		SegmentData.Segment segment = data.get(0).getSegment();

		assertThat(segment).isNotNull();
		assertThat(segment.id).isEqualTo(twitter_id);
		
		JSONObject event = TestHelper.event_data(USER_ID);
		event.put(Fields.VisitId.value(), UUID.randomUUID().toString());
		event.put(Fields.Referrer.combine("header"), "https://heise.de/?utm_source=facebook&utm_medium=post&utm_campaign=demo");
		
		analytics.track(TestHelper.event(event, new JSONObject()));
					
				
		data = userSegmenteGenerator.generate(USER_ID);
		assertThat(data).isNotEmpty();
		
		
		Set<String> segments = getRawSegments(data);
		assertThat(segments).isNotNull();
		assertThat(segments).containsExactlyInAnyOrder(twitter_id, facebook_id);
	}
}
