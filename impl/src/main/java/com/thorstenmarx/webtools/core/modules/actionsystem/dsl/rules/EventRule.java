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
import com.thorstenmarx.webtools.api.actions.Conditional;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import com.thorstenmarx.webtools.collection.CounterMapMap;
import com.thorstenmarx.webtools.core.modules.actionsystem.util.CounterInt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author thmarx
 */
public class EventRule implements Conditional {
	
	public static final String RULE = "EVENT";

	private String event;
	private int count;
	private boolean exact = false;

	private final CounterMapMap<String, String> results;
	
	private final Set<String> users;
	
	final CounterInt counter;
	
	public EventRule() {
		results = new CounterMapMap<>();
		users = new HashSet<>();
		this.counter = new CounterInt();
	}

	public String event() {
		return event;
	}
	
	public EventRule exact () {
		this.exact = true;
		
		return this;
	}

	public EventRule event(final String event) {
		this.event = event;
		return this;
	}

	public int count() {
		return count;
	}

	public EventRule count(int count) {
		this.count = count;
		return this;
	}


	@Override
	public String toString() {
		return "EventRule{" + "event=" + event + ", count=" + count + '}';
	}

	public Set<String> users () {
		return users;
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
		if (event().equals(docEvent)) {
			final String userid = doc.document.getString("userid");
			counter.add(userid);
		}
	}

	@Override
	public boolean affected(JSONObject event) {
		final String docEvent = event.getString("event");
		if (event().equals(docEvent)) {
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
