package de.marx_software.webtools.core.modules.actionsystem.segmentation.newdsl;

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
import de.marx_software.webtools.api.TimeWindow;
import de.marx_software.webtools.api.actions.InvalidSegmentException;
import de.marx_software.webtools.api.datalayer.SegmentData;
import de.marx_software.webtools.api.actions.SegmentService;
import de.marx_software.webtools.api.analytics.AnalyticsDB;
import de.marx_software.webtools.api.analytics.Fields;
import de.marx_software.webtools.core.modules.actionsystem.UserSegmentGenerator;
import de.marx_software.webtools.core.modules.actionsystem.TestHelper;
import de.marx_software.webtools.core.modules.actionsystem.dsl.JsonDsl;
import de.marx_software.webtools.core.modules.actionsystem.segmentation.AbstractTest;
import de.marx_software.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import de.marx_software.webtools.test.MockAnalyticsDB;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Set;
import java.util.UUID;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author thmarx
 */
public class EventTest extends AbstractTest {

	AnalyticsDB analytics;
	SegmentService service;
	UserSegmentGenerator userSegmentGenerator;

	String segment_id;

	@BeforeClass
	public void setUpClass() throws IOException, InvalidSegmentException {
		long timestamp = System.currentTimeMillis();


		MBassador mbassador = new MBassador();

		analytics = new MockAnalyticsDB();

		service = new EntitiesSegmentService(entities());

//		segment_id = createSegment(service, "Buyer", new TimeWindow(TimeWindow.UNIT.YEAR, 1), "segment().site('testSite').and(rule(EVENT).event('order').count(2))");
		segment_id = createSegment(service, "Buyer", new TimeWindow(TimeWindow.UNIT.YEAR, 1), loadContent("src/test/resources/segments/newdsl/event.json"), "testSite");

		System.out.println("service: " + service.all());

		userSegmentGenerator = new UserSegmentGenerator(analytics, new JsonDsl(new DefaultServiceRegistry()), service);
	}

	/**
	 * Test of open method, of class AnalyticsDb.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
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


		assertThat(userSegmentGenerator.generate("peter2")).isEmpty();

		event.put(Fields._UUID.value(), UUID.randomUUID().toString());
		analytics.track(TestHelper.event(event, new JSONObject()));


		List<SegmentData> getList = userSegmentGenerator.generate("peter2");
		Set<String> segments = getRawSegments(getList);
		assertThat(segments).isNotEmpty();
		assertThat(segments).containsExactly(segment_id);
	}
}
