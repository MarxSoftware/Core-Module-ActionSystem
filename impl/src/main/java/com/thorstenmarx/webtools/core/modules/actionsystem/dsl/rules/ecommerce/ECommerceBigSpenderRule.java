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
import com.google.gson.InstanceCreator;
import com.thorstenmarx.modules.api.ServiceRegistry;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import com.thorstenmarx.webtools.core.modules.actionsystem.UserSegmentGenerator;
import com.thorstenmarx.webtools.core.modules.actionsystem.util.CounterDouble;
import com.thorstenmarx.webtools.modules.metrics.api.MetricsService;
import java.lang.reflect.Type;

/**
 *
 * @author thmarx
 */
public class ECommerceBigSpenderRule implements Conditional {

	public static final String RULE = "ECOMMERCE_BIG_SPENDER";

	private static final String EVENT = "ecommerce_order";

	private final CounterDouble counter;

	private final ServiceRegistry registry;

	public ECommerceBigSpenderRule(final ServiceRegistry registry) {
		this.registry = registry;
		this.counter = new CounterDouble();
	}

	@Override
	public String toString() {
		return "ECommerceBigSpenderRule{}";
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
			final Object total = doc.document.get("c_order_total");
			counter.add(userid, toDouble(total));
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
		if (!registry.exists(MetricsService.class)) {
			return false;
		}
		MetricsService service = registry.single(MetricsService.class).get();
		final String site = UserSegmentGenerator.CONTEXT.get() != null ? UserSegmentGenerator.CONTEXT.get().site : null;
		try {
			final Number order_average = service.getKpi("order_average_value", site, 0, System.currentTimeMillis());
			if (counter.get(userid) >= order_average.doubleValue()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private double toDouble(final Object value) {
		if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof Float) {
			return Float.class.cast(value).doubleValue();
		} else if (value instanceof String) {
			return Double.valueOf(((String) value).trim());
		}

		return 0d;
	}

	public static class Creator implements InstanceCreator<ECommerceBigSpenderRule> {

		final ServiceRegistry registry;

		public Creator(final ServiceRegistry registry) {
			this.registry = registry;
		}

		@Override
		public ECommerceBigSpenderRule createInstance(final Type type) {
			return new ECommerceBigSpenderRule(registry);
		}

	}

}
