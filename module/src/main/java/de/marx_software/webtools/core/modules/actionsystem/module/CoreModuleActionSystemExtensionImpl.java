package de.marx_software.webtools.core.modules.actionsystem.module;

import com.thorstenmarx.modules.api.ModuleManager;
import com.thorstenmarx.modules.api.annotation.Extension;
import de.marx_software.webtools.api.actions.SegmentService;
import de.marx_software.webtools.api.analytics.AnalyticsDB;
import de.marx_software.webtools.api.cache.CacheLayer;
import de.marx_software.webtools.api.entities.Entities;
import de.marx_software.webtools.api.execution.Executor;
import de.marx_software.webtools.api.extensions.core.CoreSegmentationExtension;
import de.marx_software.webtools.core.modules.actionsystem.UserSegmentGenerator;
import de.marx_software.webtools.core.modules.actionsystem.dsl.JsonDsl;
import de.marx_software.webtools.core.modules.actionsystem.segmentStore.LocalUserSegmentStore;
import de.marx_software.webtools.core.modules.actionsystem.segmentation.EntitiesSegmentService;
import javax.inject.Inject;
import net.engio.mbassy.bus.MBassador;

/**
 *
 * @author marx
 */
@Extension(CoreSegmentationExtension.class)
public class CoreModuleActionSystemExtensionImpl extends CoreSegmentationExtension {


	
	@Override
	public String getName() {
		return "CoreModule ActionSystem";
	}


	@Override
	public void init() {
	}

	@Override
	public SegmentService getSegmentService() {
		return CoreModuleActionSystemModuleLifeCycle.segmentService;
	}

}
