package cn.com.fakeneko.config.impl.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.AbstractConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringListConfig extends AbstractConfig<List<String>> {
	public static final ConfigSerializer<List<String>> SERIALIZER = new ConfigSerializer<>() {
		@Override
		@NotNull
		public JsonElement serialize(List<String> value) {
			JsonArray array = new JsonArray();
			for (String s : value) {
				array.add(new JsonPrimitive(s));
			}
			return array;
		}

		@Override
		@NotNull
		public List<String> deserialize(@NotNull JsonElement element) {
			JsonArray array = element.getAsJsonArray();
			List<String> list = new ArrayList<>(array.size());
			for (JsonElement e : array) {
				list.add(e.getAsString());
			}
			return list;
		}
	};

	public StringListConfig(@NotNull String name, @NotNull ConfigCategory category, @NotNull List<String> defaultValue) {
		super(name, category, Collections.unmodifiableList(new ArrayList<>(defaultValue)));
	}

	public StringListConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, @NotNull List<String> defaultValue) {
		super(name, displayName, category, Collections.unmodifiableList(new ArrayList<>(defaultValue)));
	}

	@Override
	public void set(List<String> value) {
		if (value == null) {
			throw new IllegalArgumentException("Config value cannot be null: " + this.name());
		}
		super.set(Collections.unmodifiableList(new ArrayList<>(value)));
	}
}
