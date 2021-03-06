/**
 * webTools-contentengine
 * Copyright (C) 2016  Thorsten Marx (kontakt@thorstenmarx.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.marx_software.webtools.core.modules.actionsystem.module;

import com.thorstenmarx.modules.api.ModuleLifeCycleExtension;
import com.thorstenmarx.modules.api.annotation.Extension;
import de.marx_software.webtools.api.analytics.AnalyticsDB;
import de.marx_software.webtools.api.entities.Entities;
import de.marx_software.webtools.core.modules.actionsystem.UserSegmentGenerator;
import de.marx_software.webtools.core.modules.actionsystem.dsl.JsonDsl;
import de.marx_software.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import javax.inject.Inject;

/**
 *
 * @author marx
 */
@Extension(ModuleLifeCycleExtension.class)
public class CoreModuleActionSystemModuleLifeCycle extends ModuleLifeCycleExtension {

//	@Inject
	private AnalyticsDB analyticsDb;
//	@Inject
	private Entities entities;

	protected static UserSegmentGenerator userSegmentGenerator;

	protected static EntitiesSegmentService segmentService;

	@Override
	public void activate() {
		if (segmentService == null) {
			entities = getContext().serviceRegistry().single(Entities.class).get();
			analyticsDb = getContext().serviceRegistry().single(AnalyticsDB.class).get();
			segmentService = new EntitiesSegmentService(entities);
			userSegmentGenerator = new UserSegmentGenerator(analyticsDb, new JsonDsl(getContext().serviceRegistry()), segmentService, getContext().serviceRegistry());
		}
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void init() {

	}

}
