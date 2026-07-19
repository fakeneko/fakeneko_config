package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanConfig extends AbstractConfig<Boolean> {
	public static final ConfigSerializer<Boolean> SERIALIZER = new ConfigSerializer<>() {
		@Override
		@NotNull
		public JsonElement serialize(Boolean value) {
			return new JsonPrimitive(value);
		}

		@Override
		@NotNull
		public Boolean deserialize(@NotNull JsonElement element) {
			return element.getAsBoolean();
		}
	};

	private HotkeyConfig hotkey;

	public BooleanConfig(@NotNull String name, @NotNull ConfigCategory category, boolean defaultValue) {
		super(name, category, defaultValue);
	}

	public BooleanConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, boolean defaultValue) {
		super(name, displayName, category, defaultValue);
	}

	/**
	 * Binds a hotkey to this boolean config. Pressing the hotkey toggles the boolean value.
	 *
	 * @param hotkeyDisplayName The display name shown in the GUI.
	 * @param keybindId The keybind identifier.
	 * @param defaultKeys The default key combination.
	 * @return this
	 */
	public BooleanConfig withHotkey(@NotNull Component hotkeyDisplayName, @NotNull ResourceLocation keybindId, @NotNull InputKeys defaultKeys) {
		this.hotkey = new HotkeyConfig(
			this.name() + "_hotkey",
			hotkeyDisplayName,
			this.category(),
			defaultKeys,
			keybindId,
			keybind -> this.toggle(),
			false
		);
		return this;
	}

	@Nullable
	public HotkeyConfig hotkey() {
		return this.hotkey;
	}

	@Override
	public void reset() {
		super.reset();
		if (this.hotkey != null) {
			this.hotkey.reset();
		}
	}

	public void toggle() {
		this.set(!this.get());
	}
}
