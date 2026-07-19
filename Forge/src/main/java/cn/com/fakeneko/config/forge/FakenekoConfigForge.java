package cn.com.fakeneko.config.forge;

import cn.com.fakeneko.config.FakenekoConfigClient;
import cn.com.fakeneko.config.impl.TestConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("fakeneko_config")
public class FakenekoConfigForge {
	public static final Logger LOGGER = LoggerFactory.getLogger("fakeneko_config");

	public FakenekoConfigForge() {
		TestConfig.init();
		LOGGER.info("Loaded fakeneko_config (Forge)");

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
			ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
				new ConfigGuiHandler.ConfigGuiFactory((minecraft, parent) -> FakenekoConfigClient.createConfigScreen(parent))
			)
		);
	}
}
