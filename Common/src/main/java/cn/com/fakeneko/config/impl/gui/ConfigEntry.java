package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
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
		this.resetButton = new Button(0, 0, 40, 20, RESET, button -> this.onReset());
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
		Button button = new Button(0, 0, 100, 20, this.booleanLabel(this.screen.getEffectiveValue(booleanConfig)), b -> {
			boolean current = this.screen.getEffectiveValue(booleanConfig);
			this.screen.setPendingValue(booleanConfig, !current);
		});
		this.children.add(button);
		this.refreshers.add(() -> button.setMessage(this.booleanLabel(this.screen.getEffectiveValue(booleanConfig))));

		HotkeyConfig hotkey = booleanConfig.hotkey();
		if (hotkey != null) {
			Button hotkeyButton = new Button(0, 0, 60, 20, Component.empty(), b -> {
				Minecraft.getInstance().setScreen(new HotkeyScreen(this.screen, hotkey, this.screen.getEffectiveValue(hotkey), value -> this.screen.setPendingValue(hotkey, value)));
			});
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
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (this.isMouseOver(mouseX, mouseY)) {
					double thumbX = this.x + this.value * (this.width - 8);
					if (mouseX >= thumbX && mouseX <= thumbX + 8) {
						return super.mouseClicked(mouseX, mouseY, button);
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
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (this.isMouseOver(mouseX, mouseY)) {
					double thumbX = this.x + this.value * (this.width - 8);
					if (mouseX >= thumbX && mouseX <= thumbX + 8) {
						return super.mouseClicked(mouseX, mouseY, button);
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
		Button button = new Button(0, 0, 100, 20, Component.empty(), b -> {
			Minecraft.getInstance().setScreen(new HotkeyScreen(this.screen, hotkeyConfig, this.screen.getEffectiveValue(hotkeyConfig), value -> this.screen.setPendingValue(hotkeyConfig, value)));
		});
		this.children.add(button);
		this.refreshers.add(() -> button.setMessage(this.formatHotkey(this.screen.getEffectiveValue(hotkeyConfig))));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <E extends Enum<E>> void createEnumWidget(EnumConfig<E> enumConfig) {
		E[] values = enumConfig.values();
		if (enumConfig.widget() == EnumWidget.CYCLIC) {
			Button button = new Button(0, 0, 100, 20, Component.empty(), b -> {
				E current = this.screen.getEffectiveValue(enumConfig);
				int next = (current.ordinal() + 1) % values.length;
				this.screen.setPendingValue(enumConfig, values[next]);
			});
			this.children.add(button);
			this.refreshers.add(() -> button.setMessage(enumConfig.displayValue(this.screen.getEffectiveValue(enumConfig))));
		} else {
			Button button = new Button(0, 0, 100, 20, Component.empty(), b -> {
				Minecraft.getInstance().setScreen(new EnumDropdownScreen<>(this.screen, enumConfig, (E selected) -> {
					this.screen.setPendingValue(enumConfig, selected);
				}));
			});
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
	public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
		if (this.isModifiedFromInitial()) {
			fill(poseStack, left, top, left + width, top + height, 0x1A4488FF);
		}
		this.resetButton.active = this.isModified();
		Minecraft.getInstance().font.draw(poseStack, this.config.displayName(), left + 10, top + 6, 0xFFFFFF);
		Component description = this.config.description();
		if (hovered && description != null) {
			if (this.screen instanceof Screen) {
				this.screen.renderTooltip(poseStack, description, mouseX, mouseY);
			}
		}
		boolean hasHotkey = this.config instanceof BooleanConfig bc && bc.hotkey() != null;
		int rightEdge = left + width - 10;
		this.resetButton.x = rightEdge - 40;
		this.resetButton.y = top + 2;
		this.resetButton.render(poseStack, mouseX, mouseY, partialTick);
		if (hasHotkey) {
			int mainWidth = 70;
			int hotkeyWidth = 60;
			int mainX = rightEdge - 40 - 5 - mainWidth;
			int hotkeyX = mainX - 5 - hotkeyWidth;
			Button mainButton = (Button) this.children.get(1);
			mainButton.x = mainX;
			mainButton.y = top + 2;
			mainButton.setWidth(mainWidth);
			mainButton.render(poseStack, mouseX, mouseY, partialTick);
			Button hotkeyButton = (Button) this.children.get(2);
			hotkeyButton.x = hotkeyX;
			hotkeyButton.y = top + 2;
			hotkeyButton.setWidth(hotkeyWidth);
			hotkeyButton.render(poseStack, mouseX, mouseY, partialTick);
		} else {
			for (int i = 1; i < this.children.size(); i++) {
				GuiEventListener child = this.children.get(i);
				if (child instanceof AbstractWidget widget) {
					int widgetWidth = widget.getWidth();
					widget.x = rightEdge - 40 - 5 - widgetWidth;
					widget.y = top + 2;
					widget.render(poseStack, mouseX, mouseY, partialTick);
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
