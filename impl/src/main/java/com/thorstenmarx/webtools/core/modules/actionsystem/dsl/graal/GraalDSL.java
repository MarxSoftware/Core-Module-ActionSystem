package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal;

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
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.modules.api.ModuleManager;
import com.thorstenmarx.webtools.actions.dsl.graal.VarFunction;
import com.thorstenmarx.webtools.api.extensions.SegmentationRuleExtension;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.AND;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.DSLSegment;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.EventAction;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.NOT;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.OR;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.CampaignRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.CategoryRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.EventRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.FirstVisitRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.KeyValueRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.PageViewRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ReferrerRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules.ScoreRule;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal.functions.*;
import com.thorstenmarx.webtools.scripting.GraalScripting;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import net.engio.mbassy.bus.MBassador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public class GraalDSL {

	private static final Logger log = LoggerFactory.getLogger(GraalDSL.class);

	private final ModuleManager moduleManager;
	private final MBassador eventBus;

	private final GraalScripting engine;

	public GraalDSL(final ModuleManager moduleManager, final MBassador eventBus) {
		this.moduleManager = moduleManager;
		this.eventBus = eventBus;
		engine = new GraalScripting("com/thorstenmarx/webtools/core/modules/actionsystem/dsl/graal/modules/");
	}

	private void initExtensions(final ScriptEngine engine, final ConcurrentMap<String, Supplier<Conditional>> rules) {
		moduleManager.extensions(SegmentationRuleExtension.class).forEach(sr -> {
			rules.put(sr.getKey(), sr.getRule());
			engine.put(sr.getKey(), sr.getKey());
		});
	}

	public EventAction buildAction(final String event, final String content) throws ScriptException {
//		ScriptEngineManager manager = new ScriptEngineManager();
//		ScriptEngine engine = manager.getEngineByName("nashorn");

		ConcurrentMap<String, Supplier<Conditional>> rules = new ConcurrentHashMap<>();
		rules.put(PageViewRule.RULE, () -> new PageViewRule());
		rules.put(ScoreRule.RULE, () -> new ScoreRule());
		rules.put(EventRule.RULE, () -> new EventRule());
		rules.put(KeyValueRule.RULE, () -> new KeyValueRule());
		rules.put(FirstVisitRule.RULE, () -> new FirstVisitRule());
		rules.put(CampaignRule.RULE, () -> new CampaignRule());
		rules.put(ReferrerRule.RULE, () -> new ReferrerRule());
		rules.put(CategoryRule.RULE, () -> new CategoryRule());

		if (moduleManager != null) {
//			initExtensions(engine, rules);
		}

		final EventActionFunc eventAction = new EventActionFunc(eventBus);
		engine.eval(content, (context) -> {
			Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

			bindings.put("eventAction", eventAction);
			bindings.put("minutes", (Function<Integer, Long>) (Integer t) -> TimeUnit.MINUTES.toMillis(t));
			bindings.put("hours", (Function<Integer, Long>) (Integer t) -> TimeUnit.HOURS.toMillis(t));
			bindings.put("days", (Function<Integer, Long>) (Integer t) -> TimeUnit.DAYS.toMillis(t));
			bindings.put("weeks", (Function<Integer, Long>) (Integer t) -> 7l * TimeUnit.DAYS.toMillis(t));
			bindings.put("months", (Function<Integer, Long>) (Integer t) -> 30l * TimeUnit.DAYS.toMillis(t));
			bindings.put("years", (Function<Integer, Long>) (Integer t) -> 365l * TimeUnit.DAYS.toMillis(t));
			bindings.put("and", new AndFunc());
			bindings.put("or", new OrFunc());
			bindings.put("not", new NotFunc());
			bindings.put("rule", new RuleFunc(rules));
			bindings.put(PageViewRule.RULE, PageViewRule.RULE);
			bindings.put(ScoreRule.RULE, ScoreRule.RULE);
			bindings.put(EventRule.RULE, EventRule.RULE);
			bindings.put(KeyValueRule.RULE, KeyValueRule.RULE);
			bindings.put(FirstVisitRule.RULE, FirstVisitRule.RULE);
			bindings.put(CampaignRule.RULE, CampaignRule.RULE);
			bindings.put(ReferrerRule.RULE, ReferrerRule.RULE);
			bindings.put(CategoryRule.RULE, CategoryRule.RULE);

		});

		return eventAction.eventAction;
	}

	public DSLSegment build(final String content) throws ScriptException {
		ConcurrentMap<String, Supplier<Conditional>> rules = new ConcurrentHashMap<>();
		rules.put(PageViewRule.RULE, () -> new PageViewRule());
		rules.put(ScoreRule.RULE, () -> new ScoreRule());
		rules.put(EventRule.RULE, () -> new EventRule());
		rules.put(KeyValueRule.RULE, () -> new KeyValueRule());
		rules.put(FirstVisitRule.RULE, () -> new FirstVisitRule());
		rules.put(CampaignRule.RULE, () -> new CampaignRule());
		rules.put(ReferrerRule.RULE, () -> new ReferrerRule());
		rules.put(CategoryRule.RULE, () -> new CategoryRule());

		if (moduleManager != null) {
//			initExtensions(engine, rules);
		}

		final DSLSegment segment = new DSLSegment();
		engine.eval(content, (context) -> {

			Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

			bindings.put("segment", new SegmentFunc(segment));
			bindings.put("and", new AndFunc());
			bindings.put("or", new OrFunc());
			bindings.put("not", new NotFunc());
			bindings.put("rule", new RuleFunc(rules));
			bindings.put(PageViewRule.RULE, PageViewRule.RULE);
			bindings.put(ScoreRule.RULE, ScoreRule.RULE);
			bindings.put(EventRule.RULE, EventRule.RULE);
			bindings.put(KeyValueRule.RULE, KeyValueRule.RULE);
			bindings.put(FirstVisitRule.RULE, FirstVisitRule.RULE);
			bindings.put(CampaignRule.RULE, CampaignRule.RULE);
			bindings.put(ReferrerRule.RULE, ReferrerRule.RULE);
			bindings.put(CategoryRule.RULE, CategoryRule.RULE);
		});

		return segment;
	}

	public static void main(final String... args) throws ScriptException {

		StringBuilder sb = new StringBuilder();
		sb.append("var arule = rule(PAGEVIEW).page('page');");
		sb.append("var or1 = or(arule, arule);");
		sb.append("segment().and(or1, or1);");
		DSLSegment seg = new GraalDSL(null, null).build(sb.toString());
//		System.out.println(seg.toString());
	}
}
