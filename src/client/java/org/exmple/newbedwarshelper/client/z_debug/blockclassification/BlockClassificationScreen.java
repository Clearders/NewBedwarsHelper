package org.exmple.newbedwarshelper.client.z_debug.blockclassification;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BlockClassificationScreen extends Screen {
    private static final Component TITLE = Component.literal("Block Classification Debugger");
    private static final Component SEARCH_HINT = Component.literal("Search...").withStyle(EditBox.SEARCH_HINT_STYLE);
    private static final int ROW_HEIGHT = 30;
    private static final int HEADER_TOP = 14;
    private static final int SEARCH_TOP = 42;
    private static final int LIST_TOP = 68;
    private static final int FOOTER_HEIGHT = 36;
    private static final int GAP = 12;
    private static final int ITEM_TEXT_COLOR = 0xFFFFFFFF;
    private static final int SUBTITLE_TEXT_COLOR = 0xFF9A9A9A;
    private static final int STATUS_TEXT_COLOR = 0xFFB8B8B8;

    private final Screen parent;
    private final List<ClassifiedBlockEntry> candidates;
    private final Map<String, ClassifiedBlockEntry> entriesById = new HashMap<>();
    private final BlockClassificationData data;
    private final List<ClassifiedBlockEntry> unclassifiedEntries = new ArrayList<>();
    private final List<ClassifiedBlockEntry> categoryEntries = new ArrayList<>();
    private BlockClassificationCategory category = BlockClassificationCategory.ORES;
    private String leftSearch = "";
    private String rightSearch = "";
    private EditBox leftSearchBox;
    private EditBox rightSearchBox;
    private BlockListArea leftList;
    private BlockListArea rightList;
    private Button saveButton;
    private Component status = Component.empty();

    public BlockClassificationScreen(Minecraft minecraft, Screen parent) {
        super(minecraft, minecraft.font, TITLE);
        this.parent = parent;
        this.candidates = BlockClassificationCandidateCollector.collectOrdinaryBlockCandidates();
        for (ClassifiedBlockEntry candidate : this.candidates) {
            this.entriesById.put(candidate.id(), candidate);
        }
        this.data = BlockClassificationStore.loadOrCreate(this.candidates);
        this.refreshLists();
    }

    @Override
    protected void init() {
        int listWidth = this.listWidth();
        int leftX = this.leftX();
        int rightX = this.rightX();
        int listHeight = this.listBottom() - LIST_TOP;

        this.leftSearchBox = new EditBox(this.font, leftX, SEARCH_TOP, listWidth, 20, null, SEARCH_HINT);
        this.leftSearchBox.setHint(SEARCH_HINT);
        this.leftSearchBox.setValue(this.leftSearch);
        this.leftSearchBox.setResponder(query -> {
            this.leftSearch = query;
            this.refreshLists();
        });
        this.addRenderableWidget(this.leftSearchBox);

        this.rightSearchBox = new EditBox(this.font, rightX, SEARCH_TOP, listWidth, 20, null, SEARCH_HINT);
        this.rightSearchBox.setHint(SEARCH_HINT);
        this.rightSearchBox.setValue(this.rightSearch);
        this.rightSearchBox.setResponder(query -> {
            this.rightSearch = query;
            this.refreshLists();
        });
        this.addRenderableWidget(this.rightSearchBox);

        this.leftList = new BlockListArea(leftX, LIST_TOP, listWidth, listHeight, false);
        this.rightList = new BlockListArea(rightX, LIST_TOP, listWidth, listHeight, true);
        this.addRenderableWidget(this.leftList);
        this.addRenderableWidget(this.rightList);

        int buttonY = this.height - 28;
        int center = this.width / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Previous Category"), ignored -> this.switchCategory(this.category.previous()))
                .bounds(center - 268, buttonY, 126, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Next Category"), ignored -> this.switchCategory(this.category.next()))
                .bounds(center - 136, buttonY, 112, 20)
                .build());
        this.saveButton = this.addRenderableWidget(Button.builder(Component.literal("Save"), ignored -> this.save())
                .bounds(center + 24, buttonY, 82, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), ignored -> this.onClose())
                .bounds(center + 112, buttonY, 82, 20)
                .build());

        this.refreshLists();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        Component title = Component.literal(this.category.displayName());
        graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, HEADER_TOP, ITEM_TEXT_COLOR);

        Component leftTitle = Component.literal("Unclassified (" + this.unclassifiedEntries.size() + ")");
        Component rightTitle = Component.literal("Selected (" + this.categoryEntries.size() + ")");
        graphics.text(this.font, leftTitle, this.leftX(), SEARCH_TOP - 12, STATUS_TEXT_COLOR);
        graphics.text(this.font, rightTitle, this.rightX(), SEARCH_TOP - 12, STATUS_TEXT_COLOR);

        Component footer = Component.literal("Candidates: " + this.data.candidateBlockIds().size()
                + "  Unclassified: " + this.data.unclassifiedBlockIds().size()
                + "  Stale: " + this.data.staleBlockIds().size());
        graphics.text(this.font, footer, this.leftX(), this.height - 43, STATUS_TEXT_COLOR);

        if (this.status != Component.empty()) {
            graphics.text(this.font, this.status, this.rightX(), this.height - 43, STATUS_TEXT_COLOR);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.leftList != null && this.leftList.isMouseOver(mouseX, mouseY) && this.leftList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (this.rightList != null && this.rightList.isMouseOver(mouseX, mouseY) && this.rightList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (this.leftList != null && this.leftList.handleClick(event)) {
            return true;
        }
        if (this.rightList != null && this.rightList.handleClick(event)) {
            return true;
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.leftList != null && this.leftList.mouseDragged(event, deltaX, deltaY)) {
            return true;
        }
        if (this.rightList != null && this.rightList.mouseDragged(event, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.leftList != null) {
            this.leftList.onRelease(event);
        }
        if (this.rightList != null) {
            this.rightList.onRelease(event);
        }
        return super.mouseReleased(event);
    }

    private void switchCategory(BlockClassificationCategory category) {
        this.category = category;
        this.refreshLists();
    }

    private void assign(ClassifiedBlockEntry entry) {
        this.data.assign(entry.id(), this.category);
        this.refreshLists();
    }

    private void unassign(ClassifiedBlockEntry entry) {
        this.data.remove(entry.id());
        this.refreshLists();
    }

    private void save() {
        try {
            BlockClassificationStore.save(this.data);
            Path file = BlockClassificationStore.file();
            this.status = Component.literal("Saved: " + file).withStyle(ChatFormatting.GREEN);
            if (this.saveButton != null) {
                this.saveButton.setMessage(Component.literal("Saved"));
            }
        } catch (IOException exception) {
            this.status = Component.literal("Save failed: " + exception.getMessage()).withStyle(ChatFormatting.RED);
            if (this.saveButton != null) {
                this.saveButton.setMessage(Component.literal("Failed"));
            }
        }
    }

    private void refreshLists() {
        this.unclassifiedEntries.clear();
        this.categoryEntries.clear();

        String leftFilter = this.leftSearch.toLowerCase(Locale.ROOT);
        String rightFilter = this.rightSearch.toLowerCase(Locale.ROOT);

        for (String id : this.data.unclassifiedBlockIds()) {
            ClassifiedBlockEntry entry = this.entriesById.get(id);
            if (entry != null && matches(entry, leftFilter)) {
                this.unclassifiedEntries.add(entry);
            }
        }

        for (String id : this.data.category(this.category)) {
            ClassifiedBlockEntry entry = this.entriesById.get(id);
            if (entry != null && matches(entry, rightFilter)) {
                this.categoryEntries.add(entry);
            }
        }

        this.unclassifiedEntries.sort((left, right) -> left.id().compareTo(right.id()));
        this.categoryEntries.sort((left, right) -> left.id().compareTo(right.id()));

        if (this.leftList != null) {
            this.leftList.refreshScrollAmount();
        }
        if (this.rightList != null) {
            this.rightList.refreshScrollAmount();
        }
    }

    private static boolean matches(ClassifiedBlockEntry entry, String filter) {
        return filter == null || filter.isBlank() || entry.searchText().contains(filter);
    }

    private int listWidth() {
        return Math.max(140, Math.min(310, (this.width - GAP * 3) / 2));
    }

    private int leftX() {
        return this.width / 2 - this.listWidth() - GAP / 2;
    }

    private int rightX() {
        return this.width / 2 + GAP / 2;
    }

    private int listBottom() {
        return this.height - FOOTER_HEIGHT - 14;
    }

    private final class BlockListArea extends AbstractScrollArea {
        private final boolean selectedList;

        private BlockListArea(int x, int y, int width, int height, boolean selectedList) {
            super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(ROW_HEIGHT));
            this.selectedList = selectedList;
        }

        private List<ClassifiedBlockEntry> entries() {
            return this.selectedList ? BlockClassificationScreen.this.categoryEntries : BlockClassificationScreen.this.unclassifiedEntries;
        }

        private boolean handleClick(MouseButtonEvent event) {
            if (!this.isMouseOver(event.x(), event.y())) {
                return false;
            }
            if (this.updateScrolling(event)) {
                return true;
            }

            int row = (int) ((event.y() - this.getY() + this.scrollAmount()) / ROW_HEIGHT);
            List<ClassifiedBlockEntry> entries = this.entries();
            if (row < 0 || row >= entries.size()) {
                return false;
            }

            ClassifiedBlockEntry entry = entries.get(row);
            if (this.selectedList) {
                BlockClassificationScreen.this.unassign(entry);
            } else {
                BlockClassificationScreen.this.assign(entry);
            }
            return true;
        }

        @Override
        protected int contentHeight() {
            return this.entries().size() * ROW_HEIGHT;
        }

        @Override
        protected double scrollRate() {
            return ROW_HEIGHT;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.getWidth() - this.scrollbarWidth();
        }

        @Override
        public int scrollbarWidth() {
            return AbstractScrollArea.SCROLLBAR_WIDTH;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int start = Math.max(0, (int) (this.scrollAmount() / ROW_HEIGHT));
            int end = Math.min(this.entries().size(), (int) Math.ceil((this.scrollAmount() + this.getHeight()) / ROW_HEIGHT) + 1);

            graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
            for (int i = start; i < end; i++) {
                ClassifiedBlockEntry entry = this.entries().get(i);
                int rowY = this.getY() + i * ROW_HEIGHT - (int) this.scrollAmount();
                if (rowY + ROW_HEIGHT <= this.getY() || rowY >= this.getY() + this.getHeight()) {
                    continue;
                }
                graphics.text(BlockClassificationScreen.this.font, entry.title(), this.getX() + 4, rowY + 4, ITEM_TEXT_COLOR);
                graphics.text(BlockClassificationScreen.this.font, entry.subtitle(), this.getX() + 4, rowY + 16, SUBTITLE_TEXT_COLOR);
            }
            graphics.disableScissor();
            this.extractScrollbar(graphics, mouseX, mouseY);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
}
