package org.exmple.newbedwarshelper.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.exmple.newbedwarshelper.client.esp.EspBlockEntityGroup;
import org.exmple.newbedwarshelper.client.esp.EspBlockEntityGroups;
import org.exmple.newbedwarshelper.client.esp.EspBlockEntityTarget;
import org.exmple.newbedwarshelper.client.esp.EspEntityGroup;
import org.exmple.newbedwarshelper.client.esp.EspEntityGroups;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NullMarked
@SuppressWarnings("all")
public class EspWhitelistScreen extends Screen {
    private static final Component TITLE_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.title");
    private static final Component SEARCH_HINT = Component.translatable("screen.newbedwarshelper.esp_whitelist.search_hint").withStyle(EditBox.SEARCH_HINT_STYLE);
    private static final Component ENABLE_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.enable");
    private static final Component DISABLE_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.disable");
    private static final Component GROUP_ENABLE_ALL_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.group_enable_all");
    private static final Component GROUP_DISABLE_ALL_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.group_disable_all");
    private static final Component TEMP_NONE_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.temp_none");
    private static final Component TEMP_ALL_ON_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.temp_all_on");
    private static final Component TEMP_ALL_OFF_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.temp_all_off");
    private static final Component DONE_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.done");
    private static final Component RESET_TO_DEFAULTS_TEXT = Component.translatable("screen.newbedwarshelper.esp_whitelist.reset_to_defaults");
    private static final int SEARCH_WIDTH = 260;
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_TOP = 56;
    private static final int DONE_BUTTON_Y_OFFSET = 27;
    private static final int FOOTER_BUTTON_WIDTH = 150;
    private static final int DONE_BUTTON_HEIGHT = 20;
    private static final int FOOTER_BUTTON_GAP = 8;
    private static final int LIST_DONE_BUTTON_GAP = 8;
    private static final int LIST_TOP_PADDING = 0;
    private static final int LIST_BOTTOM_PADDING = 2;
    private static final int BUTTON_WIDTH = 72;
    private static final int GROUP_BUTTON_WIDTH = 124;
    private static final int HEADER_TEXT_COLOR = 0xFFE0E0E0;
    private static final int ITEM_TEXT_COLOR = 0xFFFFFFFF;

    private final Screen parent;
    private final List<Row> allRows = new ArrayList<>();
    private final List<Row> filteredRows = new ArrayList<>();
    private @Nullable EditBox searchBox;
    private @Nullable ScrollArea scrollArea;

    public EspWhitelistScreen(Minecraft minecraft, Screen parent) {
        super(minecraft, minecraft.font, TITLE_TEXT);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.allRows.clear();
        this.filteredRows.clear();
        this.scrollArea = null;

        int searchX = this.width / 2 - SEARCH_WIDTH / 2;
        this.searchBox = new EditBox(this.font, searchX, 28, SEARCH_WIDTH, 20, this.searchBox, SEARCH_HINT);
        this.searchBox.setHint(SEARCH_HINT);
        this.searchBox.setTextColor(-1);
        this.searchBox.setTextColorUneditable(-1);
        this.searchBox.setResponder(this::updateSearch);
        this.addRenderableWidget(this.searchBox);

        int footerButtonsX = this.width / 2 - (FOOTER_BUTTON_WIDTH * 2 + FOOTER_BUTTON_GAP) / 2;
        this.addRenderableWidget(Button.builder(RESET_TO_DEFAULTS_TEXT, ignored -> EspTargetStorage.resetWhitelistToDefaults())
                .bounds(footerButtonsX, this.doneButtonY(), FOOTER_BUTTON_WIDTH, DONE_BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(DONE_TEXT, ignored -> this.onClose())
                .bounds(footerButtonsX + FOOTER_BUTTON_WIDTH + FOOTER_BUTTON_GAP, this.doneButtonY(), FOOTER_BUTTON_WIDTH, DONE_BUTTON_HEIGHT)
                .build());

        int rowWidth = Math.min(360, this.width - 40);
        int listHeight = Math.max(1, this.listBottom() - ROW_TOP);
        this.scrollArea = this.addRenderableOnly(new ScrollArea(
                this.width / 2 - rowWidth / 2,
                ROW_TOP,
                rowWidth,
                listHeight
        ));
        this.scrollArea.active = false;

        this.buildRows();
        this.updateSearch(this.searchBox.getValue());
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(graphics);
        this.updateRowVisibilityAndPositions();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, TITLE_TEXT, this.width / 2, 10, -1);
        this.drawListSeparators(graphics);
        this.drawVisibleRows(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.scrollArea != null && this.scrollArea.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            this.updateRowVisibilityAndPositions();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (this.scrollArea != null && this.scrollArea.updateScrolling(event)) {
            return true;
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.scrollArea != null && this.scrollArea.mouseDragged(event, deltaX, deltaY)) {
            this.updateRowVisibilityAndPositions();
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.scrollArea != null) {
            this.scrollArea.onRelease(event);
        }
        return super.mouseReleased(event);
    }

    private void updateSearch(String query) {
        this.filteredRows.clear();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);

        for (Row row : this.allRows) {
            if (normalizedQuery.isEmpty()) {
                this.filteredRows.add(row);
            } else if (row.searchText.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                this.filteredRows.add(row);
            }
        }
        if (this.scrollArea != null) {
            this.scrollArea.refreshScrollAmount();
            this.scrollArea.setScrollAmount(0.0D);
        }
        this.updateRowVisibilityAndPositions();
    }

