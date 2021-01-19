package de.marx_software.webtools.core.modules.actionsystem.dsl.rules;

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
import de.marx_software.webtools.api.actions.Conditional;
import de.marx_software.webtools.api.analytics.Events;
import de.marx_software.webtools.api.analytics.Fields;
import de.marx_software.webtools.api.analytics.query.ShardDocument;
import de.marx_software.webtools.core.modules.actionsystem.util.CounterInt;

/**
 *
 * @author marx
 */
public class PageViewRule implements Conditional {

	public static final String RULE = "PAGEVIEW";
	
	private static final String MATCH_ALL_PAGES = "##match_all_pages##";
	private static final String MATCH_ALL_TYPE = "##match_all_type##";

	private String page = MATCH_ALL_PAGES; // default page
	private String type = MATCH_ALL_TYPE;
	private int count = 0; // default is 0
	private boolean exact = false;

	private final CounterInt counter;

	public PageViewRule() {
		counter = new CounterInt();
	}

	public String page() {
		return page;
	}
	
	public String type () {
		return type;
	}
	
	public PageViewRule exact () {
		exact = true;
		return this;
	}

	public PageViewRule page(String page) {
		this.page = page;
		return this;
	}
	public PageViewRule type(String type) {
		this.type = type;
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
		if (exact) {
			return counter.get(userid) == count;
		} else {
			return counter.get(userid) >= count;
		}
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
		final String docPage = doc.document.getString(Fields.Page.value());
		final String docType = doc.document.getString(Fields.Type.value());
		final String event = doc.document.getString(Fields.Event.value());
		
		if ((!Strings.isNullOrEmpty(event) && Events.PageView.value().equals(event))) {
			if ((MATCH_ALL_PAGES.equals(page) || page.equals(docPage)) 
					&& (MATCH_ALL_TYPE.equals(type) || type.equals(docType))) {
				final String userid = doc.document.getString("userid");

				counter.add(userid);
			}	
		}
	}

	@Override
	public boolean affected(JSONObject document) {
		final String docPage = document.getString(Fields.Page.value());
		final String docType = document.getString(Fields.Type.value());
		final String event = document.getString(Fields.Event.value());
		
		if ((!Strings.isNullOrEmpty(event) && Events.PageView.value().equals(event))) {
			if ((MATCH_ALL_PAGES.equals(page) || page.equals(docPage)) 
					&& (MATCH_ALL_TYPE.equals(type) || type.equals(docType))) {
				return true;
			}	
		}
		
		return false;
	}
}
