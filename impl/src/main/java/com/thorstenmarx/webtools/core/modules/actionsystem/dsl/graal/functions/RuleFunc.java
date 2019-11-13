package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions;

import com.thorstenmarx.webtools.api.actions.Conditional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author marx
 */
public class RuleFunc implements Function<String, Conditional> {
	
	final ConcurrentMap<String, Supplier<Conditional>> rules;

	public RuleFunc(final ConcurrentMap<String, Supplier<Conditional>> rules) {
		this.rules = rules;
	}

	@Override
	public Conditional apply(String name) {
		if (rules.containsKey(name)) {
			return rules.get(name).get();
		}
		return null;
	}
	
}
