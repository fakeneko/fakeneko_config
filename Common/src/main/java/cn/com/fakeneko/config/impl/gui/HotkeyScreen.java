package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.HotkeyConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HotkeyScreen extends Screen {
	private static final Component TITLE = Component.translatable("config.fakeneko_config.hotkey.title");
	private static final Component PRESS = Component.translatable("config.fakeneko_config.hotkey.press");
	private static final Component ESC_HINT = Component.translatable("config.fakeneko_config.hotkey.esc_hint");
	private static final Component RESET = Component.translatable("config.fakeneko_config.reset_single");
	private static final Component DONE = Component.translatable("gui.done");

	private final ConfigScreen lastScreen;
	private final HotkeyConfig config;
	private final java.util.function.Consumer<InputKeys> onComplete;
	private Button keyButton;
	private Button resetButton;
	private Button doneButton;
	private final List<InputConstants.Key> recording = new ArrayList<>();
	private boolean recordingState;

	public HotkeyScreen(ConfigScreen lastScreen, HotkeyConfig config, InputKeys initialValue, java.util.function.Consumer<InputKeys> onComplete) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.config = config;
		this.onComplete = onComplete;
		this.recording.addAll(initialValue);
	}

	@Override
	protected void init() {
		this.keyButton = this.addRenderableWidget(new Button(
			this.width / 2 - 75, this.height / 2 - 30, 150, 20,
			this.formatRecording(),
			button -> {}
		));

		this.resetButton = this.addRenderableWidget(new Button(
			this.width / 2 - 75, this.height / 2 + 10, 150, 20,
			RESET,
			button -> this.onReset()
		));
		this.updateResetButton();

		this.doneButton = this.addRenderableWidget(new Button(
			this.width / 2 - 75, this.height / 2 + 40, 150, 20,
			DONE,
			button -> this.onClose()
		));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		}
		if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
			this.onClose();
			return true;
		}
		if (this.recordingState) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
			this.recordKey(key);
		}
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.doneButton.isMouseOver(mouseX, mouseY) || this.resetButton.isMouseOver(mouseX, mouseY)) {
			return super.mouseClicked(mouseX, mouseY, button);
		}
		if (this.keyButton.isMouseOver(mouseX, mouseY)) {
			if (this.recordingState) {
				this.recordKey(InputConstants.Type.MOUSE.getOrCreate(button));
			} else {
				this.startRecording();
			}
			return true;
		}
		// Clicked outside: stop recording but stay on this screen.
		this.recordingState = false;
		return true;
	}

	private void startRecording() {
		this.recordingState = true;
		this.recording.clear();
		this.updateKeyButton();
	}

	private void recordKey(InputConstants.Key key) {
		if (!this.recording.contains(key)) {
			this.recording.add(key);
		}
		this.updateKeyButton();
	}

	private void onReset() {
		this.recordingState = false;
		this.recording.clear();
		this.recording.addAll(this.config.defaultValue());
		this.updateKeyButton();
	}

	private Component formatRecording() {
		return this.recording.isEmpty() ? PRESS : InputKeys.format(this.recording);
	}

	private void updateResetButton() {
		this.resetButton.active = !new InputKeys(this.recording).equals(this.config.defaultValue());
	}

	private void updateKeyButton() {
		this.keyButton.setMessage(this.formatRecording());
		this.updateResetButton();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
		drawCenteredString(poseStack, this.font, PRESS, this.width / 2, this.height / 2 - 55, 0xAAAAAA);
		drawCenteredString(poseStack, this.font, ESC_HINT, this.width / 2, this.height / 2 - 43, 0xAAAAAA);
		super.render(poseStack, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.onComplete.accept(new InputKeys(this.recording));
		this.minecraft.setScreen(this.lastScreen);
	}
}
