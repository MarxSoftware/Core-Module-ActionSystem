package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions;

import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.DSLSegment;
import java.util.function.Supplier;

/**
 *
 * @author marx
 */
public class SegmentFunc implements Supplier<DSLSegment> {
	
	private final DSLSegment segment;

	public SegmentFunc(final DSLSegment segment) {
		this.segment = segment;
	}

	@Override
	public DSLSegment get() {
		return segment;
	}
	
}
