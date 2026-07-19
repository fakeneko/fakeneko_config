package cn.com.fakeneko.config.impl;

import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.BooleanConfig;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AnalysisVerificationTest {
	@TempDir
	Path tempDir;

	@Test
	public void emptyInputKeysRoundTrip() {
		String serialized = InputKeys.EMPTY.toString();
		InputKeys back = InputKeys.fromString(serialized);
		assertEquals(InputKeys.EMPTY, back,
			"EMPTY serialized to '" + serialized + "' but read back as '" + back + "' (size=" + back.size() + ")");
	}

	@Test
	public void malformedRootJsonShouldNotThrow() {
		Path configPath = this.tempDir.resolve("bad_root.json");
		assertDoesNotThrow(() -> Files.writeString(configPath, "[1,2,3]"));
		ConfigManagerImpl manager = new ConfigManagerImpl("bad_root", configPath);
		assertDoesNotThrow(manager::load, "load() threw on non-object root JSON");
	}

	@Test
	public void nonObjectCategoryShouldNotThrow() {
		Path configPath = this.tempDir.resolve("bad_category.json");
		assertDoesNotThrow(() -> Files.writeString(configPath, "{\"general\": 5}"));
		ConfigManagerImpl manager = new ConfigManagerImpl("bad_category", configPath);
		ConfigCategory category = manager.createCategory("general", Component.literal("General"));
		new BooleanConfig("enabled", category, true);
		assertDoesNotThrow(manager::load, "load() threw when category value is not an object");
	}

	@Test
	public void clearedHotkeyStaysEmptyAfterReload() throws Exception {
		Path configPath = this.tempDir.resolve("hotkey_clear.json");
		ConfigManagerImpl manager = new ConfigManagerImpl("hotkey_clear", configPath);
		ConfigCategory category = manager.createCategory("general", Component.literal("General"));
		cn.com.fakeneko.config.impl.types.HotkeyConfig hotkey = new cn.com.fakeneko.config.impl.types.HotkeyConfig(
			"combo", category,
			InputKeys.ofKeys(com.mojang.blaze3d.platform.InputConstants.KEY_LCONTROL, com.mojang.blaze3d.platform.InputConstants.KEY_K),
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("test", "clear_combo"));

		hotkey.set(InputKeys.EMPTY);
		manager.save();
		String saved = Files.readString(configPath);

		ConfigManagerImpl manager2 = new ConfigManagerImpl("hotkey_clear", configPath);
		ConfigCategory category2 = manager2.createCategory("general", Component.literal("General"));
		cn.com.fakeneko.config.impl.types.HotkeyConfig hotkey2 = new cn.com.fakeneko.config.impl.types.HotkeyConfig(
			"combo", category2,
			InputKeys.ofKeys(com.mojang.blaze3d.platform.InputConstants.KEY_LCONTROL, com.mojang.blaze3d.platform.InputConstants.KEY_K),
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("test", "clear_combo"));
		manager2.load();

		assertEquals(InputKeys.EMPTY, hotkey2.get(),
			"Cleared hotkey became phantom key after reload. Saved file: " + saved);
		assertTrue(hotkey2.isModified(), "Cleared hotkey should differ from the non-empty default");
	}
}
