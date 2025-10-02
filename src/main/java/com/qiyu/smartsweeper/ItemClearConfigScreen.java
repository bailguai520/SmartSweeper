package com.qiyu.smartsweeper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.util.ArrayList;
import java.util.List;

public class ItemClearConfigScreen extends Screen {
    private final Screen parent;
    private EditBox whitelistInput;
    private Button addButton;
    private Button removeButton;
    private Button applyButton;
    private Button backButton;
    private Checkbox enabledCheckbox;
    private Checkbox showMessageCheckbox;
    private Checkbox onlyNaturalCheckbox;
    private ExtendedSlider intervalSlider;
    
    private List<String> whitelistItems;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private List<String> suggestions = new ArrayList<>();
    private int suggestionScroll = 0;
    
    // Search functionality
    private EditBox searchInput;
    private String searchQuery = "";
    private List<String> filteredWhitelist = new ArrayList<>();
    private int whitelistScrollOffset = 0; // Separate scroll for whitelist grid
    
    // Context menu for right-click
    private boolean showContextMenu = false;
    private int contextMenuX = 0;
    private int contextMenuY = 0;
    private int contextMenuItemIndex = -1;
    
    // Suggestion item bounds for accurate click detection
    private static class SuggestionBounds {
        int index;
        int x1, y1, x2, y2;
        SuggestionBounds(int index, int x1, int y1, int x2, int y2) {
            this.index = index;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
        }
    }
    private List<SuggestionBounds> suggestionBounds = new ArrayList<>();
    
    private static final int LIST_ITEM_HEIGHT = 22;
    private static final int LIST_HEIGHT = 220;
    private static final int SUGGESTION_HEIGHT = 22; // 增加到22以容纳图标+文字
    private static final int MAX_SUGGESTIONS = 6;
    
    // Layout constants for responsive design
    private static final int PADDING = 16;
    private static final int SETTINGS_CONTENT_HEIGHT = 100;
    private static final int WHITELIST_HEADER_HEIGHT = 30;
    private static final int WHITELIST_INPUT_HEIGHT = 22;
    private static final int WHITELIST_SEARCH_HEIGHT = 27;
    private static final int WHITELIST_BOTTOM_PADDING = 8;
    private static final int CARD_SPACING = 12;
    
    // MD3 Colors (same as main GUI)
    private static final int COLOR_PRIMARY = 0xFF6750A4;
    private static final int COLOR_SURFACE_VARIANT = 0xFF2B2930;
    private static final int COLOR_ON_SURFACE = 0xFFE6E1E5;
    private static final int COLOR_ON_SURFACE_VARIANT = 0xFFCAC4D0;
    private static final int COLOR_SUCCESS = 0xFF4CAF50;
    private static final int COLOR_ERROR = 0xFFB3261E;
    private static final int COLOR_OUTLINE = 0xFF938F99;

