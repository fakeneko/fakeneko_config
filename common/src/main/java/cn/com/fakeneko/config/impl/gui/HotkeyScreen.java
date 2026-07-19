package cn.com.fakeneko.config.impl.gui;

import com.mojang.blaze3d.platform.InputConstants;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.HotkeyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
		this.keyButton = this.addRenderableWidget(Button.builder(
			this.formatRecording(),
			button -> {}
		).bounds(this.width / 2 - 75, this.height / 2 - 30, 150, 20).build());

		this.resetButton = this.addRenderableWidget(Button.builder(RESET, button -> this.onReset())
			.bounds(this.width / 2 - 75, this.height / 2 + 10, 150, 20).build());
		this.updateResetButton();

		this.doneButton = this.addRenderableWidget(Button.builder(DONE, button -> this.onClose())
			.bounds(this.width / 2 - 75, this.height / 2 + 40, 150, 20).build());
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (event.key() == InputConstants.KEY_ESCAPE) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		}
		if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
			this.onClose();
			return true;
		}
		if (this.recordingState) {
			InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(event.key());
			this.recordKey(key);
		}
		return true;
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		if (this.doneButton.isMouseOver(event.x(), event.y()) || this.resetButton.isMouseOver(event.x(), event.y())) {
			return super.mouseClicked(event, doubleClick);
		}
		if (this.keyButton.isMouseOver(event.x(), event.y())) {
			if (this.recordingState) {
				this.recordKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
			} else {
				this.startRecording();
			}
			return super.mouseClicked(event, doubleClick);
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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.text(Minecraft.getInstance().font, this.title, this.width / 2 - this.font.width(this.title) / 2, 20, -1);
		graphics.text(Minecraft.getInstance().font, PRESS, this.width / 2 - this.font.width(PRESS) / 2, this.height / 2 - 55, 0xFFAAAAAA);
		graphics.text(Minecraft.getInstance().font, ESC_HINT, this.width / 2 - this.font.width(ESC_HINT) / 2, this.height / 2 - 43, 0xFFAAAAAA);
		for (GuiEventListener child : this.children()) {
			if (child instanceof net.minecraft.client.gui.components.Renderable renderable) {
				renderable.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}

	@Override
	public void onClose() {
		this.onComplete.accept(new InputKeys(this.recording));
		this.minecraft.setScreen(this.lastScreen);
	}
}
