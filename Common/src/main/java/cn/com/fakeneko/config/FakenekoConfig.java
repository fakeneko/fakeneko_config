package cn.com.fakeneko.config;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FakenekoConfig {
	public static final String MOD_ID = "fakeneko_config";

	private FakenekoConfig() {

	}

	@NotNull
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
