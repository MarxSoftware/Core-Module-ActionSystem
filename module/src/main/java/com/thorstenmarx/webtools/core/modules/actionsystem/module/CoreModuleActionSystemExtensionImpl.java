package com.thorstenmarx.webtools.core.modules.actionsystem.module;

import com.thorstenmarx.modules.api.ModuleManager;
import com.thorstenmarx.modules.api.annotation.Extension;
import com.thorstenmarx.webtools.api.actions.ActionSystem;
import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.analytics.AnalyticsDB;
import com.thorstenmarx.webtools.api.datalayer.DataLayer;
import com.thorstenmarx.webtools.api.entities.Entities;
import com.thorstenmarx.webtools.api.execution.Executor;
import com.thorstenmarx.webtools.api.extensions.core.CoreActionSystemExtension;
import com.thorstenmarx.webtools.core.modules.actionsystem.ActionSystemImpl;
import static com.thorstenmarx.webtools.core.modules.actionsystem.module.CoreModuleActionSystemModuleLifeCycle.actionSystem;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import javax.inject.Inject;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author marx
 */
@Extension(CoreActionSystemExtension.class)
public class CoreModuleActionSystemExtensionImpl extends CoreActionSystemExtension {

	@Inject
	private AnalyticsDB analyticsDb;
	@Inject
	private Entities entities;
	@Inject
	private ModuleManager moduleManager;
	@Inject
	private MBassador mBassador;
	@Inject
	private DataLayer datalayer;
	@Inject
	private Executor executor;
	
	
		
	@Override
	public String getName() {
		return "CoreModule ActionSystem";
	}

	@Override
	public ActionSystem getActionSystem() {
		if (CoreModuleActionSystemModuleLifeCycle.actionSystem == null) {
			actionSystem = new ActionSystemImpl(analyticsDb, getSegmentService(), moduleManager, mBassador, datalayer, executor);
			actionSystem.start();
		}
		return CoreModuleActionSystemModuleLifeCycle.actionSystem;
	}

	@Override
	public void init() {
	}

	@Override
	public SegmentService getSegmentService() {
		if (CoreModuleActionSystemModuleLifeCycle.segmentService == null) {
			CoreModuleActionSystemModuleLifeCycle.segmentService = new EntitiesSegmentService(entities);
		}
		return CoreModuleActionSystemModuleLifeCycle.segmentService;
	}

}
