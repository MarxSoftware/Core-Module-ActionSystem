package com.thorstenmarx.webtools.core.modules.actionsystem.dsl;

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
import com.thorstenmarx.modules.api.ModuleManager;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.GraalDSL;
import com.thorstenmarx.webtools.api.actions.model.AdvancedSegment;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.Searchable;
import com.thorstenmarx.webtools.api.analytics.query.Query;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import com.thorstenmarx.webtools.api.analytics.query.ShardedQuery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thmarx
 */
public class AdvancedSegmentationQuery implements ShardedQuery<List<String>, Void, AdvancedSegment> {

	private static final Logger log = LoggerFactory.getLogger(AdvancedSegmentationQuery.class);
	
	protected final AdvancedSegment advancedSegment;

	private final List<ShardDocument> documents;

	private final DSLSegment segment;

	public AdvancedSegmentationQuery(final AdvancedSegment aSegment, final ModuleManager moduleManager) throws ScriptException {
		this.advancedSegment = aSegment;
		this.segment = new GraalDSL(moduleManager, null).build(advancedSegment.getDsl());
		documents = new ArrayList<>();
	}

	protected void handle(final ShardDocument document) {
		segment.handle(document);
	}

	@Override
	public AdvancedSegment query() {
		return advancedSegment;
	}

	@Override
	public RecursiveTask<Void> getSubTask(AdvancedSegment query, Searchable target) {
		return new SegmentationTask(this, target, segment);
	}

	@Override
	public List<String> merge(List<Void> subResults) {

		List<String> matchingUsers = new ArrayList<>();

		segment.match();
		segment.getAllUsers().stream().filter(segment::matchs).forEach(matchingUsers::add);

		return matchingUsers;
	}

	/**
	 * Bearbeitet ein Segment auf einem Shard.
	 */
	static class SegmentationTask extends RecursiveTask<Void> {

		private static final long serialVersionUID = -6413763517541683972L;

		final private AdvancedSegmentationQuery advancedQuery;
		transient final private Searchable shard;
		private final DSLSegment segment;

		SegmentationTask(final AdvancedSegmentationQuery query, final Searchable shard, final DSLSegment segment) {
			this.advancedQuery = query;
			this.shard = shard;
			this.segment = segment;
		}

		@Override
		protected Void compute() {
//			if (shard.hasData(advancedQuery.advancedSegment.start(), advancedQuery.advancedSegment.end())) {
				Query query = Query.builder().start(this.advancedQuery.advancedSegment.start()).end(advancedQuery.advancedSegment.end())
						.term(Fields.Site.value(), segment.site)
						.build();

				try {
					List<ShardDocument> documents = shard.search(query);

					documents.stream().forEach((ShardDocument doc) -> {
						advancedQuery.handle(doc);
					});
				} catch (IOException ex) {
					log.error("", ex);
				}
//			}
			return null;
		}
	}
}
