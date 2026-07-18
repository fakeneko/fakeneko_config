package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigEntry extends ConfigList.Entry {
	private static final Component RESET = Component.translatable("config.fakeneko_config.reset_single");

	private final ConfigScreen screen;
	private final Config<?> config;
	private final List<GuiEventListener> children = new ArrayList<>();
	private final Button resetButton;
	private final List<Runnable> refreshers = new ArrayList<>();

	public ConfigEntry(ConfigScreen screen, @NotNull Config<?> config) {
		this.screen = screen;
		this.config = config;
		this.resetButton = Button.builder(RESET, button -> this.onReset())
			.bounds(0, 0, 40, 20)
			.build();
		this.children.add(this.resetButton);
		this.createWidget();
	}

	public Config<?> config() {
		return this.config;
	}

	private void createWidget() {
		switch (this.config) {
			case BooleanConfig booleanConfig -> this.createBooleanWidget(booleanConfig);
			case IntegerConfig integerConfig -> this.createIntegerWidget(integerConfig);
			case DoubleConfig doubleConfig -> this.createDoubleWidget(doubleConfig);
			case StringConfig stringConfig -> this.createStringWidget(stringConfig);
			case StringListConfig stringListConfig -> this.createStringListWidget(stringListConfig);
			case HotkeyConfig hotkeyConfig -> this.createHotkeyWidget(hotkeyConfig);
			case EnumConfig<?> enumConfig -> this.createEnumWidget(enumConfig);
			default -> {
				// Unsupported type
			}
		}
		this.refresh();
	}

	private void createBooleanWidget(BooleanConfig booleanConfig) {
		Button button = Button.builder(this.booleanLabel(this.screen.getEffectiveValue(booleanConfig)), b -> {
			boolean current = this.screen.getEffectiveValue(booleanConfig);
			this.screen.setPendingValue(booleanConfig, !current);
		}).bounds(0, 0, 100, 20).build();
		this.children.add(button);
		this.refreshers.add(() -> button.setMessage(this.booleanLabel(this.screen.getEffectiveValue(booleanConfig))));

		HotkeyConfig hotkey = booleanConfig.hotkey();
		if (hotkey != null) {
			Button hotkeyButton = Button.builder(Component.empty(), b -> {
				Minecraft.getInstance().gui.setScreen(new HotkeyScreen(this.screen, hotkey, this.screen.getEffectiveValue(hotkey), value -> this.screen.setPendingValue(hotkey, value)));
			}).bounds(0, 0, 60, 20).build();
			this.children.add(hotkeyButton);
			this.refreshers.add(() -> hotkeyButton.setMessage(this.formatHotkey(this.screen.getEffectiveValue(hotkey))));
		}
	}

	private void createIntegerWidget(IntegerConfig integerConfig) {
		var slider = new AbstractSliderButton(0, 0, 100, 20, Component.empty(),
			(this.screen.getEffectiveValue(integerConfig) - integerConfig.min()) / (double) (integerConfig.max() - integerConfig.min())) {
			@Override
			protected void applyValue() {
				int value = integerConfig.min() + (int) Math.round(this.value * (integerConfig.max() - integerConfig.min()));
				ConfigEntry.this.screen.setPendingValue(integerConfig, value);
			}

			@Override
			protected void updateMessage() {
				this.setMessage(Component.literal(String.valueOf(ConfigEntry.this.screen.getEffectiveValue(integerConfig))));
			}

			public void refresh() {
				int effective = ConfigEntry.this.screen.getEffectiveValue(integerConfig);
				this.value = (effective - integerConfig.min()) / (double) (integerConfig.max() - integerConfig.min());
				this.updateMessage();
			}

			{
				this.updateMessage();
			}

			@Override
			public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
				if (this.isMouseOver(event.x(), event.y())) {
					double thumbX = this.getX() + this.value * (this.getWidth() - 8);
					if (event.x() >= thumbX && event.x() <= thumbX + 8) {
						return super.mouseClicked(event, doubleClick);
					}
				}
				return false;
			}
		};
		this.children.add(slider);
		this.refreshers.add(() -> slider.refresh());
	}

	private void createDoubleWidget(DoubleConfig doubleConfig) {
		var slider = new AbstractSliderButton(0, 0, 100, 20, Component.empty(),
			(this.screen.getEffectiveValue(doubleConfig) - doubleConfig.min()) / (doubleConfig.max() - doubleConfig.min())) {
			@Override
			protected void applyValue() {
				double value = doubleConfig.min() + this.value * (doubleConfig.max() - doubleConfig.min());
				ConfigEntry.this.screen.setPendingValue(doubleConfig, value);
			}

			@Override
			protected void updateMessage() {
				this.setMessage(Component.literal(String.format("%.2f", ConfigEntry.this.screen.getEffectiveValue(doubleConfig))));
			}

			public void refresh() {
				double effective = ConfigEntry.this.screen.getEffectiveValue(doubleConfig);
				this.value = (effective - doubleConfig.min()) / (doubleConfig.max() - doubleConfig.min());
				this.updateMessage();
			}

			{
				this.updateMessage();
			}

			@Override
			public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
				if (this.isMouseOver(event.x(), event.y())) {
					double thumbX = this.getX() + this.value * (this.getWidth() - 8);
					if (event.x() >= thumbX && event.x() <= thumbX + 8) {
						return super.mouseClicked(event, doubleClick);
					}
				}
				return false;
			}
		};
		this.children.add(slider);
		this.refreshers.add(() -> slider.refresh());
	}

	private void createStringWidget(StringConfig stringConfig) {
		EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 150, 20, Component.empty());
		java.util.function.Consumer<String> responder = value -> this.screen.setPendingValue(stringConfig, value);
		box.setValue(this.screen.getEffectiveValue(stringConfig));
		box.setResponder(responder::accept);
		this.children.add(box);
		this.refreshers.add(() -> {
			String text = this.screen.getEffectiveValue(stringConfig);
			if (box.getValue().equals(text)) {
				return;
			}
			box.setResponder(null);
			box.setValue(text);
			box.setResponder(responder::accept);
		});
	}

	private void createStringListWidget(StringListConfig stringListConfig) {
		EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 150, 20, Component.empty());
		java.util.function.Consumer<String> responder = value -> this.screen.setPendingValue(stringListConfig, parseStringList(value));
		box.setValue(String.join(", ", this.screen.getEffectiveValue(stringListConfig)));
		box.setResponder(responder::accept);
		this.children.add(box);
		this.refreshers.add(() -> {
			List<String> effective = this.screen.getEffectiveValue(stringListConfig);
			if (parseStringList(box.getValue()).equals(effective)) {
				return;
			}
			box.setResponder(null);
			box.setValue(String.join(", ", effective));
			box.setResponder(responder::accept);
		});
	}

	private static List<String> parseStringList(String value) {
		String[] split = value.split(",");
		List<String> list = new ArrayList<>();
		for (String s : split) {
			String trimmed = s.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return list;
	}

	private void createHotkeyWidget(HotkeyConfig hotkeyConfig) {
		Button button = Button.builder(Component.empty(), b -> {
			Minecraft.getInstance().gui.setScreen(new HotkeyScreen(this.screen, hotkeyConfig, this.screen.getEffectiveValue(hotkeyConfig), value -> this.screen.setPendingValue(hotkeyConfig, value)));
		}).bounds(0, 0, 100, 20).build();
		this.children.add(button);
		this.refreshers.add(() -> button.setMessage(this.formatHotkey(this.screen.getEffectiveValue(hotkeyConfig))));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <E extends Enum<E>> void createEnumWidget(EnumConfig<E> enumConfig) {
		E[] values = enumConfig.values();
		if (enumConfig.widget() == EnumWidget.CYCLIC) {
			Button button = Button.builder(Component.empty(), b -> {
				E current = this.screen.getEffectiveValue(enumConfig);
				int next = (current.ordinal() + 1) % values.length;
				this.screen.setPendingValue(enumConfig, values[next]);
			}).bounds(0, 0, 100, 20).build();
			this.children.add(button);
			this.refreshers.add(() -> button.setMessage(enumConfig.displayValue(this.screen.getEffectiveValue(enumConfig))));
		} else {
			Button button = Button.builder(Component.empty(), b -> {
				Minecraft.getInstance().gui.setScreen(new EnumDropdownScreen<>(this.screen, enumConfig, (E selected) -> {
					this.screen.setPendingValue(enumConfig, selected);
				}));
			}).bounds(0, 0, 100, 20).build();
			this.children.add(button);
			this.refreshers.add(() -> button.setMessage(enumConfig.displayValue(this.screen.getEffectiveValue(enumConfig))));
		}
	}

	private Component formatHotkey(InputKeys keys) {
		return keys.isEmpty() ? Component.literal("None") : InputKeys.format(keys);
	}

	private Component booleanLabel(boolean value) {
		net.minecraft.ChatFormatting color = value ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED;
		return Component.translatable(value ? "config.fakeneko_config.true" : "config.fakeneko_config.false")
			.copy()
			.withStyle(color);
	}

	private void onReset() {
		if (this.config instanceof BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
			this.screen.setPendingValue(booleanConfig, booleanConfig.defaultValue());
			this.screen.setPendingValue(booleanConfig.hotkey(), booleanConfig.hotkey().defaultValue());
		} else {
			this.screen.setPendingValue(this.config, this.config.defaultValue());
		}
		this.refresh();
	}

	private boolean isModified() {
		if (this.config instanceof BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
			return !this.valuesEqual(this.screen.getEffectiveValue(booleanConfig), booleanConfig.defaultValue())
				|| !this.valuesEqual(this.screen.getEffectiveValue(booleanConfig.hotkey()), booleanConfig.hotkey().defaultValue());
		}
		return !this.valuesEqual(this.screen.getEffectiveValue(this.config), this.config.defaultValue());
	}

	private boolean valuesEqual(Object a, Object b) {
		if (a instanceof java.util.List<?> listA && b instanceof java.util.List<?> listB) {
			return listA.equals(listB);
		}
		return java.util.Objects.equals(a, b);
	}

	public void refresh() {
		for (Runnable refresher : this.refreshers) {
			refresher.run();
		}
	}

	void updateNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, Component.literal(this.config.name()));
	}

	private boolean isModifiedFromInitial() {
		if (this.config instanceof BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
			return this.screen.isModifiedFromInitial(this.config) || this.screen.isModifiedFromInitial(booleanConfig.hotkey());
		}
		return this.screen.isModifiedFromInitial(this.config);
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
		if (this.isModifiedFromInitial()) {
			graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x1A4488FF);
		}
		this.resetButton.active = this.isModified();
		graphics.text(Minecraft.getInstance().font, this.config.displayName(), this.getX() + 10, this.getY() + 6, -1);
		Component description = this.config.description();
		if (hovered && description != null) {
			graphics.setTooltipForNextFrame(description, mouseX, mouseY);
		}
		boolean hasHotkey = this.config instanceof BooleanConfig bc && bc.hotkey() != null;
		int rightEdge = this.getX() + this.getWidth() - 10;
		this.resetButton.setX(rightEdge - 40);
		this.resetButton.setY(this.getY() + 2);
		this.resetButton.extractRenderState(graphics, mouseX, mouseY, a);
		if (hasHotkey) {
			int mainWidth = 70;
			int hotkeyWidth = 60;
			int mainX = rightEdge - 40 - 5 - mainWidth;
			int hotkeyX = mainX - 5 - hotkeyWidth;
			Button mainButton = (Button) this.children.get(1);
			mainButton.setX(mainX);
			mainButton.setY(this.getY() + 2);
			mainButton.setWidth(mainWidth);
			mainButton.extractRenderState(graphics, mouseX, mouseY, a);
			Button hotkeyButton = (Button) this.children.get(2);
			hotkeyButton.setX(hotkeyX);
			hotkeyButton.setY(this.getY() + 2);
			hotkeyButton.setWidth(hotkeyWidth);
			hotkeyButton.extractRenderState(graphics, mouseX, mouseY, a);
		} else {
			for (int i = 1; i < this.children.size(); i++) {
				GuiEventListener child = this.children.get(i);
				if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
					int widgetWidth = widget.getWidth();
					widget.setX(rightEdge - 40 - 5 - widgetWidth);
					widget.setY(this.getY() + 2);
					widget.extractRenderState(graphics, mouseX, mouseY, a);
				}
			}
		}
	}

	@Override
	public List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
		return java.util.Collections.emptyList();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return this.children;
	}
}
