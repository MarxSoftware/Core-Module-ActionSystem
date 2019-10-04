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
package com.thorstenmarx.webtools.core.modules.actionsystem.module;

import com.thorstenmarx.modules.api.ModuleLifeCycleExtension;
import com.thorstenmarx.modules.api.annotation.Extension;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;

/**
 *
 * @author marx
 */
@Extension(ModuleLifeCycleExtension.class)
public class CoreModuleActionSystemModuleLifeCycle extends ModuleLifeCycleExtension {

	protected static ActionSystemImpl actionSystem;
	protected static EntitiesSegmentService segmentService; 
	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
		if (actionSystem != null) {
			actionSystem.close();
		}
	}

	@Override
	public void init() {

	}

}
