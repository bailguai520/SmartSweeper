# SmartSweeper - 智能物品清理模组

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.2-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.2.1--beta-blue)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)

[English](#english) | [中文](#中文)

---

## 中文

### 📖 模组简介

SmartSweeper 是一个功能强大且用户友好的 Minecraft 物品清理模组，专为 NeoForge 1.21.2 设计。它可以自动或手动清理服务器/单人世界中的掉落物品，帮助提升游戏性能并保持世界整洁。

### ✨ 主要特性

- 🔄 **自动定时清理** - 可配置的自动清理间隔（10-3600秒）
- ⏰ **清理倒计时** - 清理前10秒警告，最后5秒倒计时提示
- 📋 **物品白名单** - 保护重要物品不被清理
- 🎮 **图形化界面** - 现代化的配置界面，易于使用
- 🔍 **白名单搜索** - 支持拼音搜索，快速查找物品
- 📊 **统计系统** - 追踪已清理的物品数量和操作次数
- 💬 **多语言支持** - 内置中英文语言文件
- ⚙️ **灵活配置** - 可选择仅清理自然生成的物品
- 🎯 **精确控制** - 丰富的命令系统，适合服务器管理员

### 📦 安装方法

1. 确保已安装 [NeoForge 21.2.1-beta](https://neoforged.net/) 或更高版本
2. 下载 SmartSweeper mod 文件
3. 将 mod 文件放入 Minecraft 的 `mods` 文件夹
4. 启动游戏即可

### 🎮 使用指南

#### 打开配置界面

**方式一：游戏内**
- 进入游戏后按 `Esc` 打开菜单
- 点击 "模组" → 找到 "SmartSweeper" → 点击配置按钮

**方式二：命令**
```
/smartsweeper gui
```

#### 配置选项说明

| 选项 | 说明 | 默认值 |
|------|------|--------|
| **启用自动清理** | 是否启用定时自动清理 | 开启 |
| **显示清理消息** | 是否显示清理警告和完成消息 | 开启 |
| **仅清理自然物品** | 只清理非玩家掉落的物品 | 关闭 |
| **清理间隔** | 自动清理的时间间隔（秒） | 300秒 |
| **物品白名单** | 不会被清理的物品列表 | 钻石、下界合金锭、下界之星 |

#### 白名单管理

**添加物品：**
1. 在配置界面的输入框中输入物品ID（如 `minecraft:diamond`）
2. 点击 "添加" 按钮
3. 支持拼音搜索功能，输入 "zuanshi" 可以找到钻石

**移除物品：**
- 右键点击白名单中的物品，选择 "移除"

### 🔧 命令列表

所有命令需要**OP权限**（权限等级 2）。

#### 基础命令

```bash
# 打开图形界面
/smartsweeper gui

# 立即清理物品
/smartsweeper now

# 切换自动清理开关
/smartsweeper toggle

# 查看当前状态
/smartsweeper status
```

#### 间隔设置

```bash
# 设置清理间隔（秒）
/smartsweeper interval <秒数>

# 示例：设置为5分钟
/smartsweeper interval 300
```

#### 白名单管理

```bash
# 添加物品到白名单
/smartsweeper whitelist add <物品ID>

# 从白名单移除物品
/smartsweeper whitelist remove <物品ID>

# 查看白名单
/smartsweeper whitelist list

# 示例
/smartsweeper whitelist add minecraft:diamond
/smartsweeper whitelist remove minecraft:dirt
```

#### 统计信息

```bash
# 查看统计数据
/smartsweeper stats show

# 重置统计数据
/smartsweeper stats reset
```

### 📊 统计系统

模组会追踪以下数据：
- **清理的物品总数** - 按物品数量统计（不是堆叠数）
- **清理的实体总数** - 实际移除的物品实体数量
- **清理操作次数** - 自动和手动清理的总次数

### ⚙️ 配置文件

配置文件位于：`config/smartsweeper-common.toml`

```toml
[general]
    # 启用自动清理
    clearEnabled = true
    
    # 清理间隔（秒）
    clearInterval = 300
    
    # 显示清理消息
    showClearMessage = true
    
    # 仅清理自然生成的物品
    clearOnlyNatural = false
    
    # 物品白名单
    whitelist = [
        "minecraft:diamond",
        "minecraft:netherite_ingot",
        "minecraft:nether_star"
    ]
```

### 🎯 使用场景

#### 服务器管理员
- 设置合理的清理间隔，保持服务器性能
- 配置白名单保护稀有物品
- 使用命令快速调整清理策略

#### 单人游戏
- 通过图形界面轻松配置
- 在建筑或挖矿后快速清理杂物
- 保护重要物品不被误清

### 🔔 清理流程

1. **警告阶段**（清理前10秒）
   - 发送警告消息：`⚠ 物品将在 10 秒后被清理！`

2. **倒计时阶段**（最后5秒）
   - 每秒在动作栏显示倒计时：`⏱ 清理倒计时 5...`

3. **清理阶段**
   - 移除所有非白名单物品
   - 显示完成消息：`✓ 已清理 123 个地面物品`

### ❓ 常见问题

**Q: 如何保护某些物品不被清理？**  
A: 将物品ID添加到白名单中。可以通过GUI或命令 `/smartsweeper whitelist add <物品ID>` 添加。

**Q: 刚扔出的物品会被立即清理吗？**  
A: 不会。模组会忽略1秒内刚掉落的物品。

**Q: "仅清理自然物品" 是什么意思？**  
A: 启用后，只清理自然生成的物品（如怪物掉落、矿石），不清理玩家手动扔出的物品。

**Q: 可以在单人游戏中使用吗？**  
A: 可以！模组同时支持单人和多人游戏。

**Q: 如何完全禁用自动清理？**  
A: 使用命令 `/smartsweeper toggle` 或在GUI中取消勾选"启用自动清理"。

**Q: 配置文件在哪里？**  
A: 位于 `.minecraft/config/smartsweeper-common.toml`

### 🐛 问题反馈

如果遇到问题或有建议，请在 [GitHub Issues](https://github.com/yourusername/smartsweeper/issues) 提交。

### 📝 更新日志

#### v1.0.0 (2025-10-02)
- ✨ 初始版本发布
- ✅ 自动定时清理功能
- ✅ 图形化配置界面
- ✅ 物品白名单系统
- ✅ 拼音搜索支持
- ✅ 完整的命令系统
- ✅ 统计功能
- ✅ 中英文双语支持

### 📄 许可证

All Rights Reserved

---

## English

### 📖 Description

SmartSweeper is a powerful and user-friendly Minecraft item clearing mod designed for NeoForge 1.21.2. It can automatically or manually clear dropped items in servers/single-player worlds, helping to improve game performance and keep your world tidy.

### ✨ Key Features

- 🔄 **Auto-Timed Clearing** - Configurable auto-clear interval (10-3600 seconds)
- ⏰ **Clear Countdown** - 10-second warning before clearing, 5-second countdown
- 📋 **Item Whitelist** - Protect important items from being cleared
- 🎮 **GUI Interface** - Modern configuration interface, easy to use
- 🔍 **Whitelist Search** - Supports Pinyin search for quick item lookup
- 📊 **Statistics System** - Track cleared items and operation count
- 💬 **Multi-language** - Built-in English and Chinese language files
- ⚙️ **Flexible Config** - Option to only clear naturally spawned items
- 🎯 **Precise Control** - Rich command system for server administrators

### 📦 Installation

1. Ensure [NeoForge 21.2.1-beta](https://neoforged.net/) or higher is installed
2. Download the SmartSweeper mod file
3. Place the mod file in the Minecraft `mods` folder
4. Launch the game

### 🎮 Usage Guide

#### Open Configuration Screen

**Method 1: In-Game**
- Press `Esc` to open the menu
- Click "Mods" → Find "SmartSweeper" → Click configuration button

**Method 2: Command**
```
/smartsweeper gui
```

#### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| **Enable Auto-Clear** | Enable scheduled auto-clearing | Enabled |
| **Show Clear Messages** | Show warning and completion messages | Enabled |
| **Only Clear Natural Items** | Only clear non-player dropped items | Disabled |
| **Clear Interval** | Auto-clear time interval (seconds) | 300s |
| **Item Whitelist** | List of items that won't be cleared | Diamond, Netherite Ingot, Nether Star |

#### Whitelist Management

**Add Items:**
1. Enter item ID in the input box (e.g., `minecraft:diamond`)
2. Click "Add" button
3. Supports Pinyin search (e.g., "zuanshi" finds diamond in Chinese)

**Remove Items:**
- Right-click an item in the whitelist and select "Remove"

### 🔧 Command List

All commands require **OP permission** (level 2).

#### Basic Commands

```bash
# Open GUI
/smartsweeper gui

# Clear items immediately
/smartsweeper now

# Toggle auto-clear
/smartsweeper toggle

# View current status
/smartsweeper status
```

#### Interval Settings

```bash
# Set clear interval (seconds)
/smartsweeper interval <seconds>

# Example: Set to 5 minutes
/smartsweeper interval 300
```

#### Whitelist Management

```bash
# Add item to whitelist
/smartsweeper whitelist add <item_id>

# Remove item from whitelist
/smartsweeper whitelist remove <item_id>

# View whitelist
/smartsweeper whitelist list

# Examples
/smartsweeper whitelist add minecraft:diamond
/smartsweeper whitelist remove minecraft:dirt
```

#### Statistics

```bash
# View statistics
/smartsweeper stats show

# Reset statistics
/smartsweeper stats reset
```

### 📊 Statistics System

The mod tracks:
- **Total Items Cleared** - By item count (not stacks)
- **Total Entities Cleared** - Actual item entities removed
- **Clear Operations** - Total auto and manual clear count

### ⚙️ Configuration File

Located at: `config/smartsweeper-common.toml`

```toml
[general]
    # Enable auto-clear
    clearEnabled = true
    
    # Clear interval (seconds)
    clearInterval = 300
    
    # Show clear messages
    showClearMessage = true
    
    # Only clear naturally spawned items
    clearOnlyNatural = false
    
    # Item whitelist
    whitelist = [
        "minecraft:diamond",
        "minecraft:netherite_ingot",
        "minecraft:nether_star"
    ]
```

### 🎯 Use Cases

#### Server Administrators
- Set reasonable intervals to maintain performance
- Configure whitelist to protect rare items
- Use commands to quickly adjust clearing strategy

#### Single-Player
- Easy configuration through GUI
- Quick cleanup after building or mining
- Protect important items from accidental clearing

### 🔔 Clearing Process

1. **Warning Phase** (10 seconds before)
   - Warning message: `⚠ Items will be cleared in 10 seconds!`

2. **Countdown Phase** (Last 5 seconds)
   - Action bar countdown: `⏱ Clearing items in 5...`

3. **Clearing Phase**
   - Remove all non-whitelisted items
   - Completion message: `✓ Cleared 123 items from the ground`

### ❓ FAQ

**Q: How to protect certain items from being cleared?**  
A: Add item IDs to the whitelist via GUI or command `/smartsweeper whitelist add <item_id>`.

**Q: Will just-dropped items be cleared immediately?**  
A: No. The mod ignores items dropped within 1 second.

**Q: What does "Only Clear Natural Items" mean?**  
A: When enabled, only naturally spawned items (mob drops, ores) are cleared, not player-dropped items.

**Q: Can I use this in single-player?**  
A: Yes! The mod works in both single-player and multiplayer.

**Q: How to completely disable auto-clearing?**  
A: Use command `/smartsweeper toggle` or uncheck "Enable Auto-Clear" in GUI.

**Q: Where is the config file?**  
A: Located at `.minecraft/config/smartsweeper-common.toml`

### 🐛 Bug Reports

If you encounter issues or have suggestions, please submit at [GitHub Issues](https://github.com/yourusername/smartsweeper/issues).

### 📝 Changelog

#### v1.0.0 (2025-10-02)
- ✨ Initial release
- ✅ Auto-timed clearing feature
- ✅ GUI configuration interface
- ✅ Item whitelist system
- ✅ Pinyin search support
- ✅ Complete command system
- ✅ Statistics feature
- ✅ Bilingual support (EN/CN)

### 📄 License

All Rights Reserved

---

### 🙏 Credits

Developed with ❤️ for the Minecraft community.

**Dependencies:**
- Minecraft 1.21.2
- NeoForge 21.2.1-beta

**Special Thanks:**
- NeoForge Team
- Minecraft Modding Community
