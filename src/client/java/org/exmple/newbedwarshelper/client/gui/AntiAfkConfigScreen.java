package org.exmple.newbedwarshelper.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.exmple.newbedwarshelper.client.antiafk.AntiAFKManager;

public class AntiAfkConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.newbedwarshelper.anti_afk.title");
    private static final Component FEATURE_ENABLED_TEXT = Component.translatable("screen.newbedwarshelper.anti_afk.feature.enabled");
    private static final Component FEATURE_DISABLED_TEXT = Component.translatable("screen.newbedwarshelper.anti_afk.feature.disabled");
    private static final Component ICON_SMALL_TEXT = Component.translatable("screen.newbedwarshelper.anti_afk.icon.small");
    private static final Component ICON_LARGE_TEXT = Component.translatable("screen.newbedwarshelper.anti_afk.icon.large");
    private static final Component DONE_TEXT = Component.translatable("screen.newbedwarshelper.anti_afk.done");
    private static final int BUTTON_WIDTH = 150;

    private final Screen parent;

    public AntiAfkConfigScreen(Minecraft minecraft, Screen parent) {
        super(minecraft, minecraft.font, TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper helper = gridLayout.createRowHelper(2);
        helper.addChild(CycleButton.builder(AntiAfkConfigScreen::featureText, AntiAFKManager.isFeatureEnabled())
                .withValues(Boolean.TRUE, Boolean.FALSE)
                .displayOnlyValue()
                .create(FEATURE_ENABLED_TEXT, (button, enabled) -> AntiAFKManager.setFeatureEnabled(enabled)), gridLayout.newCellSettings().paddingTop(50));
        helper.addChild(CycleButton.builder(AntiAfkConfigScreen::iconText, AntiAFKManager.isSmallIcon())
                .withValues(Boolean.TRUE, Boolean.FALSE)
                .displayOnlyValue()
                .create(ICON_SMALL_TEXT, (button, smallIcon) -> AntiAFKManager.setSmallIcon(smallIcon)), gridLayout.newCellSettings().paddingTop(50));
        helper.addChild(Button.builder(DONE_TEXT, button -> this.onClose())
                .width(BUTTON_WIDTH)
                .build(), 2, gridLayout.newCellSettings().alignHorizontallyCenter());
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

    private static Component featureText(boolean enabled) {
        return enabled ? FEATURE_ENABLED_TEXT : FEATURE_DISABLED_TEXT;
    }

    private static Component iconText(boolean smallIcon) {
        return smallIcon ? ICON_SMALL_TEXT : ICON_LARGE_TEXT;
    }
}
