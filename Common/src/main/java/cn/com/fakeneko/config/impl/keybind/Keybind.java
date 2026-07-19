package cn.com.fakeneko.config.impl.keybind;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * This interface represents a keybind that will be
 * activated when the required {@link Keybind#keys()}
 * are all pressed.
 */
public interface Keybind {
	/**
	 * The name of the keybind.
	 *
	 * @return The keybind name.
	 */
	Component name();

	/**
	 * The required keys for this keybind
	 * to be activated.
	 *
	 * @return The input keys.
	 */
	InputKeys keys();

	/**
	 * This marks the keybind as being held.
	 */
	void hold();

	/**
	 * This clicks the keybind.
	 */
	void click();

	/**
	 * This marks the keybind as not being held.
	 */
	void release();

	/**
	 * Checks whether the keybind is being held.
	 *
	 * @return Whether the keybind is held.
	 */
	boolean isHeld();

	/**
	 * This will consume all clicks that
	 * the keybind has accumulated.
	 *
	 * @return The number of times this keybind was clicked.
	 */
	int consumeClicks();

	/**
	 * Checks whether the keybind is set to
	 * its default keys.
	 *
	 * @return Whether the keybind is using default keys.
	 */
	boolean areKeysDefault();

	/**
	 * Sets the keys for this keybind.
	 *
	 * @param keys The new keys.
	 */
	void setKeys(InputKeys keys);

	/**
	 * Resets the keys for this keybind to
	 * the default keys.
	 */
	void resetKeysToDefault();

	/**
	 * Clears the keys for this keybind.
	 */
	default void clearKeys() {
		this.setKeys(InputKeys.EMPTY);
	}

	/**
	 * Adds a listener to this keybind.
	 *
	 * @param listener The listener to add.
	 */
	void addListener(KeybindListener listener);
}
