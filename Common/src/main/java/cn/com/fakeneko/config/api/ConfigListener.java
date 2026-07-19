package cn.com.fakeneko.config.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * A listener for changes to a {@link Config} value.
 *
 * @param <T> The type of the configuration value.
 */
@FunctionalInterface
public interface ConfigListener<T> {
	/**
	 * Called after a config value has changed.
	 *
	 * @param config The config that changed.
	 * @param from The previous value.
	 * @param to The new value.
	 */
	void onChanged(@NotNull Config<T> config, T from, T to);
}
