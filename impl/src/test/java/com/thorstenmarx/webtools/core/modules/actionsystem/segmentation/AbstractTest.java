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
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.entities.Entities;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentStore;
import com.thorstenmarx.webtools.test.MockEntities;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;

/**
 *
 * @author marx
 */
public abstract class AbstractTest {

	Entities entities;

	public Entities entities() {
		if (entities == null) {
			entities = new MockEntities();
		}

		return entities;
	}
	
	protected String createSegment (final SegmentService service, String name, TimeWindow start, String dsl) {
		AdvancedSegment tester = new AdvancedSegment();
		// TODO: entities kann nicht mit id umgehen, da es denkt, es handelt ischum ein update
		tester.setName(name);
		tester.setActive(true);
		tester.start(start);
		tester.setDsl(dsl);
		service.add(tester);
		
		return tester.getId();
	}
	
	protected void await(final UserSegmentStore userSegmentStore, final String USER_ID, final int count) {
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
				!userSegmentStore.get(USER_ID).isEmpty() && userSegmentStore.get(USER_ID).size() == count
		);
	}
	
	protected static Set<String> getRawSegments(List<SegmentData> data) {
		return data.stream().map(SegmentData::getSegment).map((s) -> s.id).collect(Collectors.toSet());
	}
}
