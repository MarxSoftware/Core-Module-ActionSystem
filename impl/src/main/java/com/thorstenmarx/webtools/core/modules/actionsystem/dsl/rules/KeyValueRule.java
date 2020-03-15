package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.rules;

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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import com.thorstenmarx.webtools.collection.CounterMapMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Eine einfache Key-Value Rule die generisch verwendet werden kann
 *
 * Ein User muss ein Feld (key) mit dem Wert (value) aufweisen.
 *
 * Das Document muss mindestens einen Wert in dem Feld key haben, der in values gefordert ist
 *
 * @author thmarx
 */
public class KeyValueRule implements Conditional {

	public static final String RULE = "KEYVALUE";

	private String key;
	private String[] values;
	private List<String> valuesList;

	private final CounterMapMap<String, String> results;

	private final Set<String> users;

	private enum Operator {
		AND, OR
	};
	private Operator operator = Operator.OR;

	public KeyValueRule() {
		results = new CounterMapMap<>();
		users = new HashSet<>();
	}

	public String key() {
		return key;
	}

	public KeyValueRule and() {
		this.operator = Operator.AND;
		return this;
	}

	public KeyValueRule or() {
		this.operator = Operator.OR;
		return this;
	}

	public KeyValueRule key(final String key) {
		this.key = key;
		return this;
	}

	public String[] values() {
		return values;
	}

	public KeyValueRule values(final String[] values) {
		this.values = values;
		this.valuesList = Arrays.asList(values);
		return this;
	}

	@Override
	public boolean matchs(final String userid) {
		return users.contains(userid);
	}

	@Override
	public void match() {
		results.entrySet().forEach((entry) -> {
			final String userid = entry.getKey();
			Map<String, Integer> testValues = entry.getValue();

			if (Operator.AND == operator) {
				if (testValues.keySet().stream().allMatch(valuesList::contains)) {
					users.add(userid);
				}
			} else if (Operator.OR == operator) {
				if (testValues.keySet().stream().anyMatch(valuesList::contains)) {
					users.add(userid);
				}
			}
		});
	}

	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public void handle(final ShardDocument doc) {
		if (!doc.document.containsKey(key)) {
			return;
		}

		if (valuesList == null || valuesList.isEmpty()) {
			this.valuesList = Arrays.asList(values);
		}
		
		List<String> docValues = docValues(doc.document);
		docValues.stream().filter(valuesList::contains).forEach(docValue -> {
			final String userid = doc.document.getString("userid");
			results.add(userid, docValue, 1);
		});
	}

	@Override
	public boolean affected(final JSONObject event) {
		if (!event.containsKey(key)) {
			return false;
		}

		List<String> docValues = docValues(event);
		return docValues.stream().filter(valuesList::contains).count() > 0;
	}

	public List<String> docValues(final JSONObject document) {
		final Object object = document.get(key);
		if (object instanceof String) {
			return Arrays.asList((String) object);
		} else if (object instanceof JSONArray) {
			return ((JSONArray) object).toJavaList(String.class);
		}
		return Collections.EMPTY_LIST;
	}
}
