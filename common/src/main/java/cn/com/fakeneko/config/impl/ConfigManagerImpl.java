package cn.com.fakeneko.config.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigManager;
import cn.com.fakeneko.config.api.ConfigSerializer;
import cn.com.fakeneko.config.impl.types.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManagerImpl implements ConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("ConfigManager");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final String modId;
	private final Path configPath;
	private final net.minecraft.network.chat.Component displayName;
	private final Map<String, ConfigCategory> categories = new LinkedHashMap<>();

	public ConfigManagerImpl(@NotNull String modId, @NotNull Path configPath, @NotNull net.minecraft.network.chat.Component displayName) {
		this.modId = modId;
		this.configPath = configPath;
		this.displayName = displayName;
	}

	public ConfigManagerImpl(@NotNull String modId, @NotNull net.minecraft.network.chat.Component displayName) {
		this(modId, Path.of("config").resolve(modId + ".json"), displayName);
	}

	public ConfigManagerImpl(@NotNull String modId, @NotNull Path configPath) {
		this(modId, configPath, net.minecraft.network.chat.Component.translatable("config." + modId + ".title"));
	}

	public ConfigManagerImpl(@NotNull String modId) {
		this(modId, Path.of("config").resolve(modId + ".json"), net.minecraft.network.chat.Component.translatable("config." + modId + ".title"));
	}

	@Override
	@NotNull
	public net.minecraft.network.chat.Component displayName() {
		return this.displayName;
	}

	@Override
	public ConfigCategory createCategory(@NotNull String name, @NotNull net.minecraft.network.chat.Component displayName) {
		ConfigCategory category = new ConfigCategory(name, displayName);
		this.registerCategory(category);
		return category;
	}

	@Override
	public void registerCategory(@NotNull ConfigCategory category) {
		this.categories.put(category.name(), category);
	}

	@Override
	public ConfigCategory getCategory(@NotNull String name) {
		return this.categories.get(name);
	}

	@Override
	@NotNull
	public Collection<ConfigCategory> categories() {
		return Collections.unmodifiableCollection(this.categories.values());
	}

	@Override
	public void load() {
		if (!Files.exists(this.configPath)) {
			LOGGER.info("Config file not found, saving defaults: {}", this.configPath);
			this.save();
			return;
		}

		try (var reader = Files.newBufferedReader(this.configPath)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			for (ConfigCategory category : this.categories.values()) {
				JsonObject categoryObject = root.getAsJsonObject(category.name());
				if (categoryObject == null) {
					continue;
				}
				for (Config<?> config : category.configs()) {
					this.loadConfig(config, categoryObject);
				}
			}
		} catch (IOException | JsonParseException e) {
			LOGGER.error("Failed to load config: {}", this.configPath, e);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> void loadConfig(Config<T> config, JsonObject categoryObject) {
		JsonElement element = categoryObject.get(config.name());
		if (element == null || element.isJsonNull()) {
			return;
		}
		try {
			ConfigSerializer<T> serializer = this.serializerFor(config);
			if (serializer != null) {
				config.set(serializer.deserialize(element));
			}
		} catch (Exception e) {
			LOGGER.error("Failed to deserialize config {} in category {}", config.name(), config.category().name(), e);
		}
	}

	@Override
	public void save() {
		JsonObject root = new JsonObject();
		for (ConfigCategory category : this.categories.values()) {
			JsonObject categoryObject = new JsonObject();
			for (Config<?> config : category.configs()) {
				this.saveConfig(config, categoryObject);
			}
			root.add(category.name(), categoryObject);
		}
		try {
			Files.createDirectories(this.configPath.getParent());
			try (var writer = Files.newBufferedWriter(this.configPath)) {
				GSON.toJson(root, writer);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to save config: {}", this.configPath, e);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> void saveConfig(Config<T> config, JsonObject categoryObject) {
		ConfigSerializer<T> serializer = this.serializerFor(config);
		if (serializer != null) {
			categoryObject.add(config.name(), serializer.serialize(config.get()));
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> ConfigSerializer<T> serializerFor(Config<T> config) {
		return (ConfigSerializer<T>) switch (config) {
			case BooleanConfig ignored -> BooleanConfig.SERIALIZER;
			case IntegerConfig ignored -> IntegerConfig.SERIALIZER;
			case DoubleConfig ignored -> DoubleConfig.SERIALIZER;
			case StringConfig ignored -> StringConfig.SERIALIZER;
			case StringListConfig ignored -> StringListConfig.SERIALIZER;
			case HotkeyConfig ignored -> HotkeyConfig.SERIALIZER;
			default -> null;
		};
	}

	public Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(this.modId, path);
	}
}
