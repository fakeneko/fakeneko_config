package cn.com.fakeneko.config.api;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a single configuration option.
 *
 * @param <T> The type of the configuration value.
 */
public interface Config<T> {
	/**
	 * The unique identifier of this config option.
	 *
	 * @return The config identifier.
	 */
	@NotNull
	String name();

	/**
	 * The human-readable display name of this config option.
	 * Defaults to the raw config identifier if no display name is provided.
	 *
	 * @return The display name.
	 */
	@NotNull
	default Component displayName() {
		return Component.literal(this.name());
	}

	/**
	 * The human-readable description of this config option, shown as a
	 * tooltip when hovering the entry in the config GUI.
	 *
	 * @return The description, or {@code null} if this config has no description.
	 */
	@org.jetbrains.annotations.Nullable
	default Component description() {
		return null;
	}

	/**
	 * Whether this config should be shown as a standalone row in the config GUI.
	 * Internal / sub-configs can return false.
	 *
	 * @return true if visible.
	 */
	default boolean isVisible() {
		return true;
	}

	/**
	 * The category this config belongs to.
	 *
	 * @return The category.
	 */
	@NotNull
	ConfigCategory category();

	/**
	 * The default value for this config.
	 *
	 * @return The default value.
	 */
	T defaultValue();

	/**
	 * Gets the current value.
	 *
	 * @return The current value.
	 */
	T get();

	/**
	 * Sets the current value.
	 *
	 * @param value The new value.
	 */
	void set(T value);

	/**
	 * Resets the value to its default.
	 */
	default void reset() {
		this.set(this.defaultValue());
	}

	/**
	 * Whether the current value differs from the default.
	 *
	 * @return True if modified.
	 */
	default boolean isModified() {
		return !this.get().equals(this.defaultValue());
	}

	/**
	 * Adds a listener that will be notified when the value changes.
	 *
	 * @param listener The listener to add.
	 */
	void addListener(@NotNull ConfigListener<T> listener);

	/**
	 * Adds listeners to this config.
	 *
	 * @param listeners The listeners to add.
	 */
	@SuppressWarnings("unchecked")
	default void addListeners(@NotNull ConfigListener<T>... listeners) {
		for (ConfigListener<T> listener : listeners) {
			this.addListener(listener);
		}
	}
}
