package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions;

import com.thorstenmarx.webtools.actions.dsl.graal.VarFunction;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.OR;

/**
 *
 * @author marx
 */
public class OrFunc implements VarFunction<Conditional, OR> {
	
	@Override
	public OR apply(Conditional... conditionals) {
		return new OR(conditionals);
	}
	
}
