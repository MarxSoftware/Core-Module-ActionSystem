package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions;

import com.thorstenmarx.webtools.actions.dsl.graal.VarFunction;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.AND;

/**
 *
 * @author marx
 */
public class AndFunc implements VarFunction<Conditional, AND> {
	
	@Override
	public AND apply(Conditional... conditionals) {
		return new AND(conditionals);
	}
	
}
