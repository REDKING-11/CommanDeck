package xyz.redking.commanddeck.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xyz.redking.commanddeck.CommandDeck;
import xyz.redking.commanddeck.data.ActionButtonData;
import xyz.redking.commanddeck.other.ActionButtonDataHandler;
import xyz.redking.commanddeck.other.ModKeybindings;
import xyz.redking.commanddeck.ui.components.CommandDeckButton;
import xyz.redking.commanddeck.ui.popups.BreadcrumbPopupUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainUI extends Screen {
    private static final List<ActionButtonData> navigationStack = new ArrayList<>();

    public boolean editMode = false;
    private boolean isSearching = false;
    private int menuX;
    private int menuY;
    private int menuWidth;
    private int menuHeight;
    private int scrollOffset = 0;
    private final int rowHeight = 30;
    private EditBox searchBox;
    private final Map<CommandDeckButton, ActionButtonData> buttonDataMap = new HashMap<>();
    private boolean isDraggingScrollbar = false;
    private boolean firstInit = true;
    private final boolean preserveInitialNavigation;
    private int searchToggleX;
    private int searchToggleY;
    private int editToggleX;
    private int editToggleY;

    public MainUI() {
        this(false);
    }

    public MainUI(boolean preserveInitialNavigation) {
        super(Component.translatable("menu.main.title"));
        this.preserveInitialNavigation = preserveInitialNavigation;
    }

    public static ActionButtonData currentFolder() {
        return navigationStack.isEmpty() ? null : navigationStack.get(navigationStack.size() - 1);
    }

    public static void navigateTo(ActionButtonData folder) {
        navigationStack.add(folder);
    }

    public static void navigateToLevel(int index) {
        if (index == -1) {
            navigationStack.clear();
        } else {
            while (navigationStack.size() > index + 1) {
                navigationStack.remove(navigationStack.size() - 1);
            }
        }
    }

    public static void navigateRoot() {
        navigationStack.clear();
    }

    public static boolean navigateToFolder(ActionButtonData folder) {
        List<ActionButtonData> path = new ArrayList<>();
        if (!findFolderPath(ActionButtonDataHandler.actions, folder, path)) {
            return false;
        }
        navigationStack.clear();
        navigationStack.addAll(path);
        return true;
    }

    public static boolean navigateToFolderPath(String folderPath) {
        if (folderPath == null || folderPath.isBlank()) {
            return false;
        }

        List<ActionButtonData> path = new ArrayList<>();
        List<ActionButtonData> currentActions = ActionButtonDataHandler.actions;
        for (String segment : folderPath.split("/")) {
            String name = segment.trim();
            if (name.isEmpty()) {
                continue;
            }

            ActionButtonData match = null;
            for (ActionButtonData action : currentActions) {
                if (action.isFolder && action.name.equalsIgnoreCase(name)) {
                    match = action;
                    break;
                }
            }
            if (match == null) {
                return false;
            }
            path.add(match);
            currentActions = match.children;
        }

        navigationStack.clear();
        navigationStack.addAll(path);
        return true;
    }

    private static boolean findFolderPath(List<ActionButtonData> actions, ActionButtonData target, List<ActionButtonData> path) {
        for (ActionButtonData action : actions) {
            if (!action.isFolder) {
                continue;
            }

            path.add(action);
            if (action == target || action.equals(target)) {
                return true;
            }
            if (findFolderPath(action.children, target, path)) {
                return true;
            }
            path.remove(path.size() - 1);
        }
        return false;
    }

    @Override
    protected void init() {
        var config = CommandDeck.CONFIG;
        if (firstInit) {
            if (!preserveInitialNavigation && !config.keepNavigationHistory && !navigationStack.isEmpty()) {
                navigateRoot();
            }
            firstInit = false;
        }

        menuWidth = config.buttonsPerRow * 30 + 16;
        menuHeight = 24 + config.visibleRows * 30 + 5;
        menuX = (width - menuWidth) / 2;
        menuY = (height - menuHeight) / 2;
        buttonDataMap.clear();

        if (isSearching) {
            String existingValue = searchBox == null ? "" : searchBox.getValue();
            searchBox = new EditBox(font, menuX + 8, menuY + 6, menuWidth - 50, 12, Component.empty());
            searchBox.setBordered(false);
            searchBox.setValue(existingValue);
            searchBox.setResponder(value -> {
                scrollOffset = 0;
                rebuildWidgets();
            });
            addRenderableWidget(searchBox);
            setInitialFocus(searchBox);
        }

        int toggleButtonsY = menuY + 4;
        int currentToggleX = menuX + menuWidth - 22;

        if (!config.hideEditIcon) {
            editToggleX = currentToggleX;
            editToggleY = toggleButtonsY;
            addRenderableWidget(Button.builder(Component.empty(), button -> {
                        editMode = !editMode;
                        rebuildWidgets();
                    })
                    .pos(editToggleX, editToggleY)
                    .size(18, 18)
                    .build());
            currentToggleX -= 20;
        }

        searchToggleX = currentToggleX;
        searchToggleY = toggleButtonsY;
        addRenderableWidget(Button.builder(isSearching ? Component.literal("<") : Component.empty(), button -> {
                    isSearching = !isSearching;
                    if (!isSearching && searchBox != null) {
                        searchBox.setValue("");
                    }
                    rebuildWidgets();
                })
                .pos(searchToggleX, searchToggleY)
                .size(18, 18)
                .build());

        List<ActionButtonData> actions = currentActions();
        int startX = menuX + 10;
        int startY = menuY + 28;
        int visibleAreaHeight = config.visibleRows * rowHeight;

        for (int index = 0; index < actions.size(); index++) {
            ActionButtonData data = actions.get(index);
            int row = index / config.buttonsPerRow;
            int col = index % config.buttonsPerRow;
            int btnX = startX + col * 30;
            int btnY = startY + row * rowHeight - scrollOffset;

            if (btnY >= startY && btnY + 26 <= startY + visibleAreaHeight) {
                CommandDeckButton button = new CommandDeckButton(data.icon, ignored -> handleLeftClick(data), ignored -> handleRightClick(data), data.isFolder);
                button.setX(btnX);
                button.setY(btnY);
                button.setTooltip(Tooltip.create(Component.literal(data.name)));
                addRenderableWidget(button);
                buttonDataMap.put(button, data);
            }
        }

        if (editMode) {
            int editorY = menuY + menuHeight + 8;
            addRenderableWidget(Button.builder(Component.literal("+ Action"), button -> gotoActionEditor(null))
                    .pos(menuX + 4, editorY + 4)
                    .size(menuWidth / 2 - 2, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("Settings"), button -> minecraft.setScreen(new ConfigUI(this)))
                    .pos(menuX + menuWidth / 2 + 2, editorY + 4)
                    .size(menuWidth / 2 - 2, 20)
                    .build());
        }
    }

    private List<ActionButtonData> currentActions() {
        if (isSearching && searchBox != null && !searchBox.getValue().isEmpty()) {
            return getFilteredActions(searchBox.getValue());
        }
        ActionButtonData folder = currentFolder();
        return folder != null ? folder.children : ActionButtonDataHandler.actions;
    }

    private List<ActionButtonData> getFilteredActions(String query) {
        List<ActionButtonData> result = new ArrayList<>();
        collectFiltered(ActionButtonDataHandler.actions, query.toLowerCase(), result);
        return result;
    }

    private void collectFiltered(List<ActionButtonData> actions, String query, List<ActionButtonData> result) {
        for (ActionButtonData action : actions) {
            if (action.name.toLowerCase().contains(query)) {
                result.add(action);
            }
            if (action.isFolder) {
                collectFiltered(action.children, query, result);
            }
        }
    }

    @Override
    public void rebuildWidgets() {
        clearWidgets();
        init();
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        int totalRows = (int) Math.ceil(currentActions().size() / (double) CommandDeck.CONFIG.buttonsPerRow);
        if (totalRows <= CommandDeck.CONFIG.visibleRows) {
            return false;
        }
        int sbX = menuX + menuWidth - 6;
        return mouseX >= sbX && mouseX <= sbX + 4 && mouseY >= menuY + 28 && mouseY <= menuY + 28 + (CommandDeck.CONFIG.visibleRows * rowHeight);
    }

    private void updateScrollFromMouse(double mouseY) {
        int totalRows = (int) Math.ceil(currentActions().size() / (double) CommandDeck.CONFIG.buttonsPerRow);
        int visibleH = CommandDeck.CONFIG.visibleRows * rowHeight;
        int maxScroll = Math.max(0, (totalRows * rowHeight) - visibleH);
        int sbY = menuY + 28;
        double percentage = clamp((mouseY - sbY) / (double) visibleH, 0.0, 1.0);
        scrollOffset = ((int) (percentage * maxScroll) / rowHeight) * rowHeight;
        scrollOffset = clamp(scrollOffset, 0, maxScroll);
        rebuildWidgets();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (isMouseOverScrollbar(event.x(), event.y())) {
            isDraggingScrollbar = true;
            updateScrollFromMouse(event.y());
            return true;
        }

        if (!isSearching) {
            int y = menuY + 8;
            List<BreadcrumbItem> breadcrumbs = getBreadcrumbs();
            for (BreadcrumbItem breadcrumb : breadcrumbs) {
                if (event.x() >= breadcrumb.startX && event.x() <= breadcrumb.endX && event.y() >= y && event.y() <= y + 9) {
                    if (breadcrumb.level == -2) {
                        List<BreadcrumbPopupUI.OmittedFolder> omitted = new ArrayList<>();
                        for (int i = 0; i < navigationStack.size(); i++) {
                            int level = i;
                            boolean visible = breadcrumbs.stream().anyMatch(item -> item.level == level);
                            if (!visible) {
                                omitted.add(new BreadcrumbPopupUI.OmittedFolder(i, navigationStack.get(i).name));
                            }
                        }
                        if (!omitted.isEmpty()) {
                            minecraft.setScreen(new BreadcrumbPopupUI(omitted, level -> {
                                navigateToLevel(level);
                                scrollOffset = 0;
                                rebuildWidgets();
                            }, this));
                        }
                        return true;
                    }
                    navigateToLevel(breadcrumb.level);
                    scrollOffset = 0;
                    rebuildWidgets();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (isDraggingScrollbar) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        isDraggingScrollbar = false;
        if (!editMode && CommandDeck.CONFIG.closeOnKeyReleased && ModKeybindings.matchesMenuOpenKey(event)) {
            handleReleaseAction();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalRows = (int) Math.ceil(currentActions().size() / (double) CommandDeck.CONFIG.buttonsPerRow);
        int maxScroll = Math.max(0, (totalRows * rowHeight) - (CommandDeck.CONFIG.visibleRows * rowHeight));
        scrollOffset = clamp(scrollOffset - (int) (verticalAmount * rowHeight), 0, maxScroll);
        rebuildWidgets();
        return true;
    }

    private boolean isKeyDown(String keyName) {
        try {
            InputConstants.Key key = InputConstants.getKey(keyName);
            if (key == InputConstants.UNKNOWN) {
                return false;
            }
            if (key.getType() == InputConstants.Type.MOUSE) {
                return switch (key.getValue()) {
                    case 0 -> Minecraft.getInstance().mouseHandler.isLeftPressed();
                    case 1 -> Minecraft.getInstance().mouseHandler.isRightPressed();
                    case 2 -> Minecraft.getInstance().mouseHandler.isMiddlePressed();
                    default -> false;
                };
            }
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0x44000000);
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0x77121212);
        renderThinBorder(guiGraphics, menuX, menuY, menuWidth, menuHeight, 0xAA000000);
        renderThinBorder(guiGraphics, menuX + 1, menuY + 1, menuWidth - 2, menuHeight - 2, 0x33FFFFFF);
        guiGraphics.fill(menuX + 4, menuY + 4, menuX + menuWidth - 4, menuY + 24, 0xDD141414);
        renderThinBorder(guiGraphics, menuX + 4, menuY + 4, menuWidth - 8, 20, 0x66000000);
        if (editMode) {
            int footerY = menuY + menuHeight + 8;
            guiGraphics.fill(menuX, footerY, menuX + menuWidth + 4, footerY + 28, 0xBB101010);
            renderThinBorder(guiGraphics, menuX, footerY, menuWidth + 4, 28, 0xAA000000);
        }
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (!CommandDeck.CONFIG.hideEditIcon) {
            renderPencilIcon(guiGraphics, editMode);
        }
        if (!isSearching) {
            renderSearchIcon(guiGraphics);
        }

        Component escapeHint = Component.literal("Esc to close");
        guiGraphics.text(font, escapeHint, menuX + (menuWidth - font.width(escapeHint)) / 2, menuY - 12, 0x99FFFFFF, true);

        List<ActionButtonData> actions = currentActions();
        int totalRows = (int) Math.ceil(actions.size() / (double) CommandDeck.CONFIG.buttonsPerRow);
        if (totalRows > CommandDeck.CONFIG.visibleRows) {
            int sbX = menuX + menuWidth - 5;
            int sbY = menuY + 28;
            int sbH = CommandDeck.CONFIG.visibleRows * rowHeight;
            guiGraphics.fill(sbX, sbY, sbX + 3, sbY + sbH, 0x22FFFFFF);
            int thumbH = Math.max(4, (int) (CommandDeck.CONFIG.visibleRows / (double) totalRows * sbH));
            int maxScroll = (totalRows - CommandDeck.CONFIG.visibleRows) * rowHeight;
            int thumbY = maxScroll > 0 ? sbY + (int) (scrollOffset / (double) maxScroll * (sbH - thumbH)) : sbY;
            int thumbColor = isDraggingScrollbar || isMouseOverScrollbar(mouseX, mouseY) ? 0xAAFFFFFF : 0x66FFFFFF;
            guiGraphics.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, thumbColor);
        }

        int contentStartY = menuY + 25;
        int contentEndY = contentStartY + CommandDeck.CONFIG.visibleRows * rowHeight + 3;
        int maxScroll = Math.max(0, (totalRows - CommandDeck.CONFIG.visibleRows) * rowHeight);
        if (scrollOffset > 0) {
            guiGraphics.fillGradient(menuX + 1, contentStartY, menuX + menuWidth - 1, contentStartY + 12, 0x99000000, 0x00000000);
        }
        if (scrollOffset < maxScroll) {
            guiGraphics.fillGradient(menuX + 1, contentEndY - 12, menuX + menuWidth - 1, contentEndY, 0x00000000, 0x99000000);
        }

        if (!isSearching) {
            renderBreadcrumbs(guiGraphics, mouseX, mouseY);
        } else if (searchBox != null) {
            int fieldLeft = menuX + 8;
            int fieldRight = menuX + menuWidth - 42;
            int underlineColor = searchBox.isFocused() ? 0xCCFFFFFF : 0x66FFFFFF;
            guiGraphics.fill(fieldLeft, menuY + 18, fieldRight, menuY + 19, underlineColor);
            if (searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
                guiGraphics.text(font, Component.literal("Search..."), fieldLeft, menuY + 8, 0x77FFFFFF, false);
            }
        }
        if (actions.isEmpty()) {
            Component emptyMsg = Component.translatable("menu.main.no_actions");
            guiGraphics.text(font, emptyMsg, menuX + (menuWidth - font.width(emptyMsg)) / 2, menuY + (menuHeight / 2), 0x66FFFFFF, false);
        }

        if (editMode) {
            boolean isDeleteDown = isKeyDown(CommandDeck.CONFIG.deleteModifier);
            boolean isMoveDown = isKeyDown(CommandDeck.CONFIG.moveModifier);
            for (CommandDeckButton button : buttonDataMap.keySet()) {
                if (button.isHovered()) {
                    if (isDeleteDown) {
                        renderIndicator(guiGraphics, button, 0xFFFF0000, "X");
                    } else if (isMoveDown) {
                        renderIndicator(guiGraphics, button, 0xFF00AAFF, "<>");
                    }
                }
            }
        }
    }

    private List<BreadcrumbItem> getBreadcrumbs() {
        int maxWidth = menuWidth - 30;
        int currentX = menuX + 10;
        int rootWidth = font.width("Root");
        int totalWidth = currentX + rootWidth + 5;
        List<BreadcrumbItem> allItems = new ArrayList<>();

        for (int i = 0; i < navigationStack.size(); i++) {
            String label = navigationStack.get(i).name;
            int w = font.width("> " + label);
            allItems.add(new BreadcrumbItem(label, i, 0, w));
            totalWidth += w + 5;
        }

        List<BreadcrumbItem> result = new ArrayList<>();
        int x = currentX;
        result.add(new BreadcrumbItem("Root", -1, x, x + rootWidth));
        x += rootWidth + 5;

        if (totalWidth <= menuX + maxWidth) {
            for (BreadcrumbItem item : allItems) {
                int w = font.width("> " + item.label);
                result.add(new BreadcrumbItem(item.label, item.level, x, x + w));
                x += w + 5;
            }
            return result;
        }

        int dotsW = font.width("> ...");
        result.add(new BreadcrumbItem("...", -2, x, x + dotsW));
        x += dotsW + 5;
        int availableWidth = (menuX + maxWidth) - x;
        List<BreadcrumbItem> trailingItems = new ArrayList<>();
        int usedTrailingWidth = 0;

        for (int i = allItems.size() - 1; i >= 0; i--) {
            BreadcrumbItem item = allItems.get(i);
            int w = font.width("> " + item.label);
            if (usedTrailingWidth + w + 5 <= availableWidth) {
                trailingItems.add(0, item);
                usedTrailingWidth += w + 5;
            } else {
                break;
            }
        }
        if (trailingItems.isEmpty() && !allItems.isEmpty()) {
            trailingItems.add(allItems.get(allItems.size() - 1));
        }

        for (BreadcrumbItem item : trailingItems) {
            int w = font.width("> " + item.label);
            result.add(new BreadcrumbItem(item.label, item.level, x, x + w));
            x += w + 5;
        }
        return result;
    }

    private void renderBreadcrumbs(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int y = menuY + 8;
        if (navigationStack.isEmpty()) {
            Component label = Component.literal("CommandDeck");
            guiGraphics.text(font, label, menuX + (menuWidth - font.width(label)) / 2, y, 0xFFFFFFFF, true);
            return;
        }

        for (BreadcrumbItem item : getBreadcrumbs()) {
            boolean isRoot = item.level == -1;
            boolean isDots = item.level == -2;
            String displayText = isRoot ? item.label : "> " + item.label;
            boolean hovered = mouseX >= item.startX && mouseX <= item.endX && mouseY >= y && mouseY <= y + 9;
            boolean isLast = item.level == navigationStack.size() - 1;
            int color = isDots ? 0xFF666666 : isLast ? -1 : hovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            guiGraphics.text(font, displayText, item.startX, y, color, true);
        }
    }

    private void renderIndicator(GuiGraphicsExtractor guiGraphics, CommandDeckButton button, int color, String text) {
        int size = 10;
        int x = button.getX() + button.getWidth() - size + 2;
        int y = button.getY() - 2;
        guiGraphics.fill(x, y, x + size, y + size, color);
        guiGraphics.text(font, text, x + (size - font.width(text)) / 2 + 1, y + 1, -1, false);
    }

    private void renderSearchIcon(GuiGraphicsExtractor guiGraphics) {
        int x = searchToggleX + 5;
        int y = searchToggleY + 4;
        int color = 0xFFE6E6E6;

        guiGraphics.fill(x + 1, y, x + 6, y + 1, color);
        guiGraphics.fill(x, y + 1, x + 1, y + 6, color);
        guiGraphics.fill(x + 6, y + 1, x + 7, y + 6, color);
        guiGraphics.fill(x + 1, y + 6, x + 6, y + 7, color);
        guiGraphics.fill(x + 6, y + 6, x + 8, y + 8, color);
        guiGraphics.fill(x + 8, y + 8, x + 10, y + 10, color);
    }

    private void renderPencilIcon(GuiGraphicsExtractor guiGraphics, boolean active) {
        int x = editToggleX + 5;
        int y = editToggleY + 4;
        int color = active ? 0xFFFFFFFF : 0xFFE6E6E6;
        int tipColor = active ? 0xFF7FE8FF : 0xFFFFD36B;

        guiGraphics.fill(x + 6, y, x + 8, y + 2, tipColor);
        guiGraphics.fill(x + 5, y + 2, x + 8, y + 3, color);
        guiGraphics.fill(x + 4, y + 3, x + 7, y + 4, color);
        guiGraphics.fill(x + 3, y + 4, x + 6, y + 5, color);
        guiGraphics.fill(x + 2, y + 5, x + 5, y + 6, color);
        guiGraphics.fill(x + 1, y + 6, x + 4, y + 7, color);
        guiGraphics.fill(x, y + 7, x + 3, y + 8, color);
        guiGraphics.fill(x, y + 8, x + 1, y + 10, 0xFFB8B8B8);
        if (active) {
            guiGraphics.fill(editToggleX + 4, editToggleY + 15, editToggleX + 14, editToggleY + 16, 0xFF7FE8FF);
        }
    }

    private void renderThinBorder(GuiGraphicsExtractor guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y + 1, x + 1, y + h - 1, color);
        guiGraphics.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void handleLeftClick(ActionButtonData data) {
        if (editMode) {
            if (isKeyDown(CommandDeck.CONFIG.deleteModifier)) {
                deleteAction(data);
            } else if (isKeyDown(CommandDeck.CONFIG.moveModifier)) {
                moveAction(data, -1);
            } else if (data.isFolder) {
                clearSearchForNavigation();
                navigateTo(data);
                scrollOffset = 0;
                rebuildWidgets();
            } else {
                gotoActionEditor(data);
            }
            return;
        }

        if (data.isFolder) {
            clearSearchForNavigation();
            navigateTo(data);
            scrollOffset = 0;
            rebuildWidgets();
        } else {
            data.run();
            if (CommandDeck.CONFIG.closeOnAction) {
                minecraft.setScreen(null);
            }
        }
    }

    private void clearSearchForNavigation() {
        if (isSearching) {
            isSearching = false;
            if (searchBox != null) {
                searchBox.setValue("");
            }
        }
    }

    private void handleRightClick(ActionButtonData data) {
        if (!editMode) {
            return;
        }
        if (isKeyDown(CommandDeck.CONFIG.moveModifier)) {
            moveAction(data, 1);
        } else {
            gotoActionEditor(data);
        }
    }

    private void moveAction(ActionButtonData data, int direction) {
        if (isSearching) {
            return;
        }
        List<ActionButtonData> actions = currentFolder() != null ? currentFolder().children : ActionButtonDataHandler.actions;
        int index = actions.indexOf(data);
        int newIndex = index + direction;
        if (newIndex >= 0 && newIndex < actions.size()) {
            Collections.swap(actions, index, newIndex);
            ActionButtonDataHandler.save();
            rebuildWidgets();
        }
    }

    private void deleteAction(ActionButtonData data) {
        if (isSearching) {
            findAndDelete(ActionButtonDataHandler.actions, data);
        } else {
            List<ActionButtonData> actions = currentFolder() != null ? currentFolder().children : ActionButtonDataHandler.actions;
            actions.remove(data);
        }
        ActionButtonDataHandler.save();
        rebuildWidgets();
    }

    private boolean findAndDelete(List<ActionButtonData> list, ActionButtonData target) {
        if (list.remove(target)) {
            return true;
        }
        for (ActionButtonData action : list) {
            if (action.isFolder && findAndDelete(action.children, target)) {
                return true;
            }
        }
        return false;
    }

    private void gotoActionEditor(ActionButtonData action) {
        ActionEditorUI actionEditor = new ActionEditorUI(action);
        actionEditor.previousScreen = this;
        minecraft.setScreen(actionEditor);
    }

    private void handleReleaseAction() {
        for (Map.Entry<CommandDeckButton, ActionButtonData> entry : buttonDataMap.entrySet()) {
            if (entry.getKey().isHovered() && !entry.getValue().isFolder) {
                handleLeftClick(entry.getValue());
                break;
            }
        }
        minecraft.setScreen(null);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_F && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
            isSearching = !isSearching;
            if (!isSearching && searchBox != null) {
                searchBox.setValue("");
            }
            rebuildWidgets();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_E) {
            editMode = !editMode;
            rebuildWidgets();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE && currentFolder() != null && !isSearching) {
            navigateToLevel(navigationStack.size() - 2);
            scrollOffset = 0;
            rebuildWidgets();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE && isSearching) {
            isSearching = false;
            if (searchBox != null) {
                searchBox.setValue("");
            }
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (!editMode && CommandDeck.CONFIG.closeOnKeyReleased && ModKeybindings.matchesMenuOpenKey(event)) {
            handleReleaseAction();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record BreadcrumbItem(String label, int level, int startX, int endX) {
    }
}

