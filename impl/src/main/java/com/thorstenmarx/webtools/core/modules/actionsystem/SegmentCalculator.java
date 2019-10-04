package com.thorstenmarx.webtools.core.modules.actionsystem;

/*-
 * #%L
 * webtools-actions
 * %%
 * Copyright (C) 2016 - 2019 Thorsten Marx
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

import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.GraalDSL;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.query.Aggregator;
import com.thorstenmarx.webtools.api.analytics.query.Query;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.DSLSegment;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public class SegmentCalculator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SegmentCalculator.class);

	private final AnalyticsDB db;
	private final GraalDSL dslRunner;

	public SegmentCalculator(final AnalyticsDB db, final GraalDSL dslRunner) {
		this.db = db;
		this.dslRunner = dslRunner;
	}

	public Result calculate(final AdvancedSegment segment) {

		Query simpleQuery = Query.builder().start(segment.start()).end(segment.end()).build();

		Future<Set<String>> future;
		future = db.query(simpleQuery, new Aggregator<Set<String>>() {
			@Override
			public Set<String> call() throws Exception {
				DSLSegment dsl;
				if (segment instanceof AdvancedSegment) {
					AdvancedSegment aseg = (AdvancedSegment) segment;
					if (aseg.getDsl() == null) {
						aseg.setDsl(aseg.getContent());
					}
					dsl = dslRunner.build(aseg.getDsl());
				} else {
					throw new IllegalStateException("unkown segment definition");
				}
				documents.stream().forEach(dsl::handle);
				dsl.match();

				Set<String> matchingUsers = new HashSet<>();
				dsl.getAllUsers().stream().filter(dsl::matchs).forEach(matchingUsers::add);

				return matchingUsers;
			}
		});
		try {
			return new Result(segment, future.get());
		} catch (InterruptedException | ExecutionException ex) {
			LOGGER.error("", ex);
		}
		return new Result(segment, Collections.EMPTY_SET);
	}

	public static class Result {

		public final AdvancedSegment segment;
		public final Set<String> users;

		public Result(AdvancedSegment segment, Set<String> users) {
			this.segment = segment;
			this.users = users;
		}

	}
}