    public ItemClearConfigScreen(Screen parent) {
        super(Component.translatable("SmartSweeper.config.title"));
        this.parent = parent;
        this.whitelistItems = new ArrayList<>(Config.WHITELIST.get());
        this.filteredWhitelist = new ArrayList<>(this.whitelistItems);
        Config.refreshWhitelist();
        
        // Debug log
        SmartSweeper.LOGGER.info("ItemClearConfigScreen initialized with {} whitelist items", whitelistItems.size());
        for (String item : whitelistItems) {
            SmartSweeper.LOGGER.info("  - {}", item);
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int cardWidth = Math.min(500, Math.max(350, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        int padding = 16;
        
        // Settings card base position
        int settingsCardY = 45;
        int baseY = settingsCardY - scrollOffset;
        
        // Checkboxes in a row
        int checkboxY = baseY + 30;
        int checkboxWidth = (cardWidth - padding * 4) / 3;
        
        this.enabledCheckbox = Checkbox.builder(
            Component.translatable("SmartSweeper.config.enabled"), 
            this.font)
            .pos(cardX + padding, checkboxY)
            .selected(Config.CLEAR_ENABLED.get())
            .onValueChange((checkbox, checked) -> {
                Config.CLEAR_ENABLED.set(checked);
                Config.SPEC.save();
            })
            .build();
        this.addRenderableWidget(this.enabledCheckbox);

        this.showMessageCheckbox = Checkbox.builder(
            Component.translatable("SmartSweeper.config.show_message"), 
            this.font)
            .pos(cardX + padding + checkboxWidth + padding, checkboxY)
            .selected(Config.SHOW_CLEAR_MESSAGE.get())
            .onValueChange((checkbox, checked) -> {
                Config.SHOW_CLEAR_MESSAGE.set(checked);
                Config.SPEC.save();
            })
            .build();
        this.addRenderableWidget(this.showMessageCheckbox);

        this.onlyNaturalCheckbox = Checkbox.builder(
            Component.translatable("SmartSweeper.config.only_natural"), 
            this.font)
            .pos(cardX + padding + (checkboxWidth + padding) * 2, checkboxY)
            .selected(Config.CLEAR_ONLY_NATURAL.get())
            .onValueChange((checkbox, checked) -> {
                Config.CLEAR_ONLY_NATURAL.set(checked);
                Config.SPEC.save();
            })
            .build();
        this.addRenderableWidget(this.onlyNaturalCheckbox);

        // Interval slider
        int sliderY = baseY + 60;
        
        this.intervalSlider = new ExtendedSlider(
            cardX + padding, sliderY, cardWidth - padding * 2, 20,
            Component.translatable("SmartSweeper.config.interval"),
            Component.literal("s"),
            10, 3600,
            Config.CLEAR_INTERVAL.get(),
            1.0,
            1,
            true
        ) {
            @Override
            protected void updateMessage() {
                super.updateMessage();
                // Real-time update while dragging
                Config.CLEAR_INTERVAL.set((int) this.getValue());
            }
            
            @Override
            public void onRelease(double mouseX, double mouseY) {
                super.onRelease(mouseX, mouseY);
                Config.CLEAR_INTERVAL.set((int) this.getValue());
                Config.SPEC.save();
            }
        };
        this.addRenderableWidget(this.intervalSlider);
        
        // Whitelist section
        int whitelistCardY = settingsCardY + 105 + 12;
        int whitelistY = whitelistCardY + 30 - scrollOffset;
        
        // Whitelist input box
        int inputWidth = cardWidth - padding * 2 - 100;
        this.whitelistInput = new EditBox(this.font, cardX + padding, whitelistY, inputWidth, 20, 
            Component.translatable("SmartSweeper.config.whitelist.input"));
        this.whitelistInput.setMaxLength(100);
        this.whitelistInput.setHint(Component.translatable("SmartSweeper.config.whitelist.hint"));
        this.whitelistInput.setResponder(text -> updateSuggestions(text));
        this.addRenderableWidget(this.whitelistInput);

        // Search input box (below whitelist input)
        int searchY = whitelistY + 25;
        int searchWidth = cardWidth - padding * 2;
        this.searchInput = new EditBox(this.font, cardX + padding, searchY, searchWidth, 20, 
            Component.translatable("SmartSweeper.config.search.hint"));
        this.searchInput.setMaxLength(50);
        this.searchInput.setHint(Component.translatable("SmartSweeper.config.search.hint"));
        this.searchInput.setResponder(text -> {
            this.searchQuery = text.toLowerCase();
            filterWhitelist();
        });
        this.addRenderableWidget(this.searchInput);

        // Add and Remove buttons
        int buttonX = cardX + padding + inputWidth + 5;
        this.addButton = Button.builder(Component.translatable("SmartSweeper.config.whitelist.add"), 
            button -> this.addToWhitelist())
            .bounds(buttonX, whitelistY, 45, 20)
            .build();
        this.addRenderableWidget(this.addButton);

        this.removeButton = Button.builder(Component.translatable("SmartSweeper.config.whitelist.remove"), 
            button -> this.removeFromWhitelist())
            .bounds(buttonX + 50, whitelistY, 45, 20)
            .build();
        this.addRenderableWidget(this.removeButton);

        // Action buttons at bottom
        int bottomY = this.height - 40;
        int buttonWidth = 120;
        
        this.applyButton = Button.builder(Component.translatable("SmartSweeper.config.apply"),
            button -> {
                // Save all configurations
                Config.CLEAR_ENABLED.set(this.enabledCheckbox.selected());
                Config.SHOW_CLEAR_MESSAGE.set(this.showMessageCheckbox.selected());
                Config.CLEAR_ONLY_NATURAL.set(this.onlyNaturalCheckbox.selected());
                Config.CLEAR_INTERVAL.set((int)this.intervalSlider.getValue());
                Config.SPEC.save();
                Config.refreshWhitelist();
                
                // Only display message if player is in game
                if (this.minecraft.player != null) {
                    this.minecraft.player.displayClientMessage(
                        Component.translatable("SmartSweeper.config.settings_applied"),
                        false
                    );
                }
            })
            .bounds(centerX - buttonWidth - 5, bottomY, buttonWidth, 24)
            .build();
        this.addRenderableWidget(this.applyButton);

        this.backButton = Button.builder(Component.translatable("gui.back"),
            button -> this.onClose())
            .bounds(centerX + 5, bottomY, buttonWidth, 24)
            .build();
        this.addRenderableWidget(this.backButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Custom background
        graphics.fill(0, 0, this.width, this.height, 0xC0000000);
        
        int centerX = this.width / 2;
        int cardWidth = Math.min(500, Math.max(350, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        int padding = 16;
        
        // Title
        graphics.drawCenteredString(this.font, this.title, centerX, 12, COLOR_ON_SURFACE);
        
        // Calculate available space for scrollable area
        int scrollAreaTop = 25;
        int scrollAreaBottom = this.height - 55;
        int availableHeight = scrollAreaBottom - scrollAreaTop;
        
        graphics.enableScissor(0, scrollAreaTop, this.width, scrollAreaBottom);
        
        // Apply scroll offset
        int baseY = scrollAreaTop + 10 - scrollOffset;
        
        // ===== Settings Card ===== (固定高度)
        int settingsY = baseY;
        int settingsHeight = 105;
        drawCard(graphics, cardX, settingsY, cardWidth, settingsHeight);
        
        // Card header
        graphics.drawString(this.font,
            Component.translatable("SmartSweeper.gui.settings"),
            cardX + padding, settingsY + 10, COLOR_ON_SURFACE);
        
        // Warning info text
        graphics.drawString(this.font,
            Component.translatable("SmartSweeper.config.warning_info"),
            cardX + padding, settingsY + 90, COLOR_ON_SURFACE_VARIANT);
        
        // ===== Whitelist Card ===== (自适应高度)
        int whitelistY = settingsY + settingsHeight + 8;
        
        // 计算白名单卡片的自适应高度
        // 可用高度 = 总可用空间 - 设置卡片高度 - 卡片间距 - 顶部偏移
        int whitelistMaxHeight = availableHeight - settingsHeight - 8 - 10;
        
        // 白名单卡片内容：标题(12) + 分割线(17) + 输入框(20) + 间距(5) + 搜索框(20) + 间距(5) + 列表(LIST_HEIGHT) + 底部边距(10)
        int whitelistMinHeight = 12 + 17 + 20 + 5 + 20 + 5 + LIST_HEIGHT + 10;
        
        // 使用可用空间，尽可能使用最大高度
        int whitelistHeight = Math.max(whitelistMinHeight, whitelistMaxHeight);
        
        drawCard(graphics, cardX, whitelistY, cardWidth, whitelistHeight);
        
        // Whitelist header
        graphics.drawString(this.font,
            Component.translatable("SmartSweeper.config.whitelist.title"),
            cardX + padding, whitelistY + 12, COLOR_ON_SURFACE);
        
        // Divider
        graphics.fill(cardX + padding, whitelistY + 28, cardX + cardWidth - padding, whitelistY + 29, COLOR_OUTLINE);
        
        // Render whitelist list first
        this.renderWhitelist(graphics, cardX, whitelistY, cardWidth, mouseX, mouseY);
        
        // Render custom styled inputs and buttons afterwards so they sit on top of list
        renderCustomWidgets(graphics, mouseX, mouseY, whitelistY, whitelistY + whitelistHeight);
        
        // Render suggestions if input is focused and no context menu is open
        if (this.whitelistInput != null && this.whitelistInput.isFocused() && !suggestions.isEmpty() && !showContextMenu) {
            renderSuggestions(graphics, cardX, whitelistY, cardWidth, mouseX, mouseY);
        }
        
        graphics.disableScissor();
        
        // Render context menu if visible (after disabling scissor, top layer)
        if (showContextMenu && contextMenuItemIndex >= 0 && contextMenuItemIndex < filteredWhitelist.size()) {
            renderContextMenu(graphics, mouseX, mouseY);
        }
        
        // Render bottom buttons
        renderMD3Button(graphics, this.applyButton, mouseX, mouseY);
        renderMD3Button(graphics, this.backButton, mouseX, mouseY);
    }
    
    private void renderWhitelist(GuiGraphics graphics, int cardX, int cardY, int cardWidth, int mouseX, int mouseY) {
        int listX = cardX + 16;
        int listY = cardY + 77; // Position below search box (searchY + 20 + 5)
        int listWidth = cardWidth - 32;
        
        // Draw list background with shadow - always visible
        graphics.fill(listX + 1, listY + 1, listX + listWidth + 1, listY + LIST_HEIGHT + 1, 0x80000000);
        graphics.fill(listX, listY, listX + listWidth, listY + LIST_HEIGHT, 0xFF1A1A1A);
        drawOutline(graphics, listX, listY, listWidth, LIST_HEIGHT, COLOR_OUTLINE);
        
        // Empty state
        if (filteredWhitelist.isEmpty()) {
            String message = searchQuery.isEmpty() ? 
                Component.translatable("SmartSweeper.config.whitelist.empty").getString() :
                Component.translatable("SmartSweeper.config.search.no_results").getString();
            graphics.drawCenteredString(this.font, message,
                listX + listWidth / 2, listY + LIST_HEIGHT / 2 - 4,
                COLOR_ON_SURFACE_VARIANT);
            return;
        }
        
        // Enable scissor to clip content within list bounds
        graphics.enableScissor(listX, listY, listX + listWidth, listY + LIST_HEIGHT);
        
        // Grid layout: calculate columns based on screen width
        int itemWidth = 80; // Width per grid item
        int itemHeight = 70; // Height per grid item (icon + name + ID = 需要更多空间)
        int columns = Math.max(1, listWidth / itemWidth);
        int rows = (int) Math.ceil((double) filteredWhitelist.size() / columns);
        
        // Calculate visible area using separate whitelist scroll
        int maxVisibleRows = LIST_HEIGHT / itemHeight;
        int startRow = Math.max(0, whitelistScrollOffset / itemHeight);
        int endRow = Math.min(startRow + maxVisibleRows + 1, rows); // +1 to show partial rows
        
        for (int i = startRow * columns; i < Math.min(endRow * columns, filteredWhitelist.size()); i++) {
            String itemId = filteredWhitelist.get(i);
            int row = i / columns;
            int col = i % columns;
            
            int itemX = listX + 5 + col * itemWidth;
            int itemY = listY + 10 + row * itemHeight - whitelistScrollOffset; // 添加顶部padding从10开始而不是5
            
            // 检查物品是否在可见区域内（防止超出边界）
            if (itemY + itemHeight < listY || itemY > listY + LIST_HEIGHT) {
                continue; // 跳过不在可见区域的物品
            }
            
            // Highlight selected item (确保不超出列表边界)
            if (i == selectedIndex) {
                int highlightY1 = Math.max(itemY - 2, listY);
                int highlightY2 = Math.min(itemY + itemHeight - 2, listY + LIST_HEIGHT);
                graphics.fill(itemX - 2, highlightY1, itemX + itemWidth - 2, highlightY2, 
                    0x80000000 | (COLOR_PRIMARY & 0x00FFFFFF));
            }
            
            // Hover highlight (确保不超出列表边界)
            boolean isHovered = mouseX >= itemX && mouseX <= itemX + itemWidth - 10 &&
                               mouseY >= itemY && mouseY <= itemY + itemHeight &&
                               mouseY >= listY && mouseY <= listY + LIST_HEIGHT; // 确保在列表内
            if (isHovered && i != selectedIndex) {
                int highlightY1 = Math.max(itemY - 2, listY);
                int highlightY2 = Math.min(itemY + itemHeight - 2, listY + LIST_HEIGHT);
                graphics.fill(itemX - 2, highlightY1, itemX + itemWidth - 2, highlightY2, 0x40FFFFFF);
            }
            
            // Draw item icon
            ResourceLocation location = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.getValue(location);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                // Render item icon centered
                graphics.renderItem(stack, itemX + (itemWidth - 16) / 2, itemY + 5);
                
                // 获取物品显示名称
                String itemName = "";
                try {
                    itemName = stack.getHoverName().getString();
                } catch (Exception e) {
                    itemName = "";
                }
                
                int maxTextWidth = itemWidth - 10;
                int textColor = i == selectedIndex ? 0xFFFFFF : (isHovered ? 0xFFFFFF : COLOR_ON_SURFACE);
                
                if (!itemName.isEmpty()) {
                    // 显示中文名称（第一行，亮色）
                    String displayName = itemName;
                    if (this.font.width(displayName) > maxTextWidth) {
                        displayName = this.font.plainSubstrByWidth(displayName, maxTextWidth - 10) + "...";
                    }
                    graphics.drawCenteredString(this.font, displayName, 
                        itemX + itemWidth / 2, itemY + 25, textColor);
                    
                    // 显示物品ID路径（第二行，暗色，更小）
                    String displayId = location.getPath(); // 只显示路径部分
                    if (this.font.width(displayId) > maxTextWidth) {
                        displayId = this.font.plainSubstrByWidth(displayId, maxTextWidth - 10) + "...";
                    }
                    int idColor = i == selectedIndex ? 0xFFCCCCCC : (isHovered ? 0xFFCCCCCC : COLOR_ON_SURFACE_VARIANT);
                    graphics.drawCenteredString(this.font, displayId, 
                        itemX + itemWidth / 2, itemY + 48, idColor);
                } else {
                    // 如果没有名称，只显示ID
                    String displayId = itemId;
                    if (this.font.width(displayId) > maxTextWidth) {
                        displayId = this.font.plainSubstrByWidth(displayId, maxTextWidth - 10) + "...";
                    }
                    graphics.drawCenteredString(this.font, displayId, 
                        itemX + itemWidth / 2, itemY + 25, textColor);
                }
            } else {
                // Invalid item
                graphics.drawCenteredString(this.font, "❌", 
                    itemX + itemWidth / 2, itemY + 10, COLOR_ERROR);
                graphics.drawCenteredString(this.font, itemId, 
                    itemX + itemWidth / 2, itemY + 25, COLOR_ERROR);
            }
        }
        
        // Disable scissor before drawing scroll indicators
        graphics.disableScissor();
        
        // Scroll indicator
        if (rows > maxVisibleRows) {
            String scrollText = String.format("%d / %d", filteredWhitelist.size(), filteredWhitelist.size());
            graphics.drawString(this.font, scrollText, listX + listWidth - 50, listY + LIST_HEIGHT + 2, COLOR_ON_SURFACE_VARIANT);
            
            // Scroll arrows
            if (whitelistScrollOffset > 0) {
                graphics.drawString(this.font, "▲", listX + 2, listY + LIST_HEIGHT + 2, COLOR_PRIMARY);
            }
            int maxScroll = Math.max(0, rows * itemHeight - LIST_HEIGHT);
            if (whitelistScrollOffset < maxScroll) {
                graphics.drawString(this.font, "▼", listX + 12, listY + LIST_HEIGHT + 2, COLOR_PRIMARY);
            }
        }
    }
    
    private void renderContextMenu(GuiGraphics graphics, int mouseX, int mouseY) {
        String itemId = filteredWhitelist.get(contextMenuItemIndex);
        
        int menuWidth = 160;
        int menuHeight = 70;
        
        // Adjust position to stay within screen
        int menuX = Math.min(contextMenuX, this.width - menuWidth - 5);
        int menuY = Math.min(contextMenuY, this.height - menuHeight - 5);
        menuX = Math.max(5, menuX);
        menuY = Math.max(5, menuY);
        
        // Push pose to render on top layer with higher Z-index
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400); // Move to high Z level (above items which render at ~200)
        
        // Menu background (dark theme color)
        graphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF2B2930);
        
        // Menu outline
        drawOutline(graphics, menuX, menuY, menuWidth, menuHeight, COLOR_OUTLINE);
        
        // Menu title with item name
        String displayId = itemId;
        if (this.font.width(displayId) > menuWidth - 20) {
            displayId = this.font.plainSubstrByWidth(displayId, menuWidth - 30) + "...";
        }
        graphics.drawCenteredString(this.font, displayId, menuX + menuWidth / 2, menuY + 8, COLOR_ON_SURFACE);
        
        // Divider
        graphics.fill(menuX + 8, menuY + 22, menuX + menuWidth - 8, menuY + 23, COLOR_OUTLINE);
        
        // Remove option
        int removeY = menuY + 30;
        int removeHeight = 24;
        boolean isRemoveHovered = mouseX >= menuX + 8 && mouseX <= menuX + menuWidth - 8 &&
                                  mouseY >= removeY && mouseY <= removeY + removeHeight;
        
        if (isRemoveHovered) {
            graphics.fill(menuX + 8, removeY, menuX + menuWidth - 8, removeY + removeHeight, 0x60000000 | (COLOR_ERROR & 0x00FFFFFF));
        }
        
        graphics.drawString(this.font, "🗑 " + Component.translatable("SmartSweeper.config.context.remove").getString(),
            menuX + 12, removeY + 8, isRemoveHovered ? 0xFFFFFFFF : COLOR_ERROR);
        
        // Pop pose to restore Z level
        graphics.pose().popPose();
    }
    
    private void renderCustomWidgets(GuiGraphics graphics, int mouseX, int mouseY, int cardTop, int cardBottom) {
        // Render all scrollable widgets
        for (var widget : this.renderables) {
            if (widget == this.applyButton || widget == this.backButton) {
                continue; // Skip fixed bottom buttons
            }
            
            if (widget instanceof Button button) {
                // Render buttons inside whitelist card area
                renderMD3Button(graphics, button, mouseX, mouseY);
            } else if (widget instanceof EditBox editBox) {
                // Render opaque background behind edit box to avoid overlap with list
                int x = editBox.getX();
                int y = editBox.getY();
                int width = editBox.getWidth();
                int height = editBox.getHeight();
                graphics.fill(x, y, x + width, y + height, 0xFF1E1E1E);
                renderMD3EditBox(graphics, editBox);
                widget.render(graphics, mouseX, mouseY, 0);
            } else if (widget instanceof Checkbox || widget instanceof ExtendedSlider) {
                // Render checkboxes and slider with default rendering
                widget.render(graphics, mouseX, mouseY, 0);
            }
        }
    }
    
    private void renderMD3Button(GuiGraphics graphics, Button button, int mouseX, int mouseY) {
        if (!button.visible) return;
        
        int x = button.getX();
        int y = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();
        boolean isHovered = button.isHovered();
        
        // Button color based on function
        int buttonColor = COLOR_PRIMARY;
        String message = button.getMessage().getString();
        
        if (message.contains("应用") || message.contains("Apply")) {
            buttonColor = COLOR_SUCCESS;
        } else if (message.contains("添加") || message.contains("Add")) {
            buttonColor = COLOR_SUCCESS;
        } else if (message.contains("移除") || message.contains("Remove")) {
            buttonColor = COLOR_ERROR;
        } else if (message.contains("返回") || message.contains("Back")) {
            buttonColor = 0xFF607D8B;
        }
        
        // Button rendering
        int alpha = isHovered ? 0xFF : 0xE0;
        int finalColor = (buttonColor & 0x00FFFFFF) | (alpha << 24);
        
        // Shadow
        if (button.active) {
            graphics.fill(x + 1, y + 1, x + width + 1, y + height + 1, 0x40000000);
        }
        
        // Background
        graphics.fill(x, y, x + width, y + height, finalColor);
        
        // Hover effect
        if (isHovered && button.active) {
            graphics.fill(x, y, x + width, y + height, 0x30FFFFFF);
        }
        
        // Outline
        drawOutline(graphics, x, y, width, height, isHovered ? 0xFFFFFFFF : COLOR_OUTLINE);
        
        // Text
        int textColor = button.active ? 0xFFFFFFFF : 0xFF666666;
        graphics.drawCenteredString(this.font, button.getMessage(), x + width / 2, y + (height - 8) / 2, textColor);
    }
    
    private void renderMD3EditBox(GuiGraphics graphics, EditBox editBox) {
        int x = editBox.getX();
        int y = editBox.getY();
        int width = editBox.getWidth();
        int height = editBox.getHeight();

        // Opaque background so underlying widgets do not show through
        graphics.fill(x, y, x + width, y + height, 0xFF1E1E1E);

        // Outline
        drawOutline(graphics, x, y, width, height, COLOR_OUTLINE);

        // Add subtle glow when focused
        if (editBox.isFocused()) {
            graphics.fill(x - 1, y - 1, x + width + 1, y - 1, COLOR_PRIMARY);
            graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, COLOR_PRIMARY);
            graphics.fill(x - 1, y - 1, x, y + height + 1, COLOR_PRIMARY);
            graphics.fill(x + width, y - 1, x + width + 1, y + height + 1, COLOR_PRIMARY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int cardWidth = Math.min(500, Math.max(350, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        
        // Handle context menu clicks first (highest priority)
        if (showContextMenu && button == 0) { // Left click
            int menuWidth = 160;
            int menuHeight = 70;
            int menuX = Math.min(contextMenuX, this.width - menuWidth - 5);
            int menuY = Math.min(contextMenuY, this.height - menuHeight - 5);
            menuX = Math.max(5, menuX);
            menuY = Math.max(5, menuY);
            
            // Check if clicked on remove option
            int removeY = menuY + 30;
            int removeHeight = 24;
            if (mouseX >= menuX + 8 && mouseX <= menuX + menuWidth - 8 &&
                mouseY >= removeY && mouseY <= removeY + removeHeight) {
                // Remove the item
                removeFromWhitelist();
                showContextMenu = false;
                return true;
            }
            
            // Clicked outside menu - close it
            showContextMenu = false;
            return true;
        }
        
        // Check if clicked on suggestion (BEFORE super.mouseClicked to prevent click-through)
        if (this.whitelistInput != null && this.whitelistInput.isFocused() && !suggestions.isEmpty()) {
            // 使用记录的精确边界进行点击检测（不再使用坐标计算）
            boolean clickedOnSuggestion = false;
            for (SuggestionBounds bounds : suggestionBounds) {
                if (bounds.contains(mouseX, mouseY)) {
                    // 点击了这个提示项
                    if (bounds.index >= 0 && bounds.index < suggestions.size()) {
                        this.whitelistInput.setValue(suggestions.get(bounds.index));
                        suggestions.clear();
                        suggestionBounds.clear();
                        return true;
                    }
                    clickedOnSuggestion = true;
                    break;
                }
            }
            
            // 如果没有点击到任何提示项，关闭提示框
            if (!clickedOnSuggestion && button == 0) {
                suggestions.clear();
                suggestionBounds.clear();
            }
        }
        
        // Check if clicked on any widget (buttons, checkboxes, sliders, input boxes)
        // This prevents clicking through buttons to whitelist items below
        boolean widgetHandled = super.mouseClicked(mouseX, mouseY, button);
        if (widgetHandled) {
            // A widget was clicked, don't process whitelist clicks
            showContextMenu = false;
            return true;
        }
        
        // Check if clicked in whitelist list area (lowest priority)
        int whitelistCardY = 45 + 105 + 12;
        int baseWhitelistY = whitelistCardY - scrollOffset; // Consider page scroll
        int listX = cardX + 16;
        int listY = baseWhitelistY + 77; // Updated for search box
        int listWidth = cardWidth - 32;
        
        // Check if clicked in whitelist area
        if (mouseX >= listX && mouseX <= listX + listWidth && 
            mouseY >= listY && mouseY <= listY + LIST_HEIGHT) {
            
            // Grid layout click detection
            int itemWidth = 80;
            int itemHeight = 70; // 与渲染时保持一致
            int columns = Math.max(1, listWidth / itemWidth);
            
            int clickedCol = (int) ((mouseX - listX - 5) / itemWidth);
            int clickedRow = (int) ((mouseY - listY - 10 + whitelistScrollOffset) / itemHeight); // 对应顶部padding=10
            
            if (clickedCol >= 0 && clickedCol < columns) {
                int clickedIndex = clickedRow * columns + clickedCol;
                if (clickedIndex >= 0 && clickedIndex < filteredWhitelist.size()) {
                    SmartSweeper.LOGGER.info("Clicked whitelist item: index={}, button={}, itemId={}", 
                        clickedIndex, button, filteredWhitelist.get(clickedIndex));
                    
                    if (button == 1) { // Right click
                        contextMenuItemIndex = clickedIndex;
                        selectedIndex = clickedIndex;
                        showContextMenu = true;
                        contextMenuX = (int) mouseX;
                        contextMenuY = (int) mouseY;
                        SmartSweeper.LOGGER.info("Showing context menu at ({}, {})", contextMenuX, contextMenuY);
                        return true;
                    } else if (button == 0) { // Left click
                        selectedIndex = clickedIndex;
                        showContextMenu = false;
                        return true;
                    }
                }
            }
        }
        
        // Close context menu if clicked elsewhere
        if (showContextMenu) {
            showContextMenu = false;
        }
        
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int centerX = this.width / 2;
        int cardWidth = Math.min(500, Math.max(350, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        int whitelistCardY = 45 + 105 + 12;
        
        // Check if mouse is over suggestions dropdown first (highest priority)
        if (this.whitelistInput != null && this.whitelistInput.isFocused() && !suggestions.isEmpty()) {
            int whitelistY = whitelistCardY + 30 - scrollOffset;
            int suggestX = cardX + 16;
            int suggestY = whitelistY + 22;
            int suggestWidth = cardWidth - 32 - 100;
            int totalHeight = Math.min(suggestions.size(), MAX_SUGGESTIONS) * SUGGESTION_HEIGHT;
            
            // Check if mouse is over suggestions area
            if (mouseX >= suggestX && mouseX <= suggestX + suggestWidth &&
                mouseY >= suggestY && mouseY <= suggestY + totalHeight) {
                
                // Scroll suggestions list
                int maxSuggestionsScroll = Math.max(0, suggestions.size() - MAX_SUGGESTIONS);
                if (maxSuggestionsScroll > 0) {
                    suggestionScroll = Math.max(0, Math.min(maxSuggestionsScroll, 
                        suggestionScroll - (int)scrollY));
                    return true; // Consume the scroll event
                }
                // Even if can't scroll, consume the event to prevent page scrolling
                return true;
            }
        }
        
        // Check if mouse is over whitelist area
        int listX = cardX + 16;
        int listY = whitelistCardY + 77 - scrollOffset; // Adjusted for page scroll
        int listWidth = cardWidth - 32;
        
        if (mouseX >= listX && mouseX <= listX + listWidth && 
            mouseY >= listY && mouseY <= listY + LIST_HEIGHT) {
            
            // Scroll whitelist grid
            int itemHeight = 70; // 与渲染时保持一致
            int columns = Math.max(1, listWidth / 80);
            int rows = (int) Math.ceil((double) filteredWhitelist.size() / columns);
            int maxWhitelistScroll = Math.max(0, rows * itemHeight - LIST_HEIGHT);
            
            if (maxWhitelistScroll > 0) {
                whitelistScrollOffset = Math.max(0, Math.min(maxWhitelistScroll, 
                    whitelistScrollOffset - (int)(scrollY * 20)));
                return true;
            }
        }
        
        // Scroll the whole page
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(scrollY * 20)));
            this.repositionElements();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    private int getMaxScroll() {
        // 自适应计算总内容高度
        int availableHeight = this.height - 115; // 30 (top) + 85 (bottom)
        int settingsHeight = 105;
        int whitelistMaxHeight = availableHeight - settingsHeight - 12 - 20;
        int whitelistMinHeight = 12 + 17 + 20 + 5 + 20 + 5 + LIST_HEIGHT + 10;
        int whitelistHeight = Math.max(whitelistMinHeight, Math.min(whitelistMaxHeight, whitelistMinHeight + 50));
        
        // 总内容高度 = 设置卡片 + 间距 + 白名单卡片
        int totalContentHeight = settingsHeight + 12 + whitelistHeight;
        
        int maxScroll = Math.max(0, totalContentHeight - availableHeight);
        return maxScroll;
    }
    
    @Override
    public void repositionElements() {
        // Don't call init() - just update positions
        updateWidgetPositions();
    }
    
    private void updateWidgetPositions() {
        int centerX = this.width / 2;
        int cardWidth = Math.min(500, Math.max(350, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        int padding = 16;
        
        // Settings card base position
        int settingsCardY = 45;
        int baseY = settingsCardY - scrollOffset;
        
        // Update checkbox positions
        int checkboxY = baseY + 30;
        int checkboxWidth = (cardWidth - padding * 4) / 3;
        
        if (this.enabledCheckbox != null) {
            this.enabledCheckbox.setPosition(cardX + padding, checkboxY);
        }
        if (this.showMessageCheckbox != null) {
            this.showMessageCheckbox.setPosition(cardX + padding + checkboxWidth + padding, checkboxY);
        }
        if (this.onlyNaturalCheckbox != null) {
            this.onlyNaturalCheckbox.setPosition(cardX + padding + (checkboxWidth + padding) * 2, checkboxY);
        }
        
        // Update slider position
        int sliderY = baseY + 60;
        if (this.intervalSlider != null) {
            this.intervalSlider.setPosition(cardX + padding, sliderY);
            this.intervalSlider.setWidth(cardWidth - padding * 2);
        }
        
        // Update whitelist section positions
        int whitelistCardY = settingsCardY + 105 + 12;
        int whitelistY = whitelistCardY + 30 - scrollOffset;
        
        int inputWidth = cardWidth - padding * 2 - 100;
        if (this.whitelistInput != null) {
            this.whitelistInput.setPosition(cardX + padding, whitelistY);
            this.whitelistInput.setWidth(inputWidth);
        }
        
        // Update search input position
        int searchY = whitelistY + 25;
        int searchWidth = cardWidth - padding * 2;
        if (this.searchInput != null) {
            this.searchInput.setPosition(cardX + padding, searchY);
            this.searchInput.setWidth(searchWidth);
        }
        
        int buttonX = cardX + padding + inputWidth + 5;
        if (this.addButton != null) {
            this.addButton.setPosition(buttonX, whitelistY);
        }
        if (this.removeButton != null) {
            this.removeButton.setPosition(buttonX + 50, whitelistY);
        }
        
        // Bottom buttons (fixed position)
        int bottomY = this.height - 40;
        int buttonWidth = 120;
        
        if (this.applyButton != null) {
            this.applyButton.setPosition(centerX - buttonWidth - 5, bottomY);
        }
        if (this.backButton != null) {
            this.backButton.setPosition(centerX + 5, bottomY);
        }
    }

    private void updateSuggestions(String input) {
        suggestions.clear();
        suggestionScroll = 0;
        
        if (input.trim().isEmpty()) {
            return;
        }
        
        String lowerInput = input.toLowerCase();
        
        // Search through all registered items with enhanced matching
        for (ResourceLocation location : BuiltInRegistries.ITEM.keySet()) {
            String itemId = location.toString();
            String namespace = location.getNamespace(); // e.g. "minecraft"
            String path = location.getPath(); // e.g. "diamond_pickaxe"
            
            // Get the item to check its display name
            Item item = BuiltInRegistries.ITEM.getValue(location);
            String displayName = "";
            String displayNamePinyin = "";
            String displayNamePinyinInitials = "";
            if (item != null) {
                try {
                    displayName = new ItemStack(item).getHoverName().getString().toLowerCase();
                    // Convert Chinese name to Pinyin for searching
                    displayNamePinyin = PinyinHelper.toPinyin(displayName);
                    displayNamePinyinInitials = PinyinHelper.toPinyinInitials(displayName);
                } catch (Exception e) {
                    displayName = "";
                }
            }
            
            // Match against multiple criteria:
            // 1. Full item ID (minecraft:diamond_pickaxe)
            // 2. Namespace only (minecraft)
            // 3. Path only (diamond_pickaxe)
            // 4. Display name (钻石镐 or Diamond Pickaxe)
            // 5. Pinyin (zuanshigao for 钻石镐)
            // 6. Pinyin initials (zsg for 钻石镐)
            boolean matches = itemId.toLowerCase().contains(lowerInput) ||
                            namespace.toLowerCase().contains(lowerInput) ||
                            path.toLowerCase().contains(lowerInput) ||
                            displayName.contains(lowerInput) ||
                            displayNamePinyin.contains(lowerInput) ||
                            displayNamePinyinInitials.contains(lowerInput);
            
            if (matches) {
                suggestions.add(itemId);
                if (suggestions.size() >= 50) { // Limit suggestions
                    break;
                }
            }
        }
        
        // Sort suggestions: prioritize exact matches and starts-with matches
        suggestions.sort((a, b) -> {
            boolean aStarts = a.toLowerCase().startsWith(lowerInput);
            boolean bStarts = b.toLowerCase().startsWith(lowerInput);
            if (aStarts && !bStarts) return -1;
            if (!aStarts && bStarts) return 1;
            return a.compareTo(b);
        });
    }
    
    private void renderSuggestions(GuiGraphics graphics, int cardX, int cardY, int cardWidth, int mouseX, int mouseY) {
        if (suggestions.isEmpty()) return;
        
        // 清空并重新记录边界
        suggestionBounds.clear();
        
        int suggestX = cardX + 16;
        int suggestWidth = cardWidth - 32 - 100;
        int totalHeight = Math.min(suggestions.size(), MAX_SUGGESTIONS) * SUGGESTION_HEIGHT;
        // 提示框显示在输入框+按钮行的下方（可以遮挡搜索框）
        int suggestY = cardY + 30 + 20 + 3; // 输入框位置30 + 输入框高度20 + 小间距3
        
        // Push pose to render on top layer with higher Z-index
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400); // Move to high Z level (above items which render at ~200)
        
        // Suggestion box background with proper layering
        graphics.fill(suggestX - 1, suggestY - 1, suggestX + suggestWidth + 1, suggestY + totalHeight + 1, COLOR_OUTLINE); // Border
        graphics.fill(suggestX, suggestY, suggestX + suggestWidth, suggestY + totalHeight, 0xFF1A1A1A); // Background
        
        // Render suggestions
        int endIndex = Math.min(suggestionScroll + MAX_SUGGESTIONS, suggestions.size());
        for (int i = suggestionScroll; i < endIndex; i++) {
            String itemId = suggestions.get(i);
            int itemY = suggestY + (i - suggestionScroll) * SUGGESTION_HEIGHT;
            
            // 记录这个条目的精确边界
            suggestionBounds.add(new SuggestionBounds(i, suggestX, itemY, suggestX + suggestWidth, itemY + SUGGESTION_HEIGHT));
            
            boolean isHovered = mouseX >= suggestX && mouseX <= suggestX + suggestWidth &&
                               mouseY >= itemY && mouseY <= itemY + SUGGESTION_HEIGHT;
            
            // Hover highlight
            if (isHovered) {
                graphics.fill(suggestX, itemY, suggestX + suggestWidth, itemY + SUGGESTION_HEIGHT, COLOR_PRIMARY);
            }
            
            // Render item icon
            ResourceLocation location = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.getValue(location);
            String itemName = "";
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                graphics.renderItem(stack, suggestX + 2, itemY + 2);
                try {
                    itemName = stack.getHoverName().getString();
                } catch (Exception e) {
                    itemName = "";
                }
            }
            
            // Render item name (优先显示名称)
            int textX = suggestX + 20;
            int maxTextWidth = suggestWidth - 22;
            
            // 优先显示中文名称，如果没有则显示ID
            String displayText = !itemName.isEmpty() ? itemName : itemId;
            if (this.font.width(displayText) > maxTextWidth) {
                displayText = this.font.plainSubstrByWidth(displayText, maxTextWidth - 10) + "...";
            }
            graphics.drawString(this.font, displayText, textX, itemY + 7, isHovered ? 0xFFFFFF : COLOR_ON_SURFACE);
        }
        
        // Scroll indicator
        if (suggestions.size() > MAX_SUGGESTIONS) {
            String scrollText = String.format("%d/%d", Math.min(endIndex, suggestions.size()), suggestions.size());
            graphics.drawString(this.font, scrollText, suggestX + suggestWidth - 35, suggestY + totalHeight + 2, COLOR_ON_SURFACE_VARIANT);
        }
        
        // Pop pose to restore Z level
        graphics.pose().popPose();
    }
    
    private void filterWhitelist() {
        filteredWhitelist.clear();
        if (searchQuery.isEmpty()) {
            filteredWhitelist.addAll(whitelistItems);
        } else {
            for (String itemId : whitelistItems) {
                if (itemId.toLowerCase().contains(searchQuery)) {
                    filteredWhitelist.add(itemId);
                }
            }
        }
        selectedIndex = -1; // Reset selection when filtering
        whitelistScrollOffset = 0; // Reset scroll when filtering
    }
    
    private void addToWhitelist() {
        String input = this.whitelistInput.getValue().trim();
        if (input.isEmpty()) {
            return;
        }
        
        try {
            ResourceLocation location = ResourceLocation.parse(input);
            if (BuiltInRegistries.ITEM.getValue(location) != null) {
                if (!whitelistItems.contains(input)) {
                    Config.addToWhitelist(input);
                    Config.SPEC.save();
                    whitelistItems = new ArrayList<>(Config.WHITELIST.get());
                    this.whitelistInput.setValue("");
                    suggestions.clear();
                    
                    // Update filtered list
                    filterWhitelist();
                    
                    // Show feedback
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(
                            Component.translatable("SmartSweeper.config.whitelist.added", input),
                            true
                        );
                    }
                } else {
                    // Item already exists in whitelist
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(
                            Component.translatable("SmartSweeper.config.whitelist.already_added", input),
                            true
                        );
                    }
                }
            }
        } catch (Exception e) {
            SmartSweeper.LOGGER.warn("Invalid item ID: {}", input);
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                    Component.translatable("SmartSweeper.config.whitelist.invalid", input),
                    true
                );
            }
        }
    }

    private void removeFromWhitelist() {
        int indexToRemove = showContextMenu ? contextMenuItemIndex : selectedIndex;
        if (indexToRemove >= 0 && indexToRemove < filteredWhitelist.size()) {
            String itemId = filteredWhitelist.get(indexToRemove);
            Config.removeFromWhitelist(itemId);
            Config.SPEC.save();
            whitelistItems = new ArrayList<>(Config.WHITELIST.get());
            selectedIndex = -1;
            contextMenuItemIndex = -1;
            showContextMenu = false;
            
            // Update filtered list
            filterWhitelist();
            
            // Show feedback
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                    Component.translatable("SmartSweeper.config.whitelist.removed", itemId),
                    true
                );
            }
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
    
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Custom background rendering
    }
    
    private void drawCard(GuiGraphics graphics, int x, int y, int width, int height) {
        // Card shadow
        graphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x40000000);
        
        // Card background
        graphics.fill(x, y, x + width, y + height, COLOR_SURFACE_VARIANT);
        
        // Card outline
        drawOutline(graphics, x, y, width, height, COLOR_OUTLINE);
    }
    
    private void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}
