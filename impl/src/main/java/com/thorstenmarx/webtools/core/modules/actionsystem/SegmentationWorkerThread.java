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
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
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


	
private boolean shutdown = false;

//	private SegmentCalculator segmentCalculator;
	
	private final Consumer<AdvancedSegment> segmentConsumer;
	private final Supplier<Set<Segment>> segmentSupplier;
	
	public SegmentationWorkerThread(int index, final Supplier<Set<Segment>> segmentSupplier, final Consumer<AdvancedSegment> segmentConsumer) {
		this.segmentSupplier = segmentSupplier;
		this.segmentConsumer = segmentConsumer;
//		this.dslRunner = new GraalDSL(moduleManager, null);
		setDaemon(true);
		this.workerName = CONSUMER_NAME + "_" + index;
	}

	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				final Set<Segment> segments = segmentSupplier.get();
				handleSegments(segments);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	private void handleSegments(final Set<Segment> segments) {

		final Set<AdvancedSegment> advancedSegments = segments.stream().filter(AdvancedSegment.class::isInstance).map(AdvancedSegment.class::cast).collect(Collectors.toSet());

		advancedSegments.stream().filter(s -> s.isActive()).forEach(segmentConsumer);
	}

	public void forceSegmenteGeneration(final Segment segment) {
		handleSegments(Sets.newHashSet(segment));
	}
}
