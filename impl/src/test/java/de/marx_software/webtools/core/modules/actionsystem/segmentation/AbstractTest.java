package de.marx_software.webtools.core.modules.actionsystem.segmentation;

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

import de.marx_software.webtools.api.TimeWindow;
import de.marx_software.webtools.api.actions.InvalidSegmentException;
import de.marx_software.webtools.api.actions.SegmentService;
import de.marx_software.webtools.api.actions.model.Segment;
import de.marx_software.webtools.api.datalayer.SegmentData;
import de.marx_software.webtools.api.entities.Entities;
import de.marx_software.webtools.core.modules.actionsystem.segmentStore.LocalUserSegmentStore;
import de.marx_software.webtools.test.MockEntities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	
	protected String createSegment (final SegmentService service, final String name, final TimeWindow start, final String dsl, final String site) throws InvalidSegmentException {
		Segment tester = new Segment();
		// TODO: entities kann nicht mit id umgehen, da es denkt, es handelt ischum ein update
		tester.setName(name);
		tester.setActive(true);
		tester.start(start);
		tester.setContent(dsl);
		tester.setSite(site);
		service.add(tester);
		
		return tester.getId();
	}
	
	protected void await(final LocalUserSegmentStore userSegmentStore, final String USER_ID, final int count) {
		Awaitility.await().atMost(100, TimeUnit.SECONDS).until(() ->
				!userSegmentStore.get(USER_ID).isEmpty() && userSegmentStore.get(USER_ID).size() == count
		);
	}
	
	protected static Set<String> getRawSegments(List<SegmentData> data) {
		return data.stream().map(SegmentData::getSegment).map((s) -> s.id).collect(Collectors.toSet());
	}
	
	protected String loadContent (final String file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file)));
	}
}
