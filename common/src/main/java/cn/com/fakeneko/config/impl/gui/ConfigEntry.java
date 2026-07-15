package cn.com.fakeneko.config.impl.gui;

import com.mojang.blaze3d.platform.InputConstants;
import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.keybind.KeybindListener;
import cn.com.fakeneko.config.impl.keybind.KeybindManager;
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

	public ConfigEntry(ConfigScreen screen, @NotNull Config<?> config) {
		this.screen = screen;
		this.config = config;
		this.resetButton = Button.builder(RESET, button -> this.onReset())
			.bounds(0, 0, 40, 20)
			.build();
		this.children.add(this.resetButton);
		this.createWidget();
	}

	private void createWidget() {
		switch (this.config) {
			case BooleanConfig booleanConfig -> {
				Button button = Button.builder(this.booleanLabel(booleanConfig.get()), b -> {
					booleanConfig.toggle();
					b.setMessage(this.booleanLabel(booleanConfig.get()));
				}).bounds(0, 0, 100, 20).build();
				this.children.add(button);
				HotkeyConfig hotkey = booleanConfig.hotkey();
				if (hotkey != null) {
					Button hotkeyButton = Button.builder(hotkey.get().isEmpty() ? Component.literal("None") : InputKeys.format(hotkey.get()), b -> {
						Minecraft.getInstance().gui.setScreen(new HotkeyScreen(this.screen, hotkey));
					}).bounds(0, 0, 60, 20).build();
					this.children.add(hotkeyButton);
					hotkey.addListener((config, from, to) -> hotkeyButton.setMessage(to.isEmpty() ? Component.literal("None") : InputKeys.format(to)));
				}
			}
			case IntegerConfig integerConfig -> {
				AbstractSliderButton slider = new AbstractSliderButton(0, 0, 100, 20, Component.empty(),
					(integerConfig.get() - integerConfig.min()) / (double) (integerConfig.max() - integerConfig.min())) {
					@Override
					protected void applyValue() {
						int value = integerConfig.min() + (int) Math.round(this.value * (integerConfig.max() - integerConfig.min()));
						integerConfig.set(value);
					}

					@Override
					protected void updateMessage() {
						this.setMessage(Component.literal(String.valueOf(integerConfig.get())));
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
			}
			case DoubleConfig doubleConfig -> {
				AbstractSliderButton slider = new AbstractSliderButton(0, 0, 100, 20, Component.empty(),
					(doubleConfig.get() - doubleConfig.min()) / (doubleConfig.max() - doubleConfig.min())) {
					@Override
					protected void applyValue() {
						double value = doubleConfig.min() + this.value * (doubleConfig.max() - doubleConfig.min());
						doubleConfig.set(value);
					}

					@Override
					protected void updateMessage() {
						this.setMessage(Component.literal(String.format("%.2f", doubleConfig.get())));
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
			}
			case StringConfig stringConfig -> {
				EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 150, 20, Component.empty());
				box.setValue(stringConfig.get());
				box.setResponder(stringConfig::set);
				this.children.add(box);
			}
			case StringListConfig stringListConfig -> {
				EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 150, 20, Component.empty());
				box.setValue(String.join(", ", stringListConfig.get()));
				box.setResponder(value -> {
					String[] split = value.split(",");
					List<String> list = new ArrayList<>();
					for (String s : split) {
						String trimmed = s.trim();
						if (!trimmed.isEmpty()) {
							list.add(trimmed);
						}
					}
					stringListConfig.set(list);
				});
				this.children.add(box);
			}
			case HotkeyConfig hotkeyConfig -> {
				Button button = Button.builder(hotkeyConfig.get().isEmpty() ? Component.literal("None") : InputKeys.format(hotkeyConfig.get()), b -> {
					Minecraft.getInstance().gui.setScreen(new HotkeyScreen(this.screen, hotkeyConfig));
				}).bounds(0, 0, 100, 20).build();
				this.children.add(button);
			}
			default -> {
				// Unsupported type
			}
		}
	}

	private Component booleanLabel(boolean value) {
		net.minecraft.ChatFormatting color = value ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED;
		return Component.translatable(value ? "config.fakeneko_config.true" : "config.fakeneko_config.false")
			.copy()
			.withStyle(color);
	}

	private void onReset() {
		this.config.reset();
		this.children.clear();
		this.children.add(this.resetButton);
		this.createWidget();
	}

	void updateNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, Component.literal(this.config.name()));
	}

	private boolean isModified() {
		if (this.config instanceof BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
			return this.config.isModified() || booleanConfig.hotkey().isModified();
		}
		return this.config.isModified();
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
			graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x33FFFF00);
		}
		this.resetButton.active = this.isModified();
		graphics.text(Minecraft.getInstance().font, this.config.displayName(), this.getX() + 10, this.getY() + 6, -1);
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
