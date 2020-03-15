/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thorstenmarx.webtools.core.modules.actionsystem.dsl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ecommerce.ECommerceCouponRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.CampaignRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.KeyValueRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ReferrerRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.PageViewRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.CategoryRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.EventRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.FirstVisitRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ScoreRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.VisitRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ecommerce.ECommerceOrderRule;
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

	private final Gson gson = new Gson();

	private Map<String, Class<? extends Conditional>> conditionals = new HashMap<>();

	public JsonDsl() {
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
	}

	public DSLSegment parse(final String content) {
		JsonElement conditionElement = JsonParser.parseString(content);
		
		DSLSegment segment = new DSLSegment();
		if (conditionElement.isJsonObject()) {
			JsonObject object = conditionElement.getAsJsonObject();
			if (object.has("site")) {
				segment.site(object.get("site").getAsString());
			}
		}
		Optional<Conditional> handleCondition = handleCondition(conditionElement);
		if (handleCondition.isPresent()) {
			segment.conditional(handleCondition.get());
		}
		
		return segment;
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
