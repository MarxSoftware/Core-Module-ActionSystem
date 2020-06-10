package com.thorstenmarx.webtools.core.modules.actionsystem;

import com.thorstenmarx.modules.api.ServiceRegistry;
import com.thorstenmarx.modules.api.DefaultServiceRegistry;
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.query.Aggregator;
import com.thorstenmarx.webtools.api.analytics.query.Query;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.api.entities.criteria.Restrictions;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.DSLSegment;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.JsonDsl;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public class UserSegmentGenerator {

	public static final Logger LOGGER = LoggerFactory.getLogger(UserSegmentGenerator.class);

	final AnalyticsDB db;
	final JsonDsl dslRunner;
	final SegmentService segmentService;

	final ServiceRegistry serviceRegistry;

	public UserSegmentGenerator(final AnalyticsDB db, final JsonDsl dslRunner, final SegmentService segmentService) {
		this(db, dslRunner, segmentService, new DefaultServiceRegistry());
	}

	public UserSegmentGenerator(final AnalyticsDB db, final JsonDsl dslRunner, final SegmentService segmentService, final ServiceRegistry serviceRegistry) {
		this.db = db;
		this.dslRunner = dslRunner;
		this.segmentService = segmentService;
		this.serviceRegistry = serviceRegistry;
	}

	public List<SegmentData> generate(final String userid) {
		return generate(userid, null);
	}

	public List<SegmentData> generate(final String userid, final String site) {
		Context context = new Context();
		context.site = site;
		context.userid = userid;
		return get(context).stream().map((segment) -> {

			final SegmentData segmentData = new SegmentData();
			segmentData.setSegment(new SegmentData.Segment(segment.getName(), segment.getExternalId(), segment.getId()));
			return segmentData;
		}).collect(Collectors.toList());

	}

	protected List<Segment> get(Context context) {
		List<Segment> activeSegments;
		if (context.site != null) {
			activeSegments = segmentService.criteria().add(Restrictions.EQ.eq("site", context.site)).query().stream()
					.filter(Segment::isActive)
					.collect(Collectors.toList());
		} else {
			activeSegments = segmentService.all().stream().filter(Segment::isActive).collect(Collectors.toList());
		}
		return activeSegments.stream().filter((segment) -> segment_contains_userdata(segment, context)).collect(Collectors.toList());
	}

	public boolean segment_contains_userdata(final Segment segment, final Context context) {
		try {
			Query simpleQuery = Query.builder().start(segment.start()).end(segment.end())
					.term(Fields.UserId.value(), context.userid)
					.term(Fields.Site.value(), segment.getSite())
					.build();

			Future<Boolean> future;
			future = db.query(simpleQuery, new Aggregator<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					DSLSegment dsl = dslRunner.parse(segment.getContent(), context);
					documents.stream().forEach(dsl::handle);
					dsl.match();
					return dsl.matchs(context.userid);
				}
			});

			return future != null ? future.get() : false;
		} catch (InterruptedException | ExecutionException ex) {
			LOGGER.error("", ex);
		}

		return false;
	}
}
