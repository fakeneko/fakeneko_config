package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonElement;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.keybind.Keybind;
import cn.com.fakeneko.config.impl.keybind.KeybindListener;
import cn.com.fakeneko.config.impl.keybind.KeybindManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class HotkeyConfig extends AbstractConfig<InputKeys> {
	public static final ConfigSerializer<InputKeys> SERIALIZER = new ConfigSerializer<>() {
		@Override
		@NotNull
		public JsonElement serialize(InputKeys value) {
			return InputKeys.Serializer.INSTANCE.serialize(value, null, null);
		}

		@Override
		@NotNull
		public InputKeys deserialize(@NotNull JsonElement element) {
			return InputKeys.Serializer.INSTANCE.deserialize(element, null, null);
		}
	};

	private final ResourceLocation keybindId;
	private final Consumer<Keybind> onPress;
	private final boolean visible;
	private Keybind keybind;

	public HotkeyConfig(@NotNull String name, @NotNull ConfigCategory category, @NotNull InputKeys defaultValue, @NotNull ResourceLocation keybindId) {
		this(name, Component.literal(name), category, defaultValue, keybindId, null, true);
	}

	public HotkeyConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull InputKeys defaultValue, @NotNull ResourceLocation keybindId) {
		this(name, displayName, category, defaultValue, keybindId, null, true);
	}

	public HotkeyConfig(@NotNull String name, @NotNull ConfigCategory category, @NotNull InputKeys defaultValue, @NotNull ResourceLocation keybindId, Consumer<Keybind> onPress) {
		this(name, Component.literal(name), category, defaultValue, keybindId, onPress, true);
	}

	public HotkeyConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull InputKeys defaultValue, @NotNull ResourceLocation keybindId, Consumer<Keybind> onPress) {
		this(name, displayName, category, defaultValue, keybindId, onPress, true);
	}

	HotkeyConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull InputKeys defaultValue, @NotNull ResourceLocation keybindId, Consumer<Keybind> onPress, boolean visible) {
		super(name, displayName, category, defaultValue);
		this.keybindId = keybindId;
		this.onPress = onPress;
		this.visible = visible;
		this.registerKeybind();
		this.addListener((config, from, to) -> this.keybind.setKeys(to));
	}

	private void registerKeybind() {
		this.keybind = KeybindManager.register(this.keybindId, this.get());
		if (this.onPress != null) {
			this.keybind.addListener(KeybindListener.onPress(() -> this.onPress.accept(this.keybind)));
		}
	}

	@NotNull
	public ResourceLocation keybindId() {
		return this.keybindId;
	}

	@NotNull
	public Keybind keybind() {
		return this.keybind;
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public void reset() {
		super.reset();
		this.keybind.resetKeysToDefault();
	}

	@Override
	public void set(InputKeys value) {
		if (value == null) {
			value = InputKeys.EMPTY;
		}
		super.set(value);
	}
}
