package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DoubleConfig extends AbstractConfig<Double> {
	public static final ConfigSerializer<Double> SERIALIZER = new ConfigSerializer<>() {
		@Override
		@NotNull
		public JsonElement serialize(Double value) {
			return new JsonPrimitive(value);
		}

		@Override
		@NotNull
		public Double deserialize(@NotNull JsonElement element) {
			return element.getAsDouble();
		}
	};

	private final double min;
	private final double max;

	public DoubleConfig(@NotNull String name, @NotNull ConfigCategory category, double defaultValue, double min, double max) {
		this(name, Component.literal(name), category, defaultValue, min, max);
	}

	public DoubleConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, double defaultValue, double min, double max) {
		super(name, displayName, category, defaultValue);
		this.min = min;
		this.max = max;
	}

	public double min() {
		return this.min;
	}

	public double max() {
		return this.max;
	}

	@Override
	public void set(Double value) {
		if (value == null) {
			throw new IllegalArgumentException("Config value cannot be null: " + this.name());
		}
		double clamped = Math.max(this.min, Math.min(this.max, value));
		super.set(clamped);
	}
}
