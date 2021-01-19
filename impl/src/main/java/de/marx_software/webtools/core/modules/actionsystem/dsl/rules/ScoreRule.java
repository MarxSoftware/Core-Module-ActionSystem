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
import de.marx_software.webtools.api.actions.Conditional;
import de.marx_software.webtools.api.analytics.query.ShardDocument;
import de.marx_software.webtools.collection.CounterMapMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thmarx
 */
public class ScoreRule implements Conditional {

	public static final String RULE = "SCORE";

	private String name;

	private int score;

	private final CounterMapMap<String, String> results;

	private final Set<String> users;

	public ScoreRule() {
		results = new CounterMapMap<>();
		users = new HashSet<>();
	}

	public String name() {
		return name;
	}

	public ScoreRule name(String name) {
		this.name = name;
		return this;
	}

	public int score() {
		return score;
	}

	public ScoreRule score(int score) {
		this.score = score;
		return this;
	}

	@Override
	public String toString() {
		return "ScoreRule{" + "name=" + name + ", score=" + score + '}';
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

			if (values.containsKey(name) && values.get(name) >= score) {
				// Entsprechender Score wurde erreicht
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
		if (doc.document.containsKey("score")) {
			String[] scores = doc.document.getJSONArray("score").toArray(new String[1]);
			final String userid = doc.document.getString("userid");
			for (String docScore : scores) {
				if (docScore.startsWith(name() + ":")) {
					String[] values = docScore.split(":");
					results.add(userid, values[0], Integer.parseInt(values[1]));
				}
			}
		}
	}

	@Override
	public boolean affected(final JSONObject document) {
		if (document.containsKey("score")) {
			String[] scores = document.getJSONArray("score").toArray(new String[1]);
			final String userid = document.getString("userid");
			for (String docScore : scores) {
				if (docScore.startsWith(name() + ":")) {
					return true;
				}
			}
		}
		return false;
	}
}
