package com.thorstenmarx.webtools.core.modules.actionsystem;

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
import com.google.common.collect.Sets;
import com.thorstenmarx.modules.api.ModuleManager;
import com.thorstenmarx.webtools.api.TimeWindow;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.GraalDSL;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.api.datalayer.DataLayer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der Thread läuft die ganze Zeit über die Daten und erzeugt die User
 * Segmentierung
 *
 * @author thmarx
 */
public class SegmentationWorkerThread extends Thread {

	private static final Logger log = LoggerFactory.getLogger(SegmentationWorkerThread.class);

	public static final String CONSUMER_NAME = "segmentationWorker";

	private final String workerName;

	private final AnalyticsDB db;
	private final ActionSystemImpl actionSystem;
	private final ModuleManager moduleManager;
	private final CacheLayer cachelayer;
	private final GraalDSL dslRunner;

	private boolean shutdown = false;

	private SegmentCalculator segmentCalculator;

	public SegmentationWorkerThread(int index, final AnalyticsDB db, final ActionSystemImpl actionSystem, final ModuleManager moduleManager, final CacheLayer cachelayer) {
		this.db = db;
		this.actionSystem = actionSystem;
		this.moduleManager = moduleManager;
		this.cachelayer = cachelayer;
		this.dslRunner = new GraalDSL(moduleManager, null);
		setDaemon(true);
		this.workerName = CONSUMER_NAME + "_" + index;

		this.segmentCalculator = new SegmentCalculator(db, dslRunner);
	}

	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				final Set<Segment> segments = actionSystem.getSegments();
				handleSegments(segments);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	private void handleSegments(final Set<Segment> segments) {

		final Set<AdvancedSegment> advancedSegments = segments.stream().filter(AdvancedSegment.class::isInstance).map(AdvancedSegment.class::cast).collect(Collectors.toSet());

		final Map<String, Segment> segmentMap = advancedSegments.stream().filter((s) -> s.isActive()).collect(Collectors.toMap(Segment::getId,
				Function.identity()));

		final Map<String, Set<String>> userSegments = new ConcurrentHashMap<>();
		advancedSegments.stream().filter(s -> s.isActive()).forEach((segment) -> handleSegment(segment, userSegments));

		
		userSegments.forEach((user, segmentSet) -> {
			cachelayer.invalidate(CacheKey.key(user, SegmentData.KEY));
			segmentSet.forEach((s) -> {
				final SegmentData segmentData = new SegmentData();
				AdvancedSegment seg = (AdvancedSegment) segmentMap.get(s);
				segmentData.setSegment(new SegmentData.Segment(seg.getName(), seg.getExternalId(), seg.getId()));

				cachelayer.add(CacheKey.key(user, SegmentData.KEY), segmentData, SegmentData.class, 10, TimeUnit.MINUTES);
			});
		});
		// TODO: remove all not matching segments from cache
	}
	
	public void forceSegmenteGeneration(final Segment segment) {
		handleSegments(Sets.newHashSet(segment));
	}

	private void handleSegment(final AdvancedSegment segment, final Map<String, Set<String>> userSegments) {

		SegmentCalculator.Result result = segmentCalculator.calculate(segment);
		final String segID = segment.getId();
		result.users.forEach((user) -> {
			Set<String> userSegmentSet;
			if (userSegments.containsKey(user)) {
				userSegmentSet = userSegments.get(user);
			} else {
				userSegmentSet = new HashSet<>();
				userSegments.put(user, userSegmentSet);
			}
			userSegmentSet.add(segID);
		});
	}
}
