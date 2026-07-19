package cn.com.fakeneko.config.api;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A category that groups related {@link Config} entries.
 */
public class ConfigCategory {
	private final String name;
	private final Component displayName;
	private final List<Config<?>> configs = new ArrayList<>();

	public ConfigCategory(@NotNull String name, @NotNull Component displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	@NotNull
	public String name() {
		return this.name;
	}

	@NotNull
	public Component displayName() {
		return this.displayName;
	}

	@NotNull
	public List<Config<?>> configs() {
		return Collections.unmodifiableList(this.configs);
	}

	/**
	 * Registers a config entry into this category.
	 *
	 * @param config The config to add.
	 */
	public void addConfig(@NotNull Config<?> config) {
		this.configs.add(config);
	}

	/**
	 * Registers multiple config entries into this category.
	 *
	 * @param configs The configs to add.
	 */
	public void addConfigs(@NotNull Config<?>... configs) {
		for (Config<?> config : configs) {
			this.addConfig(config);
		}
	}
}
