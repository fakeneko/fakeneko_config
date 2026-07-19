package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.impl.types.EnumConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
		int listHeight = this.height - headerHeight - footerHeight;

		this.optionList = new EnumOptionList(this.minecraft, this.width, listHeight, headerHeight);
		this.addRenderableWidget(this.optionList);

		this.addRenderableWidget(Button.builder(CANCEL, button -> this.minecraft.setScreen(this.lastScreen))
			.bounds(this.width / 2 - OPTION_WIDTH / 2, this.height - 30, OPTION_WIDTH, OPTION_HEIGHT)
			.build());
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(Minecraft.getInstance().font, this.title, this.width / 2, 15, -1);
	}

	private class EnumOptionList extends ContainerObjectSelectionList<EnumOptionList.OptionEntry> {
		EnumOptionList(Minecraft minecraft, int width, int height, int y) {
			super(minecraft, width, height, y, ROW_HEIGHT);
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
		protected int scrollBarX() {
			return this.getRight() - 6;
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
				this.button = Button.builder(label, b -> {
					EnumDropdownScreen.this.onSelect.accept(value);
					EnumDropdownScreen.this.minecraft.setScreen(EnumDropdownScreen.this.lastScreen);
				}).bounds(0, 0, OPTION_WIDTH, OPTION_HEIGHT).build();
				if (current) {
					this.button.active = false;
				}
				this.children = List.of(this.button);
			}

			@Override
			public List<? extends GuiEventListener> children() {
				return this.children;
			}

			@Override
			public List<? extends NarratableEntry> narratables() {
				return List.of(this.button);
			}

			@Override
			public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
				this.button.setX(this.getContentXMiddle() - OPTION_WIDTH / 2);
				this.button.setY(this.getContentYMiddle() - OPTION_HEIGHT / 2);
				this.button.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}
}
