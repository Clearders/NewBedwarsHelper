package org.exmple.newbedwarshelper.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.exmple.newbedwarshelper.ModConstants;

import java.net.URI;

public class ModScreen extends Screen {
    private static final int BUTTON_WIDTH_FULL = 204;
    private static final int BUTTON_WIDTH_HALF = 98;
    private static final Component OPEN_CONFIG_TEXT = Component.translatable("screen.newbedwarshelper.mod.open_config");
    private static final Component SOURCE_TEXT = Component.translatable("screen.newbedwarshelper.mod.source");
    private static final Component REPORT_BUGS_TEXT = Component.translatable("screen.newbedwarshelper.mod.report_bugs");
    private static final Component WEBSITE_TEXT = Component.translatable("screen.newbedwarshelper.mod.website");
    private static final Component MODRINTH_TEXT = Component.translatable("screen.newbedwarshelper.mod.modrinth");
    private static final Component DONE_TEXT = Component.translatable("screen.newbedwarshelper.mod.done");
    private static final URI SOURCE_URL = URI.create("https://not.completed.yet");
    private static final URI REPORT_BUGS_URL = URI.create("https://not.completed.yet");
    private static final URI WEBSITE_URL = URI.create("https://not.completed.yet");
    private static final URI MODRINTH_URL = URI.create("https://not.completed.yet");

    private final Screen parent;

    public ModScreen(Minecraft minecraft, Screen parent) {
        super(minecraft, minecraft.font, createTitle());
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper helper = gridLayout.createRowHelper(2);
        helper.addChild(Button.builder(OPEN_CONFIG_TEXT, button -> this.minecraft.gui.setScreen(new ConfigMenuScreen(this.minecraft, this)))
                .width(BUTTON_WIDTH_FULL)
                .build(), 2, gridLayout.newCellSettings().paddingTop(50));
        helper.addChild(openLinkButton(SOURCE_TEXT, SOURCE_URL));
        helper.addChild(openLinkButton(REPORT_BUGS_TEXT, REPORT_BUGS_URL));
        helper.addChild(openLinkButton(WEBSITE_TEXT, WEBSITE_URL));
        helper.addChild(openLinkButton(MODRINTH_TEXT, MODRINTH_URL));
        helper.addChild(Button.builder(DONE_TEXT, button -> this.onClose())
                .width(BUTTON_WIDTH_FULL)
                .build(), 2);
        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
        gridLayout.visitWidgets(this::addRenderableWidget);

        int textWidth = this.font.width(this.title);
        this.addRenderableWidget(new StringWidget(this.width / 2 - textWidth / 2, 40, textWidth, 9, this.title, this.font));
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private Button openLinkButton(Component message, URI link) {
        return Button.builder(message, ConfirmLinkScreen.confirmLink(this, link))
                .width(BUTTON_WIDTH_HALF)
                .build();
    }

    private static Component createTitle() {
        return Component.translatable("screen.newbedwarshelper.mod.title", getModVersion());
    }

    private static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(ModConstants.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
