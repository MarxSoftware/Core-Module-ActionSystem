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
public class CampaignRule implements Conditional {

	public static final String RULE = "CAMPAIGN";

	private String campaign;
	private String medium;
	private String source;

	private final Set<String> users;

	private final ConcurrentMap<String, MapKey> userCampaign;

	class MapKey {

		final String campaign;
		final String source;
		final String medium;
		final long timestamp;

		public MapKey(final String campaign, final String source, final String medium, final long timestamp) {
			this.campaign = campaign;
			this.source = source;
			this.medium = medium;
			this.timestamp = timestamp;
		}

	}

	public CampaignRule() {
		users = new HashSet<>();
		userCampaign = new ConcurrentHashMap<>();
	}

	public String getCampaign() {
		return campaign;
	}

	public CampaignRule campaign(String campaign) {
		this.campaign = campaign;
		return this;
	}

	public String medium() {
		return medium;
	}

	public CampaignRule medium(String medium) {
		this.medium = medium;
		return this;
	}

	public String source() {
		return source;
	}

	public CampaignRule source(String source) {
		this.source = source;
		return this;
	}

	@Override
	public boolean matchs(final String userid) {
		return users.contains(userid);
	}

	@Override
	public void match() {
		userCampaign.keySet().forEach((userid) -> {
			users.add(userid);
		});
	}

	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public void handle(final ShardDocument doc) {
		if (!doc.document.containsKey(Fields.Utm.combine("campaign"))) {
			return;
		}
		final String doc_campaign = doc.document.getString(Fields.Utm.combine("campaign"));
		final String doc_medium = doc.document.getString(Fields.Utm.combine("medium"));
		final String doc_source = doc.document.getString(Fields.Utm.combine("source"));

		if (doc_campaign.equalsIgnoreCase(campaign) && 
				matchs(Fields.Utm.combine("source"), source, doc.document) && 
				matchs(Fields.Utm.combine("medium"), medium, doc.document)) {
			final String userid = doc.document.getString("userid");
			final long timestamp = doc.document.getLongValue(Fields._TimeStamp.value());

			if (userCampaign.containsKey(userid)) {
				MapKey key = userCampaign.get(userid);
				if (timestamp > key.timestamp) {
					userCampaign.put(userid, new MapKey(doc_campaign, doc_source, doc_medium, timestamp));
				}
			} else {
				userCampaign.put(userid, new MapKey(doc_campaign, doc_source, doc_medium, timestamp));
			}

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
		if (!document.containsKey(Fields.Utm.combine("campaign"))) {
			return false;
		}

		return true;
	}
}
