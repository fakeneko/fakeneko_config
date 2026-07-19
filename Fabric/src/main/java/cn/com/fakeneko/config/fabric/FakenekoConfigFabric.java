package cn.com.fakeneko.config.fabric;

import cn.com.fakeneko.config.impl.TestConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakenekoConfigFabric implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fakeneko_config");

	@Override
	public void onInitialize() {
		TestConfig.init();
		LOGGER.info("Loaded fakeneko_config (Fabric)");
	}
}
