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
import com.thorstenmarx.webtools.api.actions.Action;
import com.thorstenmarx.webtools.api.actions.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author marx
 */
public class EventAction implements Conditional, Action {

	private Conditional conditional;
	
	private Set<String> allUsers;
	
	private final MBassador eventBus;
	
	private final String event;
	private long windowStart = System.currentTimeMillis();
	private String site;
	
	public EventAction (final String event, final MBassador eventBus) {
		allUsers = Collections.synchronizedSet(new HashSet());
		this.eventBus = eventBus;
		this.event = event;
	}

	public EventAction site (final String site) {
		this.site = site;
		return this;
	}
	
	public EventAction and (final Conditional...conditionals) {
		this.conditional = new AND(conditionals);
		return this;
	}
	
	public EventAction or (final Conditional...conditionals) {
		this.conditional = new OR(conditionals);
		return this;
	}
	public EventAction not (final Conditional...conditionals) {
		this.conditional = new NOT(conditionals);
		return this;
	}
	
	public EventAction window (long start) {
		windowStart = start;
		return this;
	}
	
	@Override
	public long start () {
		return windowStart;
	}

	@Override
	public void match() {
		conditional.match();
	}
	
	@Override
	public boolean matchs(final String userid) {
		return conditional.matchs(userid);
	}
	
	@Override
	public boolean affected(final JSONObject event) {
		
		if (!event.containsKey("site")) {
			return false;
		}
		final String docSite = event.getString("site");
		if (site != null && site.equals(docSite)) {
			return true;
		}
		
		return conditional.affected(event);
	}

	@Override
	public boolean valid() {
		return conditional.valid();
	}

	@Override
	public void handle(ShardDocument doc) {
		
		final String docSite = doc.document.getString("site");
		
		if (site != null && !site.equals(docSite)) {
			return;
		}
		
		final String userid = doc.document.getString("userid");
		allUsers.add(userid);
		conditional.handle(doc);
	}

	@Override
	public void execute() {
		allUsers.stream().filter(conditional::matchs).forEach(uid -> {
			ActionEvent actionEvent = ActionEvent.builder().setUserid(uid).setEvent(event).build();
			
			eventBus.publishAsync(actionEvent);
		});
	}

	@Override
	public long end() {
		return System.currentTimeMillis();
	}

	public Set<String> getAllUsers() {
		return allUsers;
	}
}
