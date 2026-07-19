package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class StringConfig extends AbstractConfig<String> {
	public static final ConfigSerializer<String> SERIALIZER = new ConfigSerializer<>() {
		@Override
		@NotNull
		public JsonElement serialize(String value) {
			return new JsonPrimitive(value);
		}

		@Override
		@NotNull
		public String deserialize(@NotNull JsonElement element) {
			return element.getAsString();
		}
	};

	public StringConfig(@NotNull String name, @NotNull ConfigCategory category, @NotNull String defaultValue) {
		super(name, category, defaultValue);
	}

	public StringConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull String defaultValue) {
		super(name, displayName, category, defaultValue);
	}
}
