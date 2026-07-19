package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.impl.types.EnumConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EnumDropdownScreen<E extends Enum<E>> extends Screen {
	private static final Component TITLE = Component.translatable("config.fakeneko_config.enum.title");
	private static final Component CANCEL = Component.translatable("config.fakeneko_config.cancel");

	private static final int ROW_HEIGHT = 24;
	private static final int OPTION_WIDTH = 200;
	private static final int OPTION_HEIGHT = 20;

	private final ConfigScreen lastScreen;
	private final EnumConfig<E> config;
	private final Consumer<E> onSelect;

	private EnumOptionList optionList;

	public EnumDropdownScreen(ConfigScreen lastScreen, EnumConfig<E> config, Consumer<E> onSelect) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.config = config;
		this.onSelect = onSelect;
	}

	@Override
	protected void init() {
		int headerHeight = 40;
		int footerHeight = 40;

		this.optionList = new EnumOptionList(this.minecraft, this.width, this.height, headerHeight, this.height - footerHeight);
		this.addRenderableWidget(this.optionList);

		this.addRenderableWidget(new Button(
			this.width / 2 - OPTION_WIDTH / 2, this.height - 30, OPTION_WIDTH, OPTION_HEIGHT,
			CANCEL,
			button -> this.minecraft.setScreen(this.lastScreen)
		));
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		super.render(poseStack, mouseX, mouseY, partialTick);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
	}

	private class EnumOptionList extends ContainerObjectSelectionList<EnumOptionList.OptionEntry> {
		EnumOptionList(Minecraft minecraft, int width, int height, int y0, int y1) {
			super(minecraft, width, height, y0, y1, ROW_HEIGHT);
			E current = EnumDropdownScreen.this.lastScreen.getEffectiveValue(EnumDropdownScreen.this.config);
			for (E value : EnumDropdownScreen.this.config.values()) {
				this.addEntry(new OptionEntry(value, value == current));
			}
		}

		@Override
		public int getRowWidth() {
			return OPTION_WIDTH;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.x1 - 6;
		}

		private class OptionEntry extends ContainerObjectSelectionList.Entry<OptionEntry> {
			private final Button button;
			private final List<GuiEventListener> children;

			OptionEntry(E value, boolean current) {
				Component label = EnumDropdownScreen.this.config.displayValue(value);
				if (current) {
					label = Component.literal("✓ ").withStyle(ChatFormatting.AQUA)
						.append(label.copy().withStyle(ChatFormatting.AQUA));
				}
				this.button = new Button(0, 0, OPTION_WIDTH, OPTION_HEIGHT, label, b -> {
					EnumDropdownScreen.this.onSelect.accept(value);
					EnumDropdownScreen.this.minecraft.setScreen(EnumDropdownScreen.this.lastScreen);
				});
				if (current) {
					this.button.active = false;
				}
				this.children = Collections.singletonList(this.button);
			}

			@Override
			public List<? extends GuiEventListener> children() {
				return this.children;
			}

			@Override
			public List<? extends NarratableEntry> narratables() {
				return Collections.singletonList(this.button);
			}

			@Override
			public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
				this.button.x = left + (width - OPTION_WIDTH) / 2;
				this.button.y = top + (height - OPTION_HEIGHT) / 2;
				this.button.render(poseStack, mouseX, mouseY, partialTick);
			}
		}
	}
}
