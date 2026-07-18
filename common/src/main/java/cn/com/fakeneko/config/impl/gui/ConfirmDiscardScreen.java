package cn.com.fakeneko.config.impl.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A confirmation screen shown when cancelling the config screen with unsaved changes.
 * Offers "discard changes" (return to the parent screen) or "keep editing" (back to config).
 */
public class ConfirmDiscardScreen extends Screen {
	private static final Component TITLE = Component.translatable("config.fakeneko_config.confirm_discard.title");
	private static final Component MESSAGE = Component.translatable("config.fakeneko_config.confirm_discard.message");
	private static final Component DISCARD = Component.translatable("config.fakeneko_config.confirm_discard.discard");
	private static final Component KEEP_EDITING = Component.translatable("config.fakeneko_config.confirm_discard.keep_editing");

	private final ConfigScreen configScreen;

	public ConfirmDiscardScreen(ConfigScreen configScreen) {
		super(TITLE);
		this.configScreen = configScreen;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;

		LinearLayout layout = LinearLayout.vertical().spacing(8);
		layout.defaultCellSetting().alignHorizontallyCenter();
		layout.addChild(new StringWidget(this.title, this.font));
		layout.addChild(new MultiLineTextWidget(MESSAGE, this.font).setMaxWidth(this.width - 50).setCentered(true));
		layout.arrangeElements();
		layout.setPosition(centerX - layout.getWidth() / 2, this.height / 2 - 50);
		layout.visitWidgets(this::addRenderableWidget);

		int buttonWidth = 120;
		int buttonY = this.height / 2 + 10;
		this.addRenderableWidget(Button.builder(DISCARD, button -> {
			this.configScreen.discardChanges();
			this.minecraft.gui.setScreen(this.configScreen.lastScreen());
		}).bounds(centerX - buttonWidth - 5, buttonY, buttonWidth, 20).build());
		this.addRenderableWidget(Button.builder(KEEP_EDITING, button -> this.minecraft.gui.setScreen(this.configScreen))
			.bounds(centerX + 5, buttonY, buttonWidth, 20).build());
	}

	@Override
	public void onClose() {
		// ESC on the confirm screen = discard changes and leave.
		this.configScreen.discardChanges();
		this.minecraft.gui.setScreen(this.configScreen.lastScreen());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
	}
}