    private void updateRowVisibilityAndPositions() {
        int rowWidth = Math.min(360, this.width - 40);
        double scrollAmount = this.scrollArea == null ? 0.0D : this.scrollArea.scrollAmount();
        int listTop = ROW_TOP + LIST_TOP_PADDING;
        int listBottom = this.listBottom() - LIST_BOTTOM_PADDING;
        int start = Math.max(0, (int) (scrollAmount / ROW_HEIGHT));
        int end = Math.min(this.filteredRows.size(), (int) Math.ceil((scrollAmount + (listBottom - listTop)) / ROW_HEIGHT) + 1);

        for (Row row : this.allRows) {
            if (row.enableButton != null) {
                row.enableButton.visible = false;
            }
            if (row.disableButton != null) {
                row.disableButton.visible = false;
            }
            if (row.groupToggleButton != null) {
                row.groupToggleButton.visible = false;
            }
            if (row.tempToggleButton != null) {
                row.tempToggleButton.visible = false;
            }
        }

        for (int i = start; i < end; i++) {
            Row row = this.filteredRows.get(i);
            int x = this.width / 2 - rowWidth / 2;
            int y = ROW_TOP + i * ROW_HEIGHT - (int) scrollAmount;
            int right = x + rowWidth;

            boolean inBounds = y >= listTop && (y + ROW_HEIGHT) <= listBottom;
            if (!inBounds) {
                continue;
            }

            if (row.enableButton != null && row.disableButton != null) {
                int disableX = right - BUTTON_WIDTH;
                int enableX = disableX - 4 - BUTTON_WIDTH;

                row.enableButton.setX(enableX);
                row.enableButton.setY(y - 2);
                row.disableButton.setX(disableX);
                row.disableButton.setY(y - 2);

                row.enableButton.visible = true;
                row.disableButton.visible = true;
                row.refreshButtons();
            } else if (row.groupToggleButton != null && row.tempToggleButton != null) {
                int totalButtonWidth = GROUP_BUTTON_WIDTH * 2 + 6;
                int groupX = x + (rowWidth - totalButtonWidth) / 2;
                int tempX = groupX + GROUP_BUTTON_WIDTH + 6;

                row.groupToggleButton.setX(groupX);
                row.groupToggleButton.setY(y - 2);
                row.tempToggleButton.setX(tempX);
                row.tempToggleButton.setY(y - 2);

                row.groupToggleButton.visible = true;
                row.tempToggleButton.visible = true;
                row.refreshButtons();
            }
        }
    }

    private void buildRows() {
        this.allRows.clear();

        for (EspEntityGroup group : EspEntityGroups.ALL) {
            addGroup(group);
        }
        for (EspBlockEntityGroup group : EspBlockEntityGroups.ALL) {
            addBlockEntityGroup(group);
        }
    }

