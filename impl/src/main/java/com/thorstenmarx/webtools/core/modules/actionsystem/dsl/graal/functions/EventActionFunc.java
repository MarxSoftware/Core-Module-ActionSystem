package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions;

import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.EventAction;
import java.util.function.Function;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author marx
 */
public class EventActionFunc implements Function<String, EventAction> {
	
	public EventAction eventAction;
	private final MBassador eventBus;

	public EventActionFunc(final MBassador eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public EventAction apply(String event) {
		eventAction = new EventAction(event, eventBus);
		return eventAction;
	}
	
}
