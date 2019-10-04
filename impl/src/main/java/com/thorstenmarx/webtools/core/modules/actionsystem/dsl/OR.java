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

import com.alibaba.fastjson.JSONObject;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import java.util.Arrays;

/**
 *
 * @author marx
 */
public class OR implements Conditional {

	private final Conditional[] conditionals;

	public OR(final Conditional[] conditionals) {
		this.conditionals = conditionals;
	}

	@Override
	public boolean matchs(final String userid) {
		if (conditionals == null) {
			return true;
		}
		return Arrays.stream(conditionals).anyMatch(t -> t.matchs(userid));
	}
	@Override
	public void match() {
		if (conditionals != null) {
			Arrays.stream(conditionals).forEach(Conditional::match);
		}
	}

	@Override
	public boolean valid() {
		if (conditionals == null) {
			return true;
		}
		return Arrays.stream(conditionals).allMatch(Conditional::valid);
	}

	@Override
	public void handle(final ShardDocument doc) {
		Arrays.stream(conditionals).forEach(c -> c.handle(doc));
	}

	@Override
	public boolean affected(final JSONObject event) {
		return Arrays.stream(conditionals).anyMatch(t -> t.affected(event));
	}

}
