package cn.com.fakeneko.config.impl.keybind;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * This interface represents a listener with events
 * that can be triggered by a keybind.
 */
public interface KeybindListener {
	/**
	 * This event is triggered when the keybind is pressed.
	 */
	default void onPress() {

	}

	/**
	 * This event is triggered when the keybind is released.
	 */
	default void onRelease() {

	}

	/**
	 * This event is triggered when the keybind is set.
	 *
	 * @param keys The keys that were set.
	 */
	default void onSetKeys(InputKeys keys) {

	}

	static KeybindListener onPress(Runnable runnable) {
		return new KeybindListener() {
			@Override
			public void onPress() {
				runnable.run();
			}
		};
	}

	static KeybindListener onRelease(Runnable runnable) {
		return new KeybindListener() {
			@Override
			public void onRelease() {
				runnable.run();
			}
		};
	}

	static KeybindListener onSetKeys(Consumer<InputKeys> consumer) {
		return new KeybindListener() {
			@Override
			public void onSetKeys(InputKeys keys) {
				consumer.accept(keys);
			}
		};
	}

	static KeybindListener identity(ResourceLocation id, KeybindListener listener) {
		return new Keyed(id, listener);
	}

	record Keyed(ResourceLocation id, KeybindListener listener) implements KeybindListener {
		@Override
		public void onPress() {
			this.listener.onPress();
		}

		@Override
		public void onRelease() {
			this.listener.onRelease();
		}

		@Override
		public void onSetKeys(InputKeys keys) {
			this.listener.onSetKeys(keys);
		}

		@Override
		public int hashCode() {
			return this.id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Keyed other && this.id.equals(other.id);
		}
	}
}
