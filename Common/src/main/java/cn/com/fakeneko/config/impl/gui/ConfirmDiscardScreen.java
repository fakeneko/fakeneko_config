package cn.com.fakeneko.config.impl.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cn.com.fakeneko.config.impl.Components;

/**
 * A confirmation screen shown when cancelling the config screen with unsaved changes.
 * Offers "discard changes" (return to the parent screen) or "keep editing" (back to config).
 */
public class ConfirmDiscardScreen extends Screen {
	private static final Component TITLE = Components.translatable("config.fakeneko_config.confirm_discard.title");
	private static final Component MESSAGE = Components.translatable("config.fakeneko_config.confirm_discard.message");
	private static final Component DISCARD = Components.translatable("config.fakeneko_config.confirm_discard.discard");
	private static final Component KEEP_EDITING = Components.translatable("config.fakeneko_config.confirm_discard.keep_editing");

	private final ConfigScreen configScreen;

	public ConfirmDiscardScreen(ConfigScreen configScreen) {
		super(TITLE);
		this.configScreen = configScreen;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int buttonWidth = 120;
		int gap = 10;
		int buttonY = this.height / 2 + 20;
		this.addRenderableWidget(new Button(centerX - buttonWidth - gap / 2, buttonY, buttonWidth, 20, DISCARD, button -> {
			this.configScreen.discardChanges();
			this.minecraft.setScreen(this.configScreen.lastScreen());
		}));
		this.addRenderableWidget(new Button(centerX + gap / 2, buttonY, buttonWidth, 20, KEEP_EDITING, button -> this.minecraft.setScreen(this.configScreen)));
	}

	@Override
	public void onClose() {
		// ESC on the confirm screen = discard changes and leave.
		this.configScreen.discardChanges();
		this.minecraft.setScreen(this.configScreen.lastScreen());
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(poseStack);
		// Title centered, message centered below it
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
		int msgWidth = Math.min(this.width - 100, 300);
		int msgX = (this.width - msgWidth) / 2;
		this.font.drawWordWrap(MESSAGE, msgX, this.height / 2 - 20, msgWidth, 0xAAAAAA);
		super.render(poseStack, mouseX, mouseY, partialTick);
	}
}
