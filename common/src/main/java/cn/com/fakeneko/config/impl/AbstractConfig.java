package cn.com.fakeneko.config.impl;

import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig<T> implements Config<T> {
	private final String name;
	private final Component displayName;
	private final ConfigCategory category;
	private final T defaultValue;
	private T value;
	private Component description;
	private final List<ConfigListener<T>> listeners = new ArrayList<>();

	protected AbstractConfig(@NotNull String name, @NotNull ConfigCategory category, T defaultValue) {
		this(name, Component.literal(name), category, defaultValue);
	}

	protected AbstractConfig(@NotNull String name, @NotNull Component displayName, @NotNull ConfigCategory category, T defaultValue) {
		this.name = name;
		this.displayName = displayName;
		this.category = category;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		category.addConfig(this);
	}

	@Override
	@NotNull
	public String name() {
		return this.name;
	}

	@Override
	@NotNull
	public Component displayName() {
		return this.displayName;
	}

	@Override
	@org.jetbrains.annotations.Nullable
	public Component description() {
		return this.description;
	}

	/**
	 * Sets the description shown as a tooltip for this config in the GUI.
	 *
	 * @param description The description component.
	 * @return This config, for chaining.
	 */
	public AbstractConfig<T> withDescription(@NotNull Component description) {
		this.description = description;
		return this;
	}

	@Override
	@NotNull
	public ConfigCategory category() {
		return this.category;
	}

	@Override
	public T defaultValue() {
		return this.defaultValue;
	}

	@Override
	public T get() {
		return this.value;
	}

	@Override
	public void set(T value) {
		if (value == null) {
			throw new IllegalArgumentException("Config value cannot be null: " + this.name);
		}
		T from = this.value;
		this.value = value;
		for (ConfigListener<T> listener : this.listeners) {
			listener.onChanged(this, from, value);
		}
	}

	@Override
	public boolean isModified() {
		if (this.value instanceof List<?> list && this.defaultValue instanceof List<?> defaultList) {
			return !list.equals(defaultList);
		}
		if (this.value == null) {
			return this.defaultValue != null;
		}
		return !this.value.equals(this.defaultValue);
	}

	@Override
	public void addListener(@NotNull ConfigListener<T> listener) {
		this.listeners.add(listener);
	}

	public void removeListener(@NotNull ConfigListener<T> listener) {
		this.listeners.remove(listener);
	}
}
