package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {
	private static final Component SEARCH = Component.translatable("config.fakeneko_config.search");
	private static final Component CANCEL = Component.translatable("config.fakeneko_config.cancel");
	private static final Component DONE = Component.translatable("config.fakeneko_config.done");

	private final Screen lastScreen;
	private final ConfigManager manager;
	private ConfigList configList;
	private EditBox searchBox;
	private Button cancelButton;
	private Button doneButton;
	private double scrollAmount;

	public ConfigScreen(Screen lastScreen, @NotNull ConfigManager manager) {
		super(manager.displayName());
		this.lastScreen = lastScreen;
		this.manager = manager;
	}

	public ConfigManager manager() {
		return this.manager;
	}

	@Override
	protected void init() {
		this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, SEARCH);
		this.searchBox.setResponder(value -> this.configList.setFilter(value));
		this.addRenderableWidget(this.searchBox);

		this.configList = new ConfigList(this, this.minecraft, this.width, this.height - 90, 50, 24);
		this.configList.setScrollAmount(this.scrollAmount);
		this.addRenderableWidget(this.configList);

		this.cancelButton = Button.builder(CANCEL, button -> this.onCancel())
			.bounds(this.width / 2 - 155, this.height - 30, 150, 20)
			.build();
		this.doneButton = Button.builder(DONE, button -> this.onDone())
			.bounds(this.width / 2 + 5, this.height - 30, 150, 20)
			.build();
		this.addRenderableWidget(this.cancelButton);
		this.addRenderableWidget(this.doneButton);

		for (ConfigCategory category : this.manager.categories()) {
			for (Config<?> config : category.configs()) {
				this.addModificationListener(config);
			}
		}
		this.updateDoneButton();
	}

	private <T> void addModificationListener(Config<T> config) {
		config.addListener((cfg, from, to) -> this.updateDoneButton());
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		this.setFocused(null);
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.text(Minecraft.getInstance().font, this.title, this.width / 2 - this.font.width(this.title) / 2, 8, -1);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}

	public void setScrollAmount(double amount) {
		this.scrollAmount = amount;
	}

	private void onDone() {
		this.manager.save();
		this.minecraft.gui.setScreen(this.lastScreen);
	}

	private void onCancel() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}

	private void updateDoneButton() {
		this.doneButton.active = this.manager.isModified();
	}
}
