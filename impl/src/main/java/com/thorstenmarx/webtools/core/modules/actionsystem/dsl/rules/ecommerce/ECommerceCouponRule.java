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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thmarx
 */
public class ECommerceCouponRule implements Conditional {

	private static final Logger LOGGER = LoggerFactory.getLogger(ECommerceCouponRule.class);

	public static final String RULE = "ECOMMERCE_COUPON";

	private static final String EVENT = "ecommerce_order";

	private static final String COUPON_COUNT_FIELD_NAME = "order_coupons_count";

	private int count;
	private boolean exact = false;


	private final Counter counter;

	public ECommerceCouponRule() {
		counter = new Counter();
	}

	public ECommerceCouponRule exact() {
		this.exact = true;

		return this;
	}

	public int count() {
		return count;
	}

	public ECommerceCouponRule count(int count) {
		this.count = count;
		return this;
	}

	@Override
	public String toString() {
		return "ECommerceCouponRule{" + "count=" + count + ", exact=" + exact + '}';
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
		if (EVENT.equals(docEvent) && doc.document.containsKey(COUPON_COUNT_FIELD_NAME)) {

			final String userid = doc.document.getString("userid");
			int couponsUsed = getCouponCount(doc.document.get(COUPON_COUNT_FIELD_NAME));
			counter.add(userid, couponsUsed);
		}
	}

	private int getCouponCount(final Object couponCountField) {
		if (couponCountField instanceof Integer) {
			return (int) couponCountField;
		} else if (couponCountField instanceof String) {
			return Integer.valueOf(String.valueOf(couponCountField));
		}

		return 0;
	}

	@Override
	public boolean affected(JSONObject event) {
		final String docEvent = event.getString("event");
		if (EVENT.equals(docEvent) && event.containsKey(COUPON_COUNT_FIELD_NAME)) {
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
