package cn.com.fakeneko.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import cn.com.fakeneko.config.FakenekoConfigClient;
import net.minecraft.client.gui.screens.Screen;

public class FakenekoConfigModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
		return FakenekoConfigClient::createConfigScreen;
	}
}
