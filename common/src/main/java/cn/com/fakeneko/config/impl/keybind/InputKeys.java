package cn.com.fakeneko.config.impl.keybind;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * This class represents a list of ordered input keys.
 */
public class InputKeys extends AbstractCollection<InputConstants.Key> {
	private static final Component SEPARATOR = Component.literal(" + ");
	public static final InputKeys EMPTY = new InputKeys(List.of());

	public static final Codec<InputKeys> CODEC = Codec.STRING.xmap(
		InputKeys::fromString,
		InputKeys::toString
	);

	private static final String SEPARATOR_STR = "+";

	private final List<InputConstants.Key> keys;

	public InputKeys(List<InputConstants.Key> keys) {
		this.keys = keys.stream().distinct().toList();
	}

	public InputKeys(InputConstants.Key[] keys) {
		this.keys = Arrays.stream(keys).distinct().toList();
	}

	public boolean isLastKey(InputConstants.Key key) {
		return !this.keys.isEmpty() && this.keys.getLast().equals(key);
	}

	@Override
	public @NotNull Iterator<InputConstants.Key> iterator() {
		return this.keys.iterator();
	}

	@Override
	public int size() {
		return this.keys.size();
	}

	@Override
	public boolean contains(Object o) {
		return this.keys.contains(o);
	}

	public static MutableComponent format(Collection<InputConstants.Key> keys) {
		if (keys.isEmpty()) {
			return Component.empty().append(InputConstants.UNKNOWN.getDisplayName());
		}
		return ComponentUtils.formatList(keys, SEPARATOR, InputConstants.Key::getDisplayName);
	}

	public static Component formatEditing(Collection<InputConstants.Key> keys) {
		return Component.literal("> ")
			.append(format(keys).withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
			.append(" <")
			.withStyle(ChatFormatting.YELLOW);
	}

	public static InputKeys of(int ...keys) {
		return ofKeys(keys);
	}

	public static InputKeys ofKeys(int ...keys) {
		return new InputKeys(IntStream.of(keys).mapToObj(InputConstants.Type.KEYSYM::getOrCreate).toList());
	}

	public static InputKeys ofMouse(int ...buttons) {
		return new InputKeys(IntStream.of(buttons).mapToObj(InputConstants.Type.MOUSE::getOrCreate).toList());
	}

	public static InputKeys ofAll(InputConstants.Key... keys) {
		return new InputKeys(Arrays.asList(keys));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof InputKeys other) {
			if (this.keys.size() != other.keys.size()) {
				return false;
			}
			for (int i = 0; i < this.keys.size(); i++) {
				if (!this.keys.get(i).getName().equals(other.keys.get(i).getName())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 1;
		for (InputConstants.Key key : this.keys) {
			result = 31 * result + key.getName().hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		if (this.keys.isEmpty()) {
			return InputConstants.UNKNOWN.getName();
		}
		return String.join(SEPARATOR_STR, this.keys.stream().map(InputKeys::toShortName).toList());
	}

	private static String toShortName(InputConstants.Key key) {
		String name = key.getName();
		if (name.startsWith("key.mouse.")) {
			int index = switch (name.substring("key.mouse.".length())) {
				case "left" -> 1;
				case "right" -> 2;
				case "middle" -> 3;
				case "4" -> 4;
				case "5" -> 5;
				case "6" -> 6;
				case "7" -> 7;
				case "8" -> 8;
				default -> throw new IllegalArgumentException("Unknown mouse key: " + name);
			};
			return "M" + index;
		}
		if (name.startsWith("key.keyboard.")) {
			return name.substring("key.keyboard.".length()).toUpperCase(Locale.ROOT);
		}
		return name;
	}

	public static InputKeys fromString(String value) {
		if (value == null || value.isBlank()) {
			return EMPTY;
		}
		String[] parts = value.split("\\+");
		List<InputConstants.Key> keys = new ArrayList<>(parts.length);
		for (String part : parts) {
			keys.add(fromShortName(part.trim()));
		}
		return new InputKeys(keys);
	}

	private static InputConstants.Key fromShortName(String shortName) {
		if (shortName.length() >= 2 && shortName.charAt(0) == 'M') {
			try {
				int index = Integer.parseInt(shortName.substring(1));
				if (index >= 1 && index <= 8) {
					String name = switch (index) {
						case 1 -> "key.mouse.left";
						case 2 -> "key.mouse.right";
						case 3 -> "key.mouse.middle";
						default -> "key.mouse." + index;
					};
					return InputConstants.getKey(name);
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return InputConstants.getKey("key.keyboard." + shortName.toLowerCase(Locale.ROOT));
	}

	public static class Serializer implements JsonSerializer<InputKeys>, JsonDeserializer<InputKeys> {
		public static final Serializer INSTANCE = new Serializer();

		private Serializer() {

		}

		@Override
		public InputKeys deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			DataResult<InputKeys> result = CODEC.parse(JsonOps.INSTANCE, json);
			return result.getOrThrow(JsonParseException::new);
		}

		@Override
		public JsonElement serialize(InputKeys src, Type typeOfSrc, JsonSerializationContext context) {
			DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, src);
			return result.getOrThrow(IllegalArgumentException::new);
		}
	}
}
