package cn.com.fakeneko.config.fabric;

import cn.com.fakeneko.config.impl.TestConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakenekoConfigFabricClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("fakeneko_config-client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Loaded fakeneko_config client (Fabric)");
	}
}
