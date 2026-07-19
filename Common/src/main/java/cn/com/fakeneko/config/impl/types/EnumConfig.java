package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class EnumConfig<E extends Enum<E>> extends AbstractConfig<E> {
	private final Class<E> enumClass;
	private final EnumWidget widget;
	private final E[] values;
	private final java.util.function.Function<E, Component> formatter;

	public EnumConfig(@NotNull String name, @NotNull ConfigCategory category, @NotNull E defaultValue) {
		this(name, Component.literal(name), category, defaultValue, EnumWidget.CYCLIC, e -> Component.literal(e.name()));
	}

	public EnumConfig(@NotNull String name, @NotNull ConfigCategory category, @NotNull E defaultValue, @NotNull EnumWidget widget) {
		this(name, Component.literal(name), category, defaultValue, widget, e -> Component.literal(e.name()));
	}

	public EnumConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull E defaultValue) {
		this(name, displayName, category, defaultValue, EnumWidget.CYCLIC, e -> Component.literal(e.name()));
	}

	public EnumConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull E defaultValue, @NotNull EnumWidget widget) {
		this(name, displayName, category, defaultValue, widget, e -> Component.literal(e.name()));
	}

	public EnumConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull E defaultValue, @NotNull EnumWidget widget, @NotNull java.util.function.Function<E, Component> formatter) {
		super(name, displayName, category, defaultValue);
		this.enumClass = defaultValue.getDeclaringClass();
		this.values = this.enumClass.getEnumConstants();
		if (this.values.length < 2 || this.values.length > 8) {
			throw new IllegalArgumentException("Enum config '" + name + "' must have between 2 and 8 values, found " + this.values.length);
		}
		this.widget = widget;
		this.formatter = formatter;
	}

	public ConfigSerializer<E> serializer() {
		return new ConfigSerializer<>() {
			@Override
			@NotNull
			public JsonElement serialize(E value) {
				return new JsonPrimitive(value.name());
			}

			@Override
			@NotNull
			public E deserialize(@NotNull JsonElement element) {
				return Enum.valueOf(EnumConfig.this.enumClass, element.getAsString());
			}
		};
	}

	@NotNull
	public Class<E> enumClass() {
		return this.enumClass;
	}

	@NotNull
	public E[] values() {
		return this.values;
	}

	@NotNull
	public EnumWidget widget() {
		return this.widget;
	}

	@NotNull
	public Component displayValue(@NotNull E value) {
		return this.formatter.apply(value);
	}
}
