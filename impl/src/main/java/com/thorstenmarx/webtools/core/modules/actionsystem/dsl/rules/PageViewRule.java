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

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.api.analytics.Events;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import com.thorstenmarx.webtools.collection.CounterMapMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author marx
 */
public class PageViewRule implements Conditional {

	public static final String RULE = "PAGEVIEW";
	
	private static final String MATCH_ALL_PAGES = "##match_all_pages##";

	private String page = MATCH_ALL_PAGES; // default page
	private int count = 0; // default is 0

	private final CounterMapMap<String, String> results;

	private final Set<String> users;

	public PageViewRule() {
		results = new CounterMapMap<>();
		users = new HashSet<>();
	}

	public String page() {
		return page;
	}

	public PageViewRule page(String page) {
		this.page = page;
		return this;
	}

	public int count() {
		return count;
	}

	public PageViewRule count(int count) {
		this.count = count;
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
			Map<String, Integer> values = entry.getValue();

			final String key = page;
			if (values.containsKey(key) && values.get(key) >= count) {
				// Anzahl der nötigen PageViews ist erreicht
				users.add(userid);
			}
		});
	}

	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public void handle(final ShardDocument doc) {
		final String docPage = doc.document.getString(Fields.Page.value());
		final String event = doc.document.getString(Fields.Event.value());
		
		if ((!Strings.isNullOrEmpty(event) && Events.PageView.value().equals(event))) {
			if (MATCH_ALL_PAGES.equals(page) || page.equals(docPage)) {
				final String userid = doc.document.getString("userid");

				results.add(userid, page, 1);
			}	
		}
	}

	@Override
	public boolean affected(JSONObject document) {
		final String docPage = document.getString(Fields.Page.value());
		final String event = document.getString(Fields.Event.value());
		
		if ((!Strings.isNullOrEmpty(event) && Events.PageView.value().equals(event))) {
			if (MATCH_ALL_PAGES.equals(page) || page.equals(docPage)) {
				return true;
			}	
		}
		
		return false;
	}
}
