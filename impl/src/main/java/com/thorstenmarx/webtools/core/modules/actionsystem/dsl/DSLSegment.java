package com.thorstenmarx.webtools.core.modules.actionsystem.dsl;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public class DSLSegment implements Conditional {

	private static final Logger log = LoggerFactory.getLogger(DSLSegment.class);
	
	private Conditional conditional;
	
	private Set<String> allUsers;
	
	public String site;
	
	public DSLSegment () {
		allUsers = Collections.synchronizedSet(new HashSet());
	}

	public DSLSegment site (final String site) {
		this.site = site;
		return this;
	}
	
	public DSLSegment and (final Conditional...conditionals) {
		this.conditional = new AND(conditionals);
		return this;
	}
	
	public DSLSegment or (final Conditional...conditionals) {
		this.conditional = new OR(conditionals);
		return this;
	}
	public DSLSegment not (final Conditional...conditionals) {
		this.conditional = new NOT(conditionals);
		return this;
	}

	@Override
	public void match() {
		if (conditional != null) {
			conditional.match();
		}
	}
	
	@Override
	public boolean matchs(final String userid) {
		final boolean matchs = conditional.matchs(userid);
		return matchs;
	}

	@Override
	public boolean valid() {
		return conditional.valid();
	}

	@Override
	public void handle(ShardDocument doc) {
		final String userid = doc.document.getString("userid");
		final String docSite = doc.document.getString("site");
		
		if (site != null && !site.equals(docSite)) {
			return;
		}
		
		
		allUsers.add(userid);
		conditional.handle(doc);
	}
	
	@Override
	public boolean affected (final JSONObject event) {
		
		if (!event.containsKey("site")) {
			return false;
		}
		final String docSite = event.getString("site");
		if (site != null && site.equals(docSite)) {
			return true;
		}
		
		return conditional.affected(event);
	}

	public Set<String> getAllUsers() {
		return allUsers;
	}
}
