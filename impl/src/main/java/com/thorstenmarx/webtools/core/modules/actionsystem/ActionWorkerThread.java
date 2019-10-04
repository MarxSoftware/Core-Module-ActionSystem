package com.thorstenmarx.webtools.core.modules.actionsystem;

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
import com.thorstenmarx.modules.api.ModuleManager;
import com.thorstenmarx.webtools.api.actions.Action;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.EventAction;
import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.EventActionQuery;
import java.util.List;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der Thread läuft die ganze Zeit über die Daten und erzeugt die User
 * Segmentierung
 *
 * @author thmarx
 */
public class ActionWorkerThread extends Thread {

	private static final Logger log = LoggerFactory.getLogger(ActionWorkerThread.class);
	
	public static final String CONSUMER_NAME = "actionWorker";

	private String workerName;

	private final AnalyticsDB db;
	private final ActionSystemImpl actionSystem;
	private final ModuleManager moduleManager;

	private boolean shutdown = false;

	public ActionWorkerThread(int index, final AnalyticsDB db, final ActionSystemImpl actionSystem, final ModuleManager moduleManager) {
		this.db = db;
		this.actionSystem = actionSystem;
		this.moduleManager = moduleManager;
		setDaemon(true);
		this.workerName = CONSUMER_NAME + "_" + index;
	}

	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				final List<Action> segments = actionSystem.actions();

				segments.stream().forEach(action -> {
					EventActionQuery query;
					try {
						query = new EventActionQuery((EventAction) action, moduleManager);
						db.queryAsync(query);
						action.execute();
					} catch (ScriptException ex) {
						log.error("error create segement query " + action, ex);

					}
				});
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
