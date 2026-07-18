package cn.com.fakeneko.config.impl.gui;

import cn.com.fakeneko.config.api.Config;
import cn.com.fakeneko.config.api.ConfigCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {
	private final ConfigScreen screen;
	private final List<Entry> allEntries = new ArrayList<>();
	private String filter = "";
	private double currentScroll;

	public ConfigList(ConfigScreen screen, Minecraft minecraft, int width, int height, int y, int itemHeight) {
		super(minecraft, width, height, y, itemHeight);
		this.screen = screen;
		this.refreshEntries();
	}

	public void setFilter(String filter) {
		this.filter = filter == null ? "" : filter.toLowerCase();
		this.refreshEntries();
	}

	public void refreshEntry(Config<?> config) {
		for (Entry entry : this.allEntries) {
			if (entry instanceof ConfigEntry configEntry && configEntry.config() == config) {
				configEntry.refresh();
				return;
			}
		}
	}

	public void refreshEntries() {
		this.allEntries.clear();
		boolean searching = !this.filter.isEmpty();
		for (ConfigCategory category : this.screen.manager().categories()) {
			// When not searching, only show the selected category tab's configs.
			if (!searching && category != this.screen.selectedCategory()) {
				continue;
			}
			java.util.List<ConfigEntry> matched = new java.util.ArrayList<>();
			for (Config<?> config : category.configs()) {
				if (!config.isVisible()) {
					continue;
				}
				if (searching && !this.matchesFilter(config)) {
					continue;
				}
				matched.add(new ConfigEntry(this.screen, config));
			}
			// Only add the category header when searching (to label cross-category results)
			// and the category has at least one match. In tab mode the header is redundant.
			if (!matched.isEmpty()) {
				if (searching) {
					this.allEntries.add(new CategoryEntry(category));
				}
				this.allEntries.addAll(matched);
			}
		}
		this.replaceEntries(this.allEntries);
	}

	/**
	 * Whether a config matches the current search filter. A {@link cn.com.fakeneko.config.impl.types.BooleanConfig}
	 * also matches when its embedded (invisible) hotkey sub-config matches, so users can find it.
	 */
	private boolean matchesFilter(Config<?> config) {
		if (config.name().toLowerCase(java.util.Locale.ROOT).contains(this.filter)
			|| config.displayName().getString().toLowerCase(java.util.Locale.ROOT).contains(this.filter)) {
			return true;
		}
		if (config instanceof cn.com.fakeneko.config.impl.types.BooleanConfig booleanConfig && booleanConfig.hotkey() != null) {
			cn.com.fakeneko.config.impl.types.HotkeyConfig hotkey = booleanConfig.hotkey();
			return hotkey.name().toLowerCase(java.util.Locale.ROOT).contains(this.filter)
				|| hotkey.displayName().getString().toLowerCase(java.util.Locale.ROOT).contains(this.filter);
		}
		return false;
	}

	@Override
	public int getRowWidth() {
		return this.width - 20;
	}

	@Override
	protected int scrollBarX() {
		return this.getRight() - 6;
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		this.screen.setScrollAmount(amount);
	}

	public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
		public abstract void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a);
	}

	public static class CategoryEntry extends Entry {
		private final ConfigCategory category;
		private final List<GuiEventListener> children = java.util.Collections.emptyList();

		public CategoryEntry(ConfigCategory category) {
			this.category = category;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.children;
		}

		@Override
		public List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
			return java.util.Collections.emptyList();
		}

		void updateNarration(NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, this.category.displayName());
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			graphics.text(Minecraft.getInstance().font, this.category.displayName(), this.getX() + 5, this.getY() + 5, -1);
		}
	}
}
