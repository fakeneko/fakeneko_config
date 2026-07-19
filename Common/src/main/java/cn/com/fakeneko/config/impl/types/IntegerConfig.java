package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class IntegerConfig extends AbstractConfig<Integer> {
	public static final ConfigSerializer<Integer> SERIALIZER = new ConfigSerializer<>() {
		@Override
		@NotNull
		public JsonElement serialize(Integer value) {
			return new JsonPrimitive(value);
		}

		@Override
		@NotNull
		public Integer deserialize(@NotNull JsonElement element) {
			return element.getAsInt();
		}
	};

	private final int min;
	private final int max;

	public IntegerConfig(@NotNull String name, @NotNull ConfigCategory category, int defaultValue, int min, int max) {
		this(name, Component.literal(name), category, defaultValue, min, max);
	}

	public IntegerConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, int defaultValue, int min, int max) {
		super(name, displayName, category, defaultValue);
		this.min = min;
		this.max = max;
	}

	public int min() {
		return this.min;
	}

	public int max() {
		return this.max;
	}

	@Override
	public void set(Integer value) {
		if (value == null) {
			throw new IllegalArgumentException("Config value cannot be null: " + this.name());
		}
		int clamped = Math.max(this.min, Math.min(this.max, value));
		super.set(clamped);
	}
}
