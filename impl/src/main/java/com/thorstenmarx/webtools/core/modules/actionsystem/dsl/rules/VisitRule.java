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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author marx
 */
public class VisitRule implements Conditional {

	public static final String RULE = "VISIT";
	
	private int count = 0; // default is 0

	private final Set<String> users;
	
	private final Multimap<String, String> user_visits;

	public VisitRule() {
		users = new HashSet<>();
		user_visits = MultimapBuilder.hashKeys().hashSetValues().build();
	}

	

	public int count() {
		return count;
	}

	public VisitRule count(int count) {
		this.count = count;
		return this;
	}

	@Override
	public boolean matchs(final String userid) {
		return users.contains(userid);
	}

	@Override
	public void match() {
		user_visits.keySet().forEach((user) -> {
			if (user_visits.get(user).size() >= count) {
				users.add(user);
			}
		});
	}

	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public void handle(final ShardDocument doc) {
		final String user = doc.document.getString(Fields.UserId.value());
		final String visit = doc.document.getString(Fields.VisitId.value());
		
		System.out.println(user + " - " + visit);
		if (!user_visits.containsEntry(user, visit)) {
			user_visits.put(user, visit);
		}
	}

	@Override
	public boolean affected(JSONObject document) {
		return true;
	}
}