    private void addGroup(EspEntityGroup group) {
        Component groupTitle = Component.translatable(group.titleKey());
        Row header = Row.header(groupTitle);
        this.allRows.add(header);
        Row groupControls = Row.groupControls(group, groupTitle.getString());
        this.allRows.add(groupControls);
        this.addRenderableWidget(groupControls.groupToggleButton);
        this.addRenderableWidget(groupControls.tempToggleButton);

        boolean addedBoatRaftRow = false;
        for (EntityType<?> entityType : group.entityTypes()) {
            if (group == EspEntityGroups.MISC && EspEntityGroups.isBoatRaftType(entityType)) {
                if (!addedBoatRaftRow) {
                    Component label = Component.translatable("entity.newbedwarshelper.boat_raft");
                    Row row = Row.entityTypes(EspEntityGroups.BOAT_RAFT_TYPES, label, "boat raft boat_raft");
                    this.allRows.add(row);
                    this.addRenderableWidget(row.enableButton);
                    this.addRenderableWidget(row.disableButton);
                    addedBoatRaftRow = true;
                }
                continue;
            }

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            String idPath = id.getPath();
            Component label = Component.translatable("entity.newbedwarshelper." + idPath);
            Row row = Row.entity(entityType, label, idPath);
            this.allRows.add(row);
            this.addRenderableWidget(row.enableButton);
            this.addRenderableWidget(row.disableButton);

            if (group == EspEntityGroups.MISC && entityType == EntityTypes.WITHER_SKULL) {
                Component dangerousLabel = Component.translatable("entity.newbedwarshelper.wither_skull_dangerous");
                Row dangerousRow = Row.dangerousWitherSkull(dangerousLabel);
                this.allRows.add(dangerousRow);
                this.addRenderableWidget(dangerousRow.enableButton);
                this.addRenderableWidget(dangerousRow.disableButton);
            }
        }
    }

    private void addBlockEntityGroup(EspBlockEntityGroup group) {
        Component groupTitle = Component.translatable(group.titleKey());
        Row header = Row.header(groupTitle);
        this.allRows.add(header);
        Row groupControls = Row.blockEntityGroupControls(group, groupTitle.getString());
        this.allRows.add(groupControls);
        this.addRenderableWidget(groupControls.groupToggleButton);
        this.addRenderableWidget(groupControls.tempToggleButton);

        for (EspBlockEntityTarget target : group.targets()) {
            Component label = Component.translatable(target.translationKey());
            Row row = Row.blockEntityTarget(target, label);
            this.allRows.add(row);
            this.addRenderableWidget(row.enableButton);
            this.addRenderableWidget(row.disableButton);
        }
    }

    private void drawVisibleRows(GuiGraphicsExtractor graphics) {
        int rowWidth = Math.min(360, this.width - 40);
        double scrollAmount = this.scrollArea == null ? 0.0D : this.scrollArea.scrollAmount();
        int listTop = ROW_TOP + LIST_TOP_PADDING;
        int listBottom = this.listBottom() - LIST_BOTTOM_PADDING;
        int start = Math.max(0, (int) (scrollAmount / ROW_HEIGHT));
        int end = Math.min(this.filteredRows.size(), (int) Math.ceil((scrollAmount + (listBottom - listTop)) / ROW_HEIGHT) + 1);

        for (int i = start; i < end; i++) {
            Row row = this.filteredRows.get(i);
            int x = this.width / 2 - rowWidth / 2;
            int y = ROW_TOP + i * ROW_HEIGHT - (int) scrollAmount;

            if (y < listTop || (y + ROW_HEIGHT) > listBottom) {
                continue;
            }

            if (row.isHeader) {
                int textWidth = this.font.width(row.label);
                int centerX = this.width / 2 - textWidth / 2;
                graphics.text(this.font, row.label, centerX, y + 6, HEADER_TEXT_COLOR);
            } else {
                graphics.text(this.font, row.label, x, y + 6, ITEM_TEXT_COLOR);
            }
        }
    }

