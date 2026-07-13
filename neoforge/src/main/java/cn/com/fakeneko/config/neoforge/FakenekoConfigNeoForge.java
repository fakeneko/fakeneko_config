package cn.com.fakeneko.config.neoforge;

import cn.com.fakeneko.config.FakenekoConfigClient;
import cn.com.fakeneko.config.impl.TestConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("fakeneko_config")
public class FakenekoConfigNeoForge {
	public static final Logger LOGGER = LoggerFactory.getLogger("fakeneko_config");

	public FakenekoConfigNeoForge(IEventBus eventBus, ModContainer container) {
		TestConfig.init();
		LOGGER.info("Loaded fakeneko_config (NeoForge)");

		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			container.registerExtensionPoint(IConfigScreenFactory.class, (containerInstance, parent) -> FakenekoConfigClient.createConfigScreen(parent));
		}
	}
}
