package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions;

import com.thorstenmarx.webtools.actions.dsl.graal.VarFunction;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.NOT;

/**
 *
 * @author marx
 */
public class NotFunc implements VarFunction<Conditional, NOT> {
	
	@Override
	public NOT apply(Conditional... conditionals) {
		return new NOT(conditionals);
	}
	
}
