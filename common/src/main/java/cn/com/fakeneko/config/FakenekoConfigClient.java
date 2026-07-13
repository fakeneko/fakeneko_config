package cn.com.fakeneko.config;

import cn.com.fakeneko.config.impl.TestConfig;
import cn.com.fakeneko.config.impl.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

/**
 * Client-side helpers shared across loaders.
 */
public class FakenekoConfigClient {
	private FakenekoConfigClient() {

	}

	@NotNull
	public static net.minecraft.client.gui.screens.Screen createConfigScreen(Screen parent) {
		return new ConfigScreen(parent, TestConfig.MANAGER);
	}

	public static void openConfigScreen(@NotNull Screen parent) {
		Minecraft.getInstance().gui.setScreen(createConfigScreen(parent));
	}
}
