package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ecommerce;

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
import com.thorstenmarx.webtools.collection.CounterMapMap;
import com.thorstenmarx.webtools.core.modules.actionsystem.util.Counter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thmarx
 */
public class ECommerceOrderRule implements Conditional {

	public static final String RULE = "ECOMMERCE_ORDER";

	private static final String EVENT = "ecommerce_order";

	private int count;
	private boolean exact = false;

	private final Counter counter;

	public ECommerceOrderRule() {
		this.counter = new Counter();
	}

	public ECommerceOrderRule exact() {
		this.exact = true;

		return this;
	}

	public int count() {
		return count;
	}

	public ECommerceOrderRule count(int count) {
		this.count = count;
		return this;
	}

	@Override
	public String toString() {
		return "ECommerceOrderRule{count=" + count + '}';
	}

	@Override
	public void match() {

	}

	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public void handle(final ShardDocument doc) {
		final String docEvent = doc.document.getString("event");
		if (EVENT.equals(docEvent)) {
			final String userid = doc.document.getString("userid");
			counter.add(userid);
		}
	}

	@Override
	public boolean affected(JSONObject event) {
		final String docEvent = event.getString("event");
		if (EVENT.equals(docEvent)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean matchs(String userid) {
		if (exact) {
			return counter.get(userid) == count;
		} else {
			return counter.get(userid) >= count;
		}
	}
}
