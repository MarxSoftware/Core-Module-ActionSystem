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
import com.thorstenmarx.webtools.api.analytics.Fields;
import com.thorstenmarx.webtools.api.analytics.query.ShardDocument;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author marx
 */
public class ReferrerRule implements Conditional {

	public static final String RULE = "REFERRER";

	private String medium;
	private String source;

	private final Set<String> users;

	class MapValue {

		final String source;
		final String medium;

		public MapValue(final String source, final String medium) {
			this.source = source;
			this.medium = medium;
		}

	}

	public ReferrerRule() {
		users = new HashSet<>();
	}

	public String medium() {
		return medium;
	}

	public ReferrerRule medium(String medium) {
		this.medium = medium;
		return this;
	}

	public String source() {
		return source;
	}

	public ReferrerRule source(String source) {
		this.source = source;
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
		if (!(doc.document.containsKey(Fields.Referrer.combine("source"))
				|| doc.document.containsKey(Fields.Referrer.combine("medium"))))  {
			return;
		}
		if (matchs(Fields.Referrer.combine("source"), source, doc.document)
				&& matchs(Fields.Referrer.combine("medium"), medium, doc.document)) {
			final String userid = doc.document.getString(Fields.UserId.value());

			users.add(userid);
		}
	}

	private boolean matchs(final String field, final String value, final JSONObject doc) {
		if (value == null) {
			return true;
		}
		final String docValue = doc.getString(field);
		return value.equalsIgnoreCase(docValue);
	}

	@Override
	public boolean affected(JSONObject document) {
		return document.containsKey(Fields.Referrer.combine("source")) || document.containsKey(Fields.Referrer.combine("medium"));
	}
}
