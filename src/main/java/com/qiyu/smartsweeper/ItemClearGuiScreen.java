package com.qiyu.smartsweeper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ItemClearGuiScreen extends Screen {
    private final Screen parent;
    private Button toggleButton;
    private Button clearNowButton;
    private Button resetStatsButton;
    private Button settingsButton;
    private Button whitelistButton;
    private Button closeButton;
    
    private long totalItemsCleared;
    private long totalEntitiesCleared;
    private int clearCount;
    private int timeUntilClear;
    private boolean isEnabled;
    private int clearInterval;
    private int whitelistSize;
    
    private double scrollOffset = 0;
    private static final int CONTENT_HEIGHT = 360; // Total height of all content
    private static final int SCROLL_SPEED = 20;
    
    // MD3 Colors
    private static final int COLOR_PRIMARY = 0xFF6750A4;
    private static final int COLOR_SURFACE = 0xFF1C1B1F;
    private static final int COLOR_SURFACE_VARIANT = 0xFF2B2930;
    private static final int COLOR_ON_SURFACE = 0xFFE6E1E5;
    private static final int COLOR_ON_SURFACE_VARIANT = 0xFFCAC4D0;
    private static final int COLOR_SUCCESS = 0xFF4CAF50;
    private static final int COLOR_ERROR = 0xFFB3261E;
    private static final int COLOR_OUTLINE = 0xFF938F99;

    public ItemClearGuiScreen(Screen parent, long totalItems, long totalEntities, int count, 
                               int timeUntil, boolean enabled, int interval, int whitelistCount) {
        super(Component.translatable("SmartSweeper.gui.title"));
        this.parent = parent;
        this.totalItemsCleared = totalItems;
        this.totalEntitiesCleared = totalEntities;
        this.clearCount = count;
        this.timeUntilClear = timeUntil;
        this.isEnabled = enabled;
        this.clearInterval = interval;
        this.whitelistSize = whitelistCount;
    }

    @Override
    protected void init() {
        super.init();
        
        // Responsive layout calculations
        int centerX = this.width / 2;
        
        // Card width adapts to screen width (min 300, max 450)
        int cardWidth = Math.min(450, Math.max(300, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        
        // Calculate button sizes based on card width
        int padding = 16;
        int buttonWidth = cardWidth - (padding * 2);
        int buttonHeight = 24;
        int buttonSpacing = 8;
        
        // Base Y position with scroll offset applied
        int baseY = 40 - (int)scrollOffset;
        
        // Status card buttons start position
        int buttonY = baseY + 65;
        
        // Toggle button
        this.toggleButton = Button.builder(
            Component.translatable(isEnabled ? "SmartSweeper.gui.disable" : "SmartSweeper.gui.enable"),
            button -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.connection.sendCommand("itemclear toggle");
                    this.onClose();
                }
            })
            .bounds(cardX + padding, buttonY, buttonWidth, buttonHeight)
            .build();
        this.addRenderableWidget(this.toggleButton);
        
        // Clear now button
        this.clearNowButton = Button.builder(
            Component.translatable("SmartSweeper.gui.clear_now"),
            button -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.connection.sendCommand("itemclear now");
                    this.onClose();
                }
            })
            .bounds(cardX + padding, buttonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight)
            .build();
        this.addRenderableWidget(this.clearNowButton);
        
        // Settings and Whitelist buttons - side by side
        int smallButtonWidth = (buttonWidth - buttonSpacing) / 2;
        int rowY = buttonY + (buttonHeight + buttonSpacing) * 2;
        
        this.settingsButton = Button.builder(
            Component.translatable("SmartSweeper.gui.settings"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ItemClearConfigScreen(this));
                }
            })
            .bounds(cardX + padding, rowY, smallButtonWidth, buttonHeight)
            .build();
        this.addRenderableWidget(this.settingsButton);
        
        this.whitelistButton = Button.builder(
            Component.translatable("SmartSweeper.gui.whitelist"),
            button -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.connection.sendCommand("itemclear whitelist list");
                    this.onClose();
                }
            })
            .bounds(cardX + padding + smallButtonWidth + buttonSpacing, rowY, smallButtonWidth, buttonHeight)
            .build();
        this.addRenderableWidget(this.whitelistButton);
        
        // Stats card position
        int statsCardY = baseY + 190;
        
        // Reset stats button
        this.resetStatsButton = Button.builder(
            Component.translatable("SmartSweeper.gui.reset_stats"),
            button -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.connection.sendCommand("itemclear stats reset");
                    this.onClose();
                }
            })
            .bounds(cardX + padding, statsCardY + 95, buttonWidth, buttonHeight)
            .build();
        this.addRenderableWidget(this.resetStatsButton);
        
        // Close button at bottom (fixed position, no scroll)
        int closeButtonWidth = Math.min(200, cardWidth - padding * 2);
        this.closeButton = Button.builder(
            Component.translatable("gui.done"),
            button -> this.onClose())
            .bounds(centerX - closeButtonWidth / 2, this.height - 40, closeButtonWidth, buttonHeight)
            .build();
        this.addRenderableWidget(this.closeButton);
    }
    
    @Override
    public void repositionElements() {
        super.repositionElements();
        this.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw custom background (semi-transparent dark overlay)
        graphics.fill(0, 0, this.width, this.height, 0xC0000000);
        
        // Responsive calculations
        int centerX = this.width / 2;
        int cardWidth = Math.min(450, Math.max(300, this.width - 100));
        int cardX = centerX - cardWidth / 2;
        int padding = 16;
        
        // Draw fixed title
        graphics.drawCenteredString(this.font, this.title, centerX, 15, COLOR_ON_SURFACE);
        
        // Enable scissor for scrollable area
        int scrollAreaTop = 30;
        int scrollAreaBottom = this.height - 60;
        int scrollAreaHeight = scrollAreaBottom - scrollAreaTop;
        
        // Draw scroll indicators if needed
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            if (scrollOffset > 0) {
                graphics.drawCenteredString(this.font, "▲", centerX, scrollAreaTop + 2, COLOR_ON_SURFACE_VARIANT);
            }
            if (scrollOffset < maxScroll) {
                graphics.drawCenteredString(this.font, "▼", centerX, scrollAreaBottom - 10, COLOR_ON_SURFACE_VARIANT);
            }
            drawScrollbar(graphics, maxScroll, scrollAreaTop, scrollAreaHeight);
        }
        
        // Enable clipping for scrollable content
        graphics.enableScissor(0, scrollAreaTop, this.width, scrollAreaBottom);
        
        // Apply scroll offset to content
        int baseY = scrollAreaTop + 10 - (int)scrollOffset;
        
        // ===== Status Card =====
        int statusCardY = baseY;
        int statusCardHeight = 170;
        drawCard(graphics, cardX, statusCardY, cardWidth, statusCardHeight);
        
        // Status card header
        graphics.drawString(this.font,
            Component.translatable("SmartSweeper.gui.status_title"),
            cardX + padding, statusCardY + 12, COLOR_ON_SURFACE);
        
        // Status indicator
        int statusY = statusCardY + 32;
        int statusColor = isEnabled ? COLOR_SUCCESS : COLOR_ERROR;
        graphics.fill(cardX + padding, statusY + 2, cardX + padding + 6, statusY + 8, statusColor);
        
        graphics.drawString(this.font,
            Component.translatable(isEnabled ? "SmartSweeper.gui.enabled" : "SmartSweeper.gui.disabled"),
            cardX + padding + 10, statusY, statusColor);
        
        // Interval info
        String intervalText = "⏱ " + clearInterval + "s";
        graphics.drawString(this.font, intervalText, cardX + padding + 100, statusY, COLOR_ON_SURFACE_VARIANT);
        
        // Next clear countdown
        if (isEnabled && timeUntilClear >= 0) {
            int minutes = timeUntilClear / 60;
            int seconds = timeUntilClear % 60;
            String countdown = String.format("▶ %d:%02d", minutes, seconds);
            int countdownX = cardX + cardWidth - padding - this.font.width(countdown);
            graphics.drawString(this.font, countdown, countdownX, statusY, COLOR_PRIMARY);
        }
        
        // Divider
        graphics.fill(cardX + padding, statusCardY + 52, cardX + cardWidth - padding, statusCardY + 53, COLOR_OUTLINE);
        
        // Render buttons inside status card
        renderScrollableButtons(graphics, mouseX, mouseY, statusCardY, statusCardY + statusCardHeight);
        
        // ===== Statistics Card =====
        int statsCardY = statusCardY + statusCardHeight + 16;
        int statsCardHeight = 130;
        drawCard(graphics, cardX, statsCardY, cardWidth, statsCardHeight);
        
        // Stats card header
        graphics.drawString(this.font,
            Component.translatable("SmartSweeper.gui.statistics"),
            cardX + padding, statsCardY + 12, COLOR_ON_SURFACE);
        
        // Stats grid
        int statsContentY = statsCardY + 35;
        int col1X = cardX + padding;
        int col2X = cardX + cardWidth / 2 + 8;
        int rowHeight = 30;
        
        // Row 1
        drawStat(graphics, col1X, statsContentY, "📦", 
            Component.translatable("SmartSweeper.gui.stat_items"), 
            formatNumber(totalItemsCleared), cardWidth / 2 - padding - 8);
        drawStat(graphics, col2X, statsContentY, "🎯", 
            Component.translatable("SmartSweeper.gui.stat_entities"), 
            formatNumber(totalEntitiesCleared), cardWidth / 2 - padding - 8);
        
        // Row 2
        drawStat(graphics, col1X, statsContentY + rowHeight, "🔄", 
            Component.translatable("SmartSweeper.gui.stat_clears"), 
            String.valueOf(clearCount), cardWidth / 2 - padding - 8);
        drawStat(graphics, col2X, statsContentY + rowHeight, "🛡", 
            Component.translatable("SmartSweeper.gui.stat_whitelist"), 
            String.valueOf(whitelistSize), cardWidth / 2 - padding - 8);
        
        // Render reset button inside stats card
        renderScrollableButtons(graphics, mouseX, mouseY, statsCardY, statsCardY + statsCardHeight);
        
        // Disable scissor
        graphics.disableScissor();
        
        // Render fixed close button at bottom (outside scroll area)
        renderMD3Button(graphics, this.closeButton, mouseX, mouseY);
    }
    
    private void renderScrollableButtons(GuiGraphics graphics, int mouseX, int mouseY, int cardTop, int cardBottom) {
        // Only render buttons that are within the card boundaries
        for (var widget : this.renderables) {
            if (widget instanceof Button button && button != this.closeButton) {
                int buttonY = button.getY();
                // Check if button is within card area
                if (buttonY >= cardTop && buttonY <= cardBottom) {
                    renderMD3Button(graphics, button, mouseX, mouseY);
                }
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
        
        // Determine button color based on button text
        int buttonColor = COLOR_PRIMARY;
        int textColor = 0xFFFFFFFF;
        
        // Special colors for specific buttons
        String message = button.getMessage().getString();
        if (message.contains("禁用") || message.contains("Disable")) {
            buttonColor = COLOR_ERROR;
        } else if (message.contains("清理") && message.contains("立即")) {
            buttonColor = 0xFF2196F3; // Blue for action
        } else if (message.contains("重置") || message.contains("Reset")) {
            buttonColor = 0xFFFF9800; // Orange for reset
        } else if (message.contains("完成") || message.contains("Done")) {
            buttonColor = 0xFF607D8B; // Gray for close
        }
        
        // Button background with hover effect
        int alpha = isHovered ? 0xFF : 0xE0;
        int finalColor = (buttonColor & 0x00FFFFFF) | (alpha << 24);
        
        // Button shadow
        if (button.active) {
            graphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x40000000);
        }
        
        // Button background
        graphics.fill(x, y, x + width, y + height, finalColor);
        
        // Hover overlay
        if (isHovered && button.active) {
            graphics.fill(x, y, x + width, y + height, 0x30FFFFFF);
        }
        
        // Button outline
        if (button.active) {
            drawOutline(graphics, x, y, width, height, isHovered ? 0xFFFFFFFF : COLOR_OUTLINE);
        }
        
        // Button text
        int textX = x + width / 2;
        int textY = y + (height - 8) / 2;
        
        if (!button.active) {
            textColor = 0xFF666666;
        }
        
        graphics.drawCenteredString(this.font, button.getMessage(), textX, textY, textColor);
    }
    
    private void drawScrollbar(GuiGraphics graphics, int maxScroll, int top, int height) {
        if (maxScroll <= 0) return;
        
        int scrollbarX = this.width - 10;
        int scrollbarWidth = 4;
        
        // Scrollbar track
        graphics.fill(scrollbarX, top, scrollbarX + scrollbarWidth, top + height, 0x40FFFFFF);
        
        // Scrollbar thumb
        int thumbHeight = Math.max(20, height * height / (height + maxScroll));
        int thumbY = top + (int)((height - thumbHeight) * (scrollOffset / maxScroll));
        graphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, COLOR_PRIMARY);
    }
    
    private int getMaxScroll() {
        int scrollAreaHeight = this.height - 90; // 30 (top) + 60 (bottom)
        return Math.max(0, CONTENT_HEIGHT - scrollAreaHeight);
    }
    
    private void drawStat(GuiGraphics graphics, int x, int y, String icon, Component label, String value, int maxWidth) {
        // Icon
        graphics.drawString(this.font, icon, x, y, COLOR_ON_SURFACE);
        
        // Label
        String labelText = label.getString();
        if (this.font.width(labelText) > maxWidth - 20) {
            labelText = this.font.plainSubstrByWidth(labelText, maxWidth - 23) + "...";
        }
        graphics.drawString(this.font, labelText, x + 14, y, COLOR_ON_SURFACE_VARIANT);
        
        // Value (larger and below)
        graphics.drawString(this.font, value, x + 14, y + 12, COLOR_ON_SURFACE);
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
        // Top
        graphics.fill(x, y, x + width, y + 1, color);
        // Bottom
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        graphics.fill(x, y, x + 1, y + height, color);
        // Right
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_SPEED));
            this.repositionElements(); // Update button positions
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 不暂停游戏
    }
    
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Don't render default blurred background
        // We'll draw our own in render()
    }
}

