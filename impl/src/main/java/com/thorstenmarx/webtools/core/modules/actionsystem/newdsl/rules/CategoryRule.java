package com.thorstenmarx.webtools.core.modules.actionsystem.newdsl.rules;

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
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CategoryRule that works for category paths like /cat1/cat2/cat2
 * 
 * @author marx
 */
public class CategoryRule implements Conditional {

	public static final String RULE = "CATEGORY";

	private String field;
	private String path;
	private int count;
	private boolean exact = false;

	private final Set<String> users;

	class MapValue {

		final String source;
		final String medium;

		public MapValue(final String source, final String medium) {
			this.source = source;
			this.medium = medium;
		}

	}

	public CategoryRule() {
		users = new HashSet<>();
	}

	public String path() {
		return path;
	}
	
	public CategoryRule exact () {
		exact = true;
		return this;
	}

	public CategoryRule path(String path) {
		this.path = path;
		return this;
	}
	public String field() {
		return field;
	}

	public CategoryRule field(final String field) {
		this.field = field;
		return this;
	}

	public int count() {
		return count;
	}

	public CategoryRule count(final int count) {
		this.count = count;
		return this;
	}

	@Override
	public boolean matchs(final String userid) {
		return users.contains(userid);
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
		if (!doc.document.containsKey(field))  {
			return;
		}
		
		List<String> categories = getCategories(doc.document);
		
		if (categories.contains(path)) {
			final String userid = doc.document.getString(Fields.UserId.value());

			users.add(userid);
		}
	}
	
	public List<String> getCategories(final JSONObject document) {
		final Object object = document.get(field);
		if (object instanceof String) {
			return Arrays.asList((String) object);
		} else if (object instanceof JSONArray) {
			return ((JSONArray) object).toJavaList(String.class);
		} else if (object instanceof List) {
			return (List<String>)object;
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean affected(JSONObject document) {
		return document.containsKey(field);
	}
}
