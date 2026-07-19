package org.exmple.newbedwarshelper.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class StatsFetcherConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.newbedwarshelper.stats_fetcher.title");
    private static final Component SHOW_DANGEROUS_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_dangerous_players.on");
    private static final Component SHOW_DANGEROUS_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_dangerous_players.off");
    private static final Component SHOW_FINAL_KD_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_final_kd.on");
    private static final Component SHOW_FINAL_KD_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_final_kd.off");
    private static final Component SHOW_DOUBLES_FINAL_KD_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_doubles_final_kd.on");
    private static final Component SHOW_DOUBLES_FINAL_KD_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_doubles_final_kd.off");
    private static final Component SHOW_QUADS_FINAL_KD_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_quads_final_kd.on");
    private static final Component SHOW_QUADS_FINAL_KD_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_quads_final_kd.off");
    private static final Component SHOW_TOTAL_WINS_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_total_wins.on");
    private static final Component SHOW_TOTAL_WINS_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_total_wins.off");
    private static final Component SKIP_OWN_TEAM_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.skip_own_team.on");
    private static final Component SKIP_OWN_TEAM_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.skip_own_team.off");
    private static final Component AUTO_WEBALL_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.auto_weball.on");
    private static final Component AUTO_WEBALL_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.auto_weball.off");
    private static final Component SHOW_COPY_BUTTONS_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_copy_buttons.on");
    private static final Component SHOW_COPY_BUTTONS_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.show_copy_buttons.off");
    private static final Component COPY_TEXT_IN_ENGLISH_ON = Component.translatable("screen.newbedwarshelper.stats_fetcher.copy_text_in_english.on");
    private static final Component COPY_TEXT_IN_ENGLISH_OFF = Component.translatable("screen.newbedwarshelper.stats_fetcher.copy_text_in_english.off");
    private static final Component DONE_TEXT = Component.translatable("screen.newbedwarshelper.stats_fetcher.done");
    private static final int BUTTON_WIDTH = 150;

    private final Screen parent;

    public StatsFetcherConfigScreen(Minecraft minecraft, Screen parent) {
        super(minecraft, minecraft.font, TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper helper = gridLayout.createRowHelper(2);
        ModConfig.StatsFetcherConfig config = ModConfig.getInstance().statsFetcher;

        helper.addChild(createToggleButton(StatsFetcherConfigScreen::dangerousPlayersText, config.showDangerousPlayers,
                enabled -> updateConfig(cfg -> cfg.showDangerousPlayers = enabled)), gridLayout.newCellSettings().paddingTop(50));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::finalKdText, config.showFinalKD,
                enabled -> updateConfig(cfg -> cfg.showFinalKD = enabled)), gridLayout.newCellSettings().paddingTop(50));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::doublesFinalKdText, config.showDoublesFinalKD,
                enabled -> updateConfig(cfg -> cfg.showDoublesFinalKD = enabled)), gridLayout.newCellSettings().paddingTop(10));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::quadsFinalKdText, config.showQuadsFinalKD,
                enabled -> updateConfig(cfg -> cfg.showQuadsFinalKD = enabled)), gridLayout.newCellSettings().paddingTop(10));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::totalWinsText, config.showTotalWins,
                enabled -> updateConfig(cfg -> cfg.showTotalWins = enabled)), gridLayout.newCellSettings().paddingTop(10));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::skipOwnTeamText, config.skipOwnTeamInGame,
                enabled -> updateConfig(cfg -> cfg.skipOwnTeamInGame = enabled)), gridLayout.newCellSettings().paddingTop(10));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::autoWeballText, config.autoWeballOnGameStart,
                enabled -> updateConfig(cfg -> cfg.autoWeballOnGameStart = enabled)), gridLayout.newCellSettings().paddingTop(10));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::copyButtonsText, config.copyButtonsEnabled,
                enabled -> updateConfig(cfg -> cfg.copyButtonsEnabled = enabled)), gridLayout.newCellSettings().paddingTop(10));
        helper.addChild(createToggleButton(StatsFetcherConfigScreen::copyTextInEnglishText, config.copyTextInEnglish,
                enabled -> updateConfig(cfg -> cfg.copyTextInEnglish = enabled)), gridLayout.newCellSettings().paddingTop(10));
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

    private static CycleButton<Boolean> createToggleButton(Function<Boolean, Component> valueText, boolean initialValue, Consumer<Boolean> onChange) {
        return CycleButton.builder(valueText, initialValue)
                .withValues(Boolean.TRUE, Boolean.FALSE)
                .displayOnlyValue()
                .create(Component.empty(), (button, enabled) -> onChange.accept(enabled));
    }

    private static void updateConfig(Consumer<ModConfig.StatsFetcherConfig> updater) {
        ModConfig config = ModConfig.getInstance();
        updater.accept(config.statsFetcher);
        config.save();
    }

    private static Component dangerousPlayersText(boolean enabled) {
        return enabled ? SHOW_DANGEROUS_ON : SHOW_DANGEROUS_OFF;
    }

    private static Component finalKdText(boolean enabled) {
        return enabled ? SHOW_FINAL_KD_ON : SHOW_FINAL_KD_OFF;
    }

    private static Component doublesFinalKdText(boolean enabled) {
        return enabled ? SHOW_DOUBLES_FINAL_KD_ON : SHOW_DOUBLES_FINAL_KD_OFF;
    }

    private static Component quadsFinalKdText(boolean enabled) {
        return enabled ? SHOW_QUADS_FINAL_KD_ON : SHOW_QUADS_FINAL_KD_OFF;
    }

    private static Component totalWinsText(boolean enabled) {
        return enabled ? SHOW_TOTAL_WINS_ON : SHOW_TOTAL_WINS_OFF;
    }

    private static Component skipOwnTeamText(boolean enabled) {
        return enabled ? SKIP_OWN_TEAM_ON : SKIP_OWN_TEAM_OFF;
    }

    private static Component autoWeballText(boolean enabled) {
        return enabled ? AUTO_WEBALL_ON : AUTO_WEBALL_OFF;
    }

    private static Component copyButtonsText(boolean enabled) {
        return enabled ? SHOW_COPY_BUTTONS_ON : SHOW_COPY_BUTTONS_OFF;
    }

    private static Component copyTextInEnglishText(boolean enabled) {
        return enabled ? COPY_TEXT_IN_ENGLISH_ON : COPY_TEXT_IN_ENGLISH_OFF;
    }
}
