package cn.com.fakeneko.config.api;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

/**
 * Serializes and deserializes a config value to/from JSON.
 *
 * @param <T> The type of the config value.
 */
public interface ConfigSerializer<T> {
	/**
	 * Serializes the given value into a JSON element.
	 *
	 * @param value The value to serialize.
	 * @return The JSON element.
	 */
	@NotNull
	JsonElement serialize(T value);

	/**
	 * Deserializes the given JSON element into a value.
	 *
	 * @param element The JSON element.
	 * @return The deserialized value.
	 */
	@NotNull
	T deserialize(@NotNull JsonElement element);
}
