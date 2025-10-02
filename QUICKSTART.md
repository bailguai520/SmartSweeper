# SmartSweeper - 快速开始指南

## 🚀 5分钟上手

### 1️⃣ 安装模组

1. 下载并安装 [NeoForge 21.2.1-beta](https://neoforged.net/)
2. 将 SmartSweeper 放入 `mods` 文件夹
3. 启动游戏

### 2️⃣ 基础使用

#### 打开配置界面
```
按 ESC → 模组 → SmartSweeper → 配置
或输入命令：/smartsweeper gui
```

#### 默认设置
- ✅ 自动清理：**已启用**
- ⏰ 清理间隔：**5分钟（300秒）**
- 📋 白名单物品：钻石、下界合金锭、下界之星

### 3️⃣ 常用操作

#### 立即清理物品
```bash
/smartsweeper now
```

#### 开关自动清理
```bash
/smartsweeper toggle
```

#### 修改清理间隔
```bash
# 改为10分钟
/smartsweeper interval 600

# 改为1小时
/smartsweeper interval 3600
```

#### 添加白名单
```bash
# 方式1：命令
/smartsweeper whitelist add minecraft:emerald

# 方式2：GUI
打开配置界面 → 输入物品ID → 点击添加
```

### 4️⃣ 推荐配置

#### 🏠 单人游戏
```toml
clearEnabled = true
clearInterval = 300      # 5分钟
showClearMessage = true
clearOnlyNatural = false # 清理所有物品
```

#### 🌐 小型服务器（5-10人）
```toml
clearEnabled = true
clearInterval = 600      # 10分钟
showClearMessage = true
clearOnlyNatural = false
```

#### 🏰 大型服务器（10+人）
```toml
clearEnabled = true
clearInterval = 300      # 5分钟
showClearMessage = true
clearOnlyNatural = true  # 只清理自然物品
```

#### ⛏️ 生存服务器
```toml
clearEnabled = true
clearInterval = 900      # 15分钟
showClearMessage = true
clearOnlyNatural = false
# 建议添加更多白名单物品
```

### 5️⃣ 白名单推荐

#### 必备物品
```
minecraft:diamond
minecraft:netherite_ingot
minecraft:nether_star
minecraft:elytra
minecraft:totem_of_undying
```

#### 稀有物品
```
minecraft:emerald
minecraft:ancient_debris
minecraft:dragon_egg
minecraft:echo_shard
minecraft:recovery_compass
```

#### 特殊物品
```
minecraft:enchanted_book
minecraft:music_disc_*
minecraft:trident
minecraft:heart_of_the_sea
```

### ⚡ 性能优化建议

- **小世界**：300-600秒间隔
- **中世界**：180-300秒间隔  
- **大世界**：120-180秒间隔
- **高人数服务器**：启用"仅清理自然物品"

### 🎯 快速故障排除

**问题：物品被误清**
- ✅ 添加到白名单
- ✅ 启用"仅清理自然物品"

**问题：性能卡顿**
- ✅ 减少清理间隔
- ✅ 增加白名单物品

**问题：看不到清理消息**
- ✅ 启用"显示清理消息"选项

### 📞 需要帮助？

查看完整文档：[README.md](README.md)

