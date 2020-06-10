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
import com.thorstenmarx.webtools.core.modules.actionsystem.util.CounterInt;
import com.thorstenmarx.webtools.api.metrics.MetricsService;
import com.thorstenmarx.webtools.core.modules.actionsystem.Context;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.JsonDsl;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thmarx
 */
public class ECommercePercentageOfOrderAverageValueRule implements Conditional {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ECommercePercentageOfOrderAverageValueRule.class);

	public static final String RULE = "ECOMMERCE_PERCENTAGE_OF_ORDER_AVERAGE_VALUE";

	private static final String EVENT = "ecommerce_order";

	private final CounterDouble orderValueCounter;
	private final CounterInt orderCounter;
	private final Set<String> orders;

	private final ServiceRegistry registry;
	
	private float percentage = 150;
	
	private Comparator comparator = Comparator.GREATER_EQUALS;
	
	private final Context context;

	public ECommercePercentageOfOrderAverageValueRule(final ServiceRegistry registry, final Context context) {
		this.registry = registry;
		this.context = context;
		this.orderValueCounter = new CounterDouble();
		this.orderCounter = new CounterInt();
		this.orders = new HashSet<>();
	}
	
	public ECommercePercentageOfOrderAverageValueRule setPercentage (final float percentage) {
		this.percentage = percentage;
		return this;
	}
	
	public ECommercePercentageOfOrderAverageValueRule setComparator (final Comparator comparator) {
		this.comparator = comparator;
		return this;
	}

	@Override
	public String toString() {
		return "ECommercePercentageOfOrderAverageValueRule{}";
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
			final String order_id = doc.document.getString("c_order_id");
			final Object total = doc.document.get("c_order_total");
			
			
			if (!orders.contains(order_id)) {
				orders.add(order_id);
				orderCounter.add(userid);
				orderValueCounter.add(userid, toDouble(total));
			}
		}
	}

	@Override
	public boolean affected(final JSONObject event) {
		final String docEvent = event.getString("event");
		if (EVENT.equals(docEvent)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean matchs(final String userid) {
		if (!registry.exists(MetricsService.class)) {
			return false;
		} else if (orderCounter.get(userid) == 0) {
			return false;
		}
		Optional<MetricsService> serviceOptional = registry.single(MetricsService.class);
		if (serviceOptional.isEmpty()) {
			return false;
		}
		MetricsService service = serviceOptional.get();
		try {
			final Number order_average = service.getKpi("average_order_value", context.site, 0, System.currentTimeMillis());
			
			double user_value = orderValueCounter.get(userid) / orderCounter.get(userid);
			double user_percentage = calculatePercentage(user_value, order_average.doubleValue());
//			if (user_percentage >= percentage) {
			if (comparator.compare(user_percentage, percentage)) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}
	public double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
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

	public static class Creator implements InstanceCreator<ECommercePercentageOfOrderAverageValueRule> {

		final ServiceRegistry registry;

		public Creator(final ServiceRegistry registry) {
			this.registry = registry;
		}

		@Override
		public ECommercePercentageOfOrderAverageValueRule createInstance(final Type type) {
			return new ECommercePercentageOfOrderAverageValueRule(registry, JsonDsl.CONTEXT.get());
		}

	}
	
	public static enum Comparator {
		EQUAL ((obtained, total) -> {
			return obtained.compareTo(total) == 0;
		}),
		LESS ((obtained, total) -> {
			return obtained.compareTo(total) < 0;
		}),
		GREATER ((obtained, total) -> {
			return obtained.compareTo(total) > 0;
		}),
		LESS_EQUALS ((obtained, total) -> {
			return LESS.function.apply(obtained, total)
					|| EQUAL.function.apply(obtained, total);
		}),
		GREATER_EQUALS ((obtained, total) -> {
			return GREATER.function.apply(obtained, total)
					|| EQUAL.function.apply(obtained, total);
		}),
		
		;
		
		private final BiFunction<Double, Double, Boolean> function;
		
		private Comparator (final BiFunction<Double, Double, Boolean> function) {
			this.function = function;
		}
		
		public boolean compare (final double obtained, final double total) {
			return function.apply(obtained, total);
		}
	}

}
