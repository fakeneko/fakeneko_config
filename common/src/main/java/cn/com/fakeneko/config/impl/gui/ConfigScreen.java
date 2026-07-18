package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.api.ConfigManager;
import cn.com.fakeneko.config.impl.types.BooleanConfig;
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
	private final java.util.Map<Config<?>, Object> initialValues = new java.util.IdentityHashMap<>();
	private final java.util.Map<Config<?>, Object> pendingValues = new java.util.IdentityHashMap<>();
	private ConfigList configList;
	private EditBox searchBox;
	private Button cancelButton;
	private Button doneButton;
	private double scrollAmount;
	private ConfigCategory selectedCategory;
	private String searchFilter = "";
	private final java.util.List<Button> tabButtons = new java.util.ArrayList<>();

	public ConfigScreen(Screen lastScreen, @NotNull ConfigManager manager) {
		super(manager.displayName());
		this.lastScreen = lastScreen;
		this.manager = manager;
	}

	public ConfigManager manager() {
		return this.manager;
	}

	/**
	 * The screen to return to when leaving this config screen.
	 *
	 * @return The parent screen.
	 */
	public Screen lastScreen() {
		return this.lastScreen;
	}

	@Override
	protected void init() {
		this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, SEARCH);
		this.searchBox.setResponder(value -> {
			this.searchFilter = value;
			if (this.configList != null) {
				this.configList.setFilter(value);
			}
			this.updateTabStates();
		});
		this.searchBox.setValue(this.searchFilter);
		this.addRenderableWidget(this.searchBox);

		java.util.List<ConfigCategory> categories = new java.util.ArrayList<>(this.manager.categories());
		if (this.selectedCategory == null || !categories.contains(this.selectedCategory)) {
			this.selectedCategory = categories.isEmpty() ? null : categories.get(0);
		}
		boolean showTabs = categories.size() > 1;

		if (showTabs) {
			this.initTabs(categories);
		}

		int listY = showTabs ? 74 : 50;
		this.configList = new ConfigList(this, this.minecraft, this.width, this.height - 40 - listY, listY, 24);
		this.configList.setFilter(this.searchFilter);
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
				this.captureInitialValue(config);
				if (config instanceof BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
					this.captureInitialValue(booleanConfig.hotkey());
				}
			}
		}
		this.updateDoneButton();
	}

	private void initTabs(java.util.List<ConfigCategory> categories) {
		this.tabButtons.clear();
		int tabY = 48;
		int gap = 4;
		int totalWidth = 0;
		java.util.List<Integer> widths = new java.util.ArrayList<>();
		for (ConfigCategory category : categories) {
			int w = Math.max(60, this.font.width(category.displayName()) + 16);
			widths.add(w);
			totalWidth += w;
		}
		totalWidth += gap * (categories.size() - 1);
		int x = this.width / 2 - totalWidth / 2;
		for (int i = 0; i < categories.size(); i++) {
			ConfigCategory category = categories.get(i);
			int w = widths.get(i);
			Button tab = Button.builder(category.displayName(), b -> this.selectCategory(category))
				.bounds(x, tabY, w, 20)
				.build();
			this.tabButtons.add(tab);
			this.addRenderableWidget(tab);
			x += w + gap;
		}
		this.updateTabStates();
	}

	/**
	 * Updates tab button states. The selected tab is highlighted (inactive). While a search
	 * query is active, all tabs are disabled to make clear the results span every category.
	 */
	private void updateTabStates() {
		boolean searching = !this.searchFilter.isEmpty();
		java.util.List<ConfigCategory> categories = new java.util.ArrayList<>(this.manager.categories());
		for (int i = 0; i < this.tabButtons.size(); i++) {
			Button tab = this.tabButtons.get(i);
			ConfigCategory category = i < categories.size() ? categories.get(i) : null;
			tab.active = !searching && category != this.selectedCategory;
		}
	}

	private void selectCategory(ConfigCategory category) {
		if (category == this.selectedCategory) {
			return;
		}
		this.selectedCategory = category;
		this.searchFilter = "";
		this.scrollAmount = 0;
		this.rebuildWidgets();
	}

	/**
	 * The currently selected category tab, or null when there are no categories.
	 *
	 * @return The selected category.
	 */
	public ConfigCategory selectedCategory() {
		return this.selectedCategory;
	}

	private void captureInitialValue(Config<?> config) {
		this.initialValues.put(config, config.get());
	}

	@SuppressWarnings("unchecked")
	public <T> T getEffectiveValue(Config<T> config) {
		Object pending = this.pendingValues.get(config);
		if (pending != null) {
			return (T) pending;
		}
		return config.get();
	}

	public boolean hasPendingValue(Config<?> config) {
		return this.pendingValues.containsKey(config);
	}

	public void setPendingValue(Config<?> config, Object value) {
		Object initial = this.initialValues.get(config);
		if (initial == null) {
			this.pendingValues.put(config, value);
		} else if (this.areValuesEqual(initial, value)) {
			this.pendingValues.remove(config);
		} else {
			this.pendingValues.put(config, value);
		}
		this.configList.refreshEntry(config);
		this.updateDoneButton();
	}

	private boolean areValuesEqual(Object a, Object b) {
		if (a instanceof java.util.List<?> listA && b instanceof java.util.List<?> listB) {
			return listA.equals(listB);
		}
		return java.util.Objects.equals(a, b);
	}

	private void applyPendingValues() {
		for (java.util.Map.Entry<Config<?>, Object> entry : this.pendingValues.entrySet()) {
			this.applyPendingValue(entry.getKey(), entry.getValue());
		}
		this.pendingValues.clear();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void applyPendingValue(Config<?> config, Object value) {
		((Config) config).set(value);
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
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE) {
			this.onCancel();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		// No-op: Minecraft.setScreen() calls onClose() when opening child screens.
		// Actual close/cancel is handled by keyPressed(ESC) or the cancel button.
	}

	public void setScrollAmount(double amount) {
		this.scrollAmount = amount;
	}

	private void onDone() {
		this.applyPendingValues();
		this.pendingValues.clear();
		this.initialValues.clear();
		this.manager.save();
		this.minecraft.gui.setScreen(this.lastScreen);
	}

	private void onCancel() {
		if (this.isModifiedFromInitial()) {
			this.minecraft.gui.setScreen(new ConfirmDiscardScreen(this));
			return;
		}
		this.discardChanges();
		this.minecraft.gui.setScreen(this.lastScreen);
	}

	/**
	 * Discards all pending (unsaved) changes, resetting the screen state.
	 * The config values themselves are untouched since they are only applied on Done.
	 */
	public void discardChanges() {
		this.pendingValues.clear();
		this.initialValues.clear();
	}

	public boolean isModifiedFromInitial(Config<?> config) {
		Object initial = this.initialValues.get(config);
		if (initial == null) {
			return config.isModified();
		}
		Object current = this.getEffectiveValue(config);
		return !this.areValuesEqual(initial, current);
	}

	public boolean isModifiedFromInitial() {
		for (ConfigCategory category : this.manager.categories()) {
			for (Config<?> config : category.configs()) {
				if (this.isModifiedFromInitial(config)) {
					return true;
				}
				if (config instanceof BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
					if (this.isModifiedFromInitial(booleanConfig.hotkey())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void updateDoneButton() {
		this.doneButton.active = this.isModifiedFromInitial();
	}
}
