package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation;

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
import com.thorstenmarx.webtools.actions.segmentation.mocks.MockedExecutor;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.thorstenmarx.webtools.api.TimeWindow;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.actions.model.rules.KeyValueRule;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.datalayer.DataLayer;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import com.thorstenmarx.webtools.core.modules.actionsystem.TestHelper;
import com.thorstenmarx.webtools.test.MockAnalyticsDB;
import com.thorstenmarx.webtools.test.MockDataLayer;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.engio.mbassy.bus.MBassador;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;

/**
 *
 * @author thmarx
 */
@Test(singleThreaded = true)
public class RulesTest extends AbstractTest {

	AnalyticsDB instance;
	ActionSystemImpl actionSystem;
	MockedExecutor executor;

	DataLayer datalayer;

	@BeforeMethod
	public void setUp() throws IOException {
		MBassador mbassador = new MBassador();

		executor = new MockedExecutor();
		instance = new MockAnalyticsDB();

		datalayer = new MockDataLayer();

		actionSystem = new ActionSystemImpl(instance, new EntitiesSegmentService(entities()), null, mbassador, datalayer, executor);
		actionSystem.start();
	}

	@AfterMethod
	public void tearDown() throws IOException, Exception {
		actionSystem.close();
		executor.shutdown();
	}

	/**
	 * Test of open method, of class AnalyticsDb.
	 *
	 * @throws java.lang.Exception
	 */
	@Test(enabled = false)
	public void test_keyvalue_rule() throws Exception {

		Segment iphoneUser = new Segment();
		iphoneUser.setId("iphone");
		iphoneUser.setName("iPhone");
		iphoneUser.start(new TimeWindow(TimeWindow.UNIT.YEAR, 1));
		KeyValueRule kv = new KeyValueRule();
		kv.key("device");
		kv.values(new String[]{"iphone"});
		iphoneUser.addRule(kv);
		actionSystem.addSegment(iphoneUser);

		Map<String, Object> event = new HashMap<>();
		event.put(Fields._TimeStamp.value(), System.currentTimeMillis());
		event.put("userid", "u1");
		event.put("device", "iphone");

		instance.track(TestHelper.event(event, new JSONObject()));

		Awaitility.await().atMost(60, TimeUnit.SECONDS).until(()
				-> datalayer.exists("u1", SegmentData.KEY) && !datalayer.get("u1", SegmentData.KEY, SegmentData.class).get().getSegments().isEmpty()
		);

		Set<String> segments = datalayer.get("u1", SegmentData.KEY, SegmentData.class).get().getSegments();
		Assertions.assertThat(segments).isNotNull().isNotEmpty().contains("iphone");
	}

}
