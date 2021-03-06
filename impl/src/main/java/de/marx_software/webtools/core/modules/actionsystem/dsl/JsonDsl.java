/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.marx_software.webtools.core.modules.actionsystem.dsl;

import de.marx_software.webtools.core.modules.actionsystem.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thorstenmarx.modules.api.ServiceRegistry;
import de.marx_software.webtools.api.actions.Conditional;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.ecommerce.ECommerceCouponRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.CampaignRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.KeyValueRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.ReferrerRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.PageViewRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.CategoryRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.EventRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.FirstVisitRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.ScoreRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.VisitRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.ecommerce.ECommercePercentageOfOrderAverageValueRule;
import de.marx_software.webtools.core.modules.actionsystem.dsl.rules.ecommerce.ECommerceOrderRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author marx
 */
public class JsonDsl {

	private final Gson gson;

	private final Map<String, Class<? extends Conditional>> conditionals = new HashMap<>();

	private final ServiceRegistry serviceRegistry;

	public static ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

	public JsonDsl(final ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;

		conditionals.put("pageview", PageViewRule.class);
		conditionals.put("campaign", CampaignRule.class);
		conditionals.put("category", CategoryRule.class);
		conditionals.put("event", EventRule.class);
		conditionals.put("firstvisit", FirstVisitRule.class);
		conditionals.put("keyvalue", KeyValueRule.class);
		conditionals.put("referrer", ReferrerRule.class);
		conditionals.put("score", ScoreRule.class);
		conditionals.put("visit", VisitRule.class);

		conditionals.put("ecommerce_coupon", ECommerceCouponRule.class);
		conditionals.put("ecommerce_order", ECommerceOrderRule.class);

		conditionals.put("ecommerce_aov_percentage", ECommercePercentageOfOrderAverageValueRule.class);

		this.gson = new GsonBuilder()
				.registerTypeAdapter(ECommercePercentageOfOrderAverageValueRule.class, new ECommercePercentageOfOrderAverageValueRule.Creator(serviceRegistry))
				.create();
	}

	public DSLSegment parse(final String content, final Context context) {

		try {
			CONTEXT.set(context);

			JsonElement conditionElement = JsonParser.parseString(content);

			DSLSegment segment = new DSLSegment();
			if (conditionElement.isJsonObject()) {
				JsonObject object = conditionElement.getAsJsonObject();
			}
			Optional<Conditional> handleCondition = handleCondition(conditionElement);
			if (handleCondition.isPresent()) {
				segment.conditional(handleCondition.get());
			}

			return segment;

		} finally {
			CONTEXT.remove();
		}
	}

	private Optional<Conditional> handleCondition(final JsonElement condition) {
		if (!condition.isJsonObject()) {
			return Optional.empty();
		}
		JsonObject conditionObject = condition.getAsJsonObject();
		if (!conditionObject.has("conditional")) {
			return Optional.empty();
		}
		final String type = conditionObject.get("conditional").getAsString();
		if (type.equals("or")) {
			JsonArray condtionsArray = conditionObject.get("conditions").getAsJsonArray();
			OR or = new OR(handleConditions(condtionsArray));
			return Optional.of(or);
		} else if (type.equals("and")) {
			JsonArray condtionsArray = conditionObject.get("conditions").getAsJsonArray();
			AND and = new AND(handleConditions(condtionsArray));
			return Optional.of(and);
		} else if (type.equals("not")) {
			JsonArray condtionsArray = conditionObject.get("conditions").getAsJsonArray();
			NOT not = new NOT(handleConditions(condtionsArray));
			return Optional.of(not);
		} else if (conditionals.containsKey(type)) {
			return Optional.ofNullable(gson.fromJson(condition, conditionals.get(type)));
		}

		return Optional.empty();
	}

	private List<Conditional> handleConditions(final JsonArray conditionsArray) {

		final List<Conditional> conditionals = new ArrayList<>();

		for (final JsonElement conditionElement : conditionsArray) {
			Optional<Conditional> conditional = handleCondition(conditionElement);
			if (conditional.isPresent()) {
				conditionals.add(conditional.get());
			}
		}

		return conditionals;
	}
}
