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



import com.thorstenmarx.webtools.api.TimeWindow;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.actions.model.rules.EventRule;
import com.thorstenmarx.webtools.api.actions.model.rules.PageViewRule;
import com.thorstenmarx.webtools.api.actions.model.rules.ScoreRule;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;


/**
 *
 * @author marx
 */
public class SegmentServiceTest extends AbstractTest {
	
	EntitiesSegmentService service;

	@Test
	public void openNoneExistingService() {
		service = new EntitiesSegmentService(entities());
	}

	@Test(dependsOnMethods = "openNoneExistingService")
	public void addSegments() {
		
		Segment segment = new AdvancedSegment();
		segment.setName("Mode");
		segment.start(new TimeWindow(TimeWindow.UNIT.MINUTE, 22));
		segment.addRule(new EventRule().count(2).event("pageView").site("demosite"));
		service.add(segment);
		
		segment = new AdvancedSegment();
		segment.setName("Mobile user");
		segment.start(new TimeWindow(TimeWindow.UNIT.HOUR, 24));
		segment.addRule(new ScoreRule().score(1000).name("iphone"));
		service.add(segment);
		
		segment = new AdvancedSegment();
		segment.setName("Visitor");
		segment.start(new TimeWindow(TimeWindow.UNIT.HOUR, 24));
		segment.addRule(new PageViewRule().page("testpage").site("testsite"));
		segment.addRule(new ScoreRule().score(1000).name("iphone"));
		
		service.add(segment);
		
		Assertions.assertThat(service.all().size()).isEqualTo(3);
	}
	
	@Test(dependsOnMethods = "addSegments")
	public void reopenExistingSerivce() {
		Assertions.assertThat(service.all().size()).isEqualTo(3);
	}
}
