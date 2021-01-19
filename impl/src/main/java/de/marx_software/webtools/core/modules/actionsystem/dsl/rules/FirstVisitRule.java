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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import de.marx_software.webtools.api.actions.Conditional;
import de.marx_software.webtools.api.analytics.Fields;
import de.marx_software.webtools.api.analytics.query.ShardDocument;
import de.marx_software.webtools.core.modules.actionsystem.util.CounterInt;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author marx
 */
public class FirstVisitRule implements Conditional {

	public static final String RULE = "FIRSTVISIT";

	private final Set<String> users;

	private final SetMultimap<String, String> userVisits;
	private final Set<String> user_visits;
	
	private final CounterInt counter;

	public FirstVisitRule() {
		users = new HashSet<>();
		userVisits = HashMultimap.create();
		counter = new CounterInt();
		user_visits = new HashSet<>();
	}

	@Override
	public boolean matchs(final String userid) {
//		return users.contains(userid) && !users.contains(this);
		return counter.get(userid) <= 1;
	}

	@Override
	public void match() {
//		userVisits.keySet().forEach((userid) -> {
//			Set<String> visits = userVisits.get(userid);
//			if (visits.size() == 1) {
//				users.add(userid);
//			}
//		});
	}

	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public void handle(final ShardDocument doc) {
		if (!doc.document.containsKey(Fields.Site.value())) {
			return;
		}
		final String visitid = doc.document.getString(Fields.VisitId.value());

		final String userid = doc.document.getString(Fields.UserId.value());
		
		final String key = String.format("%s_%s", userid, visitid);
		if (!user_visits.contains(key)) {
			user_visits.add(key);
			counter.add(userid);
		}

//		userVisits.put(userid, visitid);
	}

	@Override
	public boolean affected(JSONObject document) {
		return true;
	}
}
