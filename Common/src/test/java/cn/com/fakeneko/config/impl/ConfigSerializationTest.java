package cn.com.fakeneko.config.impl;

import com.mojang.blaze3d.platform.InputConstants;
import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.BooleanConfig;
import cn.com.fakeneko.config.impl.types.HotkeyConfig;
import cn.com.fakeneko.config.impl.types.IntegerConfig;
import cn.com.fakeneko.config.impl.types.StringListConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigSerializationTest {
	@Test
	public void testSerializeDeserialize() throws Exception {
		Path tempDir = Files.createTempDirectory("fakeneko_config-test");
		Path configPath = tempDir.resolve("test.json");
		ConfigManagerImpl manager = new ConfigManagerImpl("test", configPath);
		ConfigCategory category = manager.createCategory("general", Component.literal("General"));

		BooleanConfig enabled = new BooleanConfig("enabled", category, true);
		IntegerConfig maxCount = new IntegerConfig("max_count", category, 5, 1, 20);
		StringListConfig blacklist = new StringListConfig("blacklist", category, List.of("a", "b"));

		manager.load();
		assertTrue(enabled.get(), "Default value should be true");
		assertEquals(5, maxCount.get(), "Default value should be 5");

		enabled.set(false);
		maxCount.set(10);
		blacklist.set(List.of("c", "d"));
		manager.save();

		ConfigManagerImpl manager2 = new ConfigManagerImpl("test", configPath);
		ConfigCategory category2 = manager2.createCategory("general", Component.literal("General"));
		BooleanConfig enabled2 = new BooleanConfig("enabled", category2, true);
		IntegerConfig maxCount2 = new IntegerConfig("max_count", category2, 5, 1, 20);
		StringListConfig blacklist2 = new StringListConfig("blacklist", category2, List.of("a", "b"));
		manager2.load();

		assertFalse(enabled2.get(), "Loaded value should be false");
		assertEquals(10, maxCount2.get(), "Loaded value should be 10");
		assertEquals(List.of("c", "d"), blacklist2.get(), "Loaded list should match");

		Files.deleteIfExists(configPath);
		Files.deleteIfExists(tempDir);
	}

	@Test
	public void testHotkeyComboSerializeDeserialize() throws Exception {
		Path tempDir = Files.createTempDirectory("fakeneko_config-test");
		Path configPath = tempDir.resolve("hotkey.json");
		ConfigManagerImpl manager = new ConfigManagerImpl("hotkey", configPath);
		ConfigCategory category = manager.createCategory("general", Component.literal("General"));

		InputKeys combo = InputKeys.ofKeys(InputConstants.KEY_LCONTROL, InputConstants.KEY_K);
		HotkeyConfig hotkey = new HotkeyConfig("combo", category, combo, new ResourceLocation("test", "combo"));

		manager.load();
		assertEquals(combo, hotkey.get(), "Default combo should be Ctrl+K");

		InputKeys newCombo = InputKeys.ofKeys(InputConstants.KEY_LSHIFT, InputConstants.KEY_M);
		hotkey.set(newCombo);
		manager.save();

		ConfigManagerImpl manager2 = new ConfigManagerImpl("hotkey", configPath);
		ConfigCategory category2 = manager2.createCategory("general", Component.literal("General"));
		HotkeyConfig hotkey2 = new HotkeyConfig("combo", category2, combo, new ResourceLocation("test", "combo"));
		manager2.load();

		assertEquals(newCombo, hotkey2.get(), "Loaded combo should be Shift+M");
		assertTrue(manager2.isModified(), "Manager should be modified after loading changed value");

		Files.deleteIfExists(configPath);
		Files.deleteIfExists(tempDir);
	}

	@Test
	public void testIsModifiedDefaultState() throws Exception {
		Path tempDir = Files.createTempDirectory("fakeneko_config-test");
		Path configPath = tempDir.resolve("default.json");
		ConfigManagerImpl manager = new ConfigManagerImpl("default", configPath);
		ConfigCategory category = manager.createCategory("general", Component.literal("General"));

		new BooleanConfig("enabled", category, true);
		new IntegerConfig("max_count", category, 5, 1, 20);
		new StringListConfig("blacklist", category, List.of("a", "b"));
		new HotkeyConfig("combo", category, InputKeys.ofKeys(InputConstants.KEY_LCONTROL, InputConstants.KEY_K), new ResourceLocation("test", "combo"));

		manager.load();
		assertFalse(manager.isModified(), "Manager should not be modified after loading defaults");

		Files.deleteIfExists(configPath);
		Files.deleteIfExists(tempDir);
	}
}