    private void drawListSeparators(GuiGraphicsExtractor graphics) {
        Identifier headerSeparator = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        Identifier footerSeparator = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        int listTop = ROW_TOP + LIST_TOP_PADDING;
        int listBottom = this.listBottom();

        graphics.blit(RenderPipelines.GUI_TEXTURED, headerSeparator, 0, listTop - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, footerSeparator, 0, listBottom, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    private int doneButtonY() {
        return this.height - DONE_BUTTON_Y_OFFSET;
    }

    private int listBottom() {
        return this.doneButtonY() - LIST_DONE_BUTTON_GAP;
    }

    private static final class Row {
        private final boolean isHeader;
        private final Component label;
        private final String searchText;
        private final EntityType<?> entityType;
        private final List<EntityType<?>> entityTypes;
        private final boolean dangerousWitherSkull;
        private final EspBlockEntityTarget blockEntityTarget;
        private final EspEntityGroup group;
        private final EspBlockEntityGroup blockEntityGroup;
        private final Button enableButton;
        private final Button disableButton;
        private final Button groupToggleButton;
        private final Button tempToggleButton;

        private Row(boolean isHeader, Component label, String searchText, EntityType<?> entityType, List<EntityType<?>> entityTypes, boolean dangerousWitherSkull, EspBlockEntityTarget blockEntityTarget, EspEntityGroup group, EspBlockEntityGroup blockEntityGroup, Button enableButton, Button disableButton, Button groupToggleButton, Button tempToggleButton) {
            this.isHeader = isHeader;
            this.label = label;
            this.searchText = searchText;
            this.entityType = entityType;
            this.entityTypes = entityTypes;
            this.dangerousWitherSkull = dangerousWitherSkull;
            this.blockEntityTarget = blockEntityTarget;
            this.group = group;
            this.blockEntityGroup = blockEntityGroup;
            this.enableButton = enableButton;
            this.disableButton = disableButton;
            this.groupToggleButton = groupToggleButton;
            this.tempToggleButton = tempToggleButton;
        }

        private static Row header(Component label) {
            return new Row(true, label, label.getString(), null, null, false, null, null, null, null, null, null, null);
        }

        private static Row groupControls(EspEntityGroup group, String groupName) {
            Button groupToggleButton = Button.builder(Component.empty(), ignored -> EspTargetStorage.applyNextGroupToggleAction(group.entityTypes()))
                    .bounds(0, 0, GROUP_BUTTON_WIDTH, 20)
                    .build();
            Button tempToggleButton = Button.builder(Component.empty(), ignored -> EspTargetStorage.cycleGroupTempToggleMode(group.entityTypes()))
                    .bounds(0, 0, GROUP_BUTTON_WIDTH, 20)
                    .build();
            return new Row(false, Component.empty(), groupName, null, null, false, null, group, null, null, null, groupToggleButton, tempToggleButton);
        }

        private static Row blockEntityGroupControls(EspBlockEntityGroup group, String groupName) {
            Button groupToggleButton = Button.builder(Component.empty(), ignored -> EspTargetStorage.applyNextBlockEntityGroupToggleAction(group.targets()))
                    .bounds(0, 0, GROUP_BUTTON_WIDTH, 20)
                    .build();
            Button tempToggleButton = Button.builder(Component.empty(), ignored -> EspTargetStorage.cycleBlockEntityGroupTempToggleMode(group.targets()))
                    .bounds(0, 0, GROUP_BUTTON_WIDTH, 20)
                    .build();
            return new Row(false, Component.empty(), groupName, null, null, false, null, null, group, null, null, groupToggleButton, tempToggleButton);
        }

        private static Row entity(EntityType<?> entityType, Component label, String idPath) {
            Button enableButton = Button.builder(ENABLE_TEXT, ignored -> EspTargetStorage.setEntityTypeIspEnabled(entityType, true))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            Button disableButton = Button.builder(DISABLE_TEXT, ignored -> EspTargetStorage.setEntityTypeIspEnabled(entityType, false))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            String searchText = idPath + " " + label.getString();
            return new Row(false, label, searchText, entityType, null, false, null, null, null, enableButton, disableButton, null, null);
        }

        private static Row entityTypes(List<EntityType<?>> entityTypes, Component label, String searchText) {
            Button enableButton = Button.builder(ENABLE_TEXT, ignored -> EspTargetStorage.setEntityTypesIspEnabled(entityTypes, true))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            Button disableButton = Button.builder(DISABLE_TEXT, ignored -> EspTargetStorage.setEntityTypesIspEnabled(entityTypes, false))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            return new Row(false, label, searchText + " " + label.getString(), null, entityTypes, false, null, null, null, enableButton, disableButton, null, null);
        }

        private static Row dangerousWitherSkull(Component label) {
            Button enableButton = Button.builder(ENABLE_TEXT, ignored -> EspTargetStorage.setDangerousWitherSkullIspEnabled(true))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            Button disableButton = Button.builder(DISABLE_TEXT, ignored -> EspTargetStorage.setDangerousWitherSkullIspEnabled(false))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            String searchText = "wither_skull dangerous " + label.getString();
            return new Row(false, label, searchText, null, null, true, null, null, null, enableButton, disableButton, null, null);
        }

        private static Row blockEntityTarget(EspBlockEntityTarget target, Component label) {
            Button enableButton = Button.builder(ENABLE_TEXT, ignored -> EspTargetStorage.setBlockEntityTargetIspEnabled(target, true))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            Button disableButton = Button.builder(DISABLE_TEXT, ignored -> EspTargetStorage.setBlockEntityTargetIspEnabled(target, false))
                    .bounds(0, 0, BUTTON_WIDTH, 20)
                    .build();
            String searchText = target.id() + " " + label.getString();
            return new Row(false, label, searchText, null, null, false, target, null, null, enableButton, disableButton, null, null);
        }

        private void refreshButtons() {
            if (this.entityType != null) {
                boolean enabled = EspTargetStorage.isEntityTypePersistentlyIspEnabled(this.entityType);
                this.enableButton.active = !enabled;
                this.disableButton.active = enabled;
            } else if (this.entityTypes != null) {
                boolean anyEnabled = false;
                boolean allEnabled = true;
                for (EntityType<?> type : this.entityTypes) {
                    boolean enabled = EspTargetStorage.isEntityTypePersistentlyIspEnabled(type);
                    anyEnabled |= enabled;
                    allEnabled &= enabled;
                }
                this.enableButton.active = !allEnabled;
                this.disableButton.active = anyEnabled;
            } else if (this.dangerousWitherSkull) {
                boolean enabled = EspTargetStorage.isDangerousWitherSkullPersistentlyIspEnabled();
                this.enableButton.active = !enabled;
                this.disableButton.active = enabled;
            } else if (this.blockEntityTarget != null) {
                boolean enabled = EspTargetStorage.isBlockEntityTargetPersistentlyIspEnabled(this.blockEntityTarget);
                this.enableButton.active = !enabled;
                this.disableButton.active = enabled;
            } else if (this.group != null) {
                EspTargetStorage.GroupToggleAction action = EspTargetStorage.getNextGroupToggleAction(this.group.entityTypes());
                this.groupToggleButton.setMessage(action == EspTargetStorage.GroupToggleAction.ENABLE_ALL ? GROUP_ENABLE_ALL_TEXT : GROUP_DISABLE_ALL_TEXT);
                this.tempToggleButton.setMessage(getTempToggleText(EspTargetStorage.getGroupTempToggleMode(this.group.entityTypes())));
            } else if (this.blockEntityGroup != null) {
                EspTargetStorage.GroupToggleAction action = EspTargetStorage.getNextBlockEntityGroupToggleAction(this.blockEntityGroup.targets());
                this.groupToggleButton.setMessage(action == EspTargetStorage.GroupToggleAction.ENABLE_ALL ? GROUP_ENABLE_ALL_TEXT : GROUP_DISABLE_ALL_TEXT);
                this.tempToggleButton.setMessage(getTempToggleText(EspTargetStorage.getBlockEntityGroupTempToggleMode(this.blockEntityGroup.targets())));
            }
        }

        private static Component getTempToggleText(EspTargetStorage.TempToggleMode mode) {
            return switch (mode) {
                case NONE -> TEMP_NONE_TEXT;
                case ALL_ON -> TEMP_ALL_ON_TEXT;
                case ALL_OFF -> TEMP_ALL_OFF_TEXT;
            };
        }
    }

    private final class ScrollArea extends AbstractScrollArea {
        private ScrollArea(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(ROW_HEIGHT));
        }

        @Override
        protected int contentHeight() {
            return EspWhitelistScreen.this.filteredRows.size() * ROW_HEIGHT + LIST_BOTTOM_PADDING;
        }

        @Override
        protected double scrollRate() {
            return ROW_HEIGHT;
        }

        @Override
        protected int scrollBarX() {
            return EspWhitelistScreen.this.width - scrollbarWidth() - 8;
        }

        @Override
        public int scrollbarWidth() {
            return AbstractScrollArea.SCROLLBAR_WIDTH;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            this.extractScrollbar(graphics, mouseX, mouseY);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
}
