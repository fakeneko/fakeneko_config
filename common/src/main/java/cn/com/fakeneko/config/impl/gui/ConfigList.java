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

	public void refreshEntries() {
		this.allEntries.clear();
		for (ConfigCategory category : this.screen.manager().categories()) {
			this.allEntries.add(new CategoryEntry(category));
			for (Config<?> config : category.configs()) {
				if (!config.isVisible()) {
					continue;
				}
				if (!this.filter.isEmpty() && !config.name().toLowerCase().contains(this.filter) && !config.displayName().getString().toLowerCase().contains(this.filter)) {
					continue;
				}
				this.allEntries.add(new ConfigEntry(this.screen, config));
			}
		}
		this.replaceEntries(this.allEntries);
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
