# fakeneko_config 接入指南

`fakeneko_config` 是一个轻量级、类似 MaLiLib 的 Minecraft 配置库，支持 Fabric 与 NeoForge 双平台。它提供纯 Java API、Gson JSON 序列化、自动配置 GUI 以及组合键热键支持。

## 目录

- [支持平台](#支持平台)
- [支持类型](#支持类型)
- [依赖接入](#依赖接入)
- [基本用法](#基本用法)
- [配置项类型](#配置项类型)
- [组合键热键](#组合键热键)
- [变更监听](#变更监听)
- [配置 GUI](#配置-gui)
- [加载与保存](#加载与保存)
- [构建与打包](#构建与打包)

## 支持平台

支持 Fabric（1.26.2）与 NeoForge（1.26.2）双平台。本库只依赖 Minecraft 与 Gson，不依赖 Fabric API 或 NeoForge API 本身。

> **注意 modId 命名**：`fakeneko_config` 的 modId 使用下划线 `fakeneko_config`，因为 NeoForge 的 modId 不允许短横线 `-`。如果你使用本库作为依赖，也请确保你的 modId 符合 NeoForge 的 `^[a-z][a-z0-9_]*$` 规范。

## 支持类型

| 类型 | 对应类 | 编辑控件 |
|---|---|---|
| Boolean | `BooleanConfig` | 开关按钮 |
| Integer | `IntegerConfig` | 滑块（带范围） |
| Double | `DoubleConfig` | 滑块（带范围） |
| String | `StringConfig` | 文本输入框 |
| String List | `StringListConfig` | 逗号分隔文本输入框 |
| Hotkey | `HotkeyConfig` | 热键绑定按钮，支持组合键 |

## 依赖接入

### 方式一：本地项目依赖（推荐用于开发/测试）

在 `settings.gradle` 中引入 `fakeneko_config` 项目：

```gradle
includeBuild('/path/to/fakeneko_config') {
    dependencySubstitution {
        substitute module('cn.com.fakeneko:fakeneko_config') using project(':common')
    }
}
```

然后在你的 Fabric/NeoForge 模块中：

```gradle
dependencies {
    implementation('cn.com.fakeneko:fakeneko_config')
}
```

### 方式二：Maven 本地发布

在 `fakeneko_config` 目录下运行：

```bash
./gradlew publishToMavenLocal
```

发布后在你的项目 `build.gradle` 中：

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation('cn.com.fakeneko:fakeneko_config-26.2:26.2.0.0')
}
```

> 注意：根据实际 `base.archivesName` 调整 artifact 名称。

### 方式三：本地 JAR 文件

构建完成后 `common/build/libs/` 目录会生成 JAR，可将其直接放入你的项目 `libs/` 或作为 `compileOnly` 依赖。

## 基本用法

### 1. 创建 ConfigManager

```java
import cn.com.fakeneko.config.api.ConfigManager;
import cn.com.fakeneko.config.impl.ConfigManagerImpl;
import net.minecraft.network.chat.Component;

public class MyConfig {
    public static final ConfigManager MANAGER = new ConfigManagerImpl(
        "my_mod_id",
        Component.translatable("config.my_mod_id.title")  // 配置界面标题，可在语言文件中翻译
    );

    static {
        MANAGER.load();
    }

    public static void init() {
        // 在 Mod 入口调用
    }
}
```

`ConfigManagerImpl` 默认将配置文件保存为 `config/my_mod_id.json`。`ConfigScreen` 的标题会显示为传入的 `displayName`。

标题既可以使用 `Component.translatable` 在语言文件中翻译，也可以直接写死：

```java
new ConfigManagerImpl(
    "my_mod_id",
    Component.literal("My Mod Config")  // 固定文字
);
```

如果不传入 `displayName`，则默认使用翻译键 `config.my_mod_id.title`：

```java
public static final ConfigManager MANAGER = new ConfigManagerImpl("my_mod_id");
```

> 请确保你的 modId 符合 NeoForge 规范，例如使用 `my_mod_id` 而不是 `my-mod-id`。

### 2. 创建分类并注册配置项

```java
import cn.com.fakeneko.config.api.ConfigCategory;
import cn.com.fakeneko.config.impl.types.BooleanConfig;
import cn.com.fakeneko.config.impl.types.IntegerConfig;
import net.minecraft.network.chat.Component;

public class MyConfig {
    public static final ConfigManager MANAGER = new ConfigManagerImpl(
        "my_mod_id",
        Component.translatable("config.my_mod_id.title")
    );

    public static final ConfigCategory GENERAL = MANAGER.createCategory(
        "general",
        Component.translatable("config.my_mod_id.category.general")
    );

    public static final BooleanConfig ENABLED = new BooleanConfig(
        "enabled",
        Component.translatable("config.my_mod_id.enabled"),
        GENERAL,
        true
    ).withHotkey(
        Component.translatable("config.my_mod_id.enabled_hotkey"),
        Identifier.fromNamespaceAndPath("my_mod_id", "enabled"),
        InputKeys.of(InputConstants.KEY_H)
    );
    public static final IntegerConfig MAX_COUNT = new IntegerConfig(
        "max_count",
        Component.translatable("config.my_mod_id.max_count"),
        GENERAL,
        5,
        1,
        20
    );

    static {
        MANAGER.load();
    }
}
```

配置项会自动注册到所在分类中，无需手动再调用 `addConfig`。

## 配置项类型

### BooleanConfig

```java
BooleanConfig enabled = new BooleanConfig(
    "enabled",
    Component.translatable("config.my_mod_id.enabled"),
    GENERAL,
    true
).withHotkey(
    Component.translatable("config.my_mod_id.enabled_hotkey"),
    Identifier.fromNamespaceAndPath("my_mod_id", "enabled"),
    InputKeys.of(InputConstants.KEY_H)
);
enabled.toggle();      // 切换布尔值
boolean value = enabled.get();
enabled.set(false);  // 直接设置
enabled.reset();       // 恢复默认值

// 热键按下时会自动调用 enabled.toggle()
// 在 GUI 中，布尔条目右侧会出现一个热键按钮。
```

### IntegerConfig

```java
IntegerConfig maxCount = new IntegerConfig(
    "max_count",
    Component.translatable("config.my_mod_id.max_count"),
    GENERAL,
    5,
    1,
    20
);
// 参数：名称、显示名、分类、默认值、最小值、最大值
maxCount.set(15);  // 超出范围会被自动 clamp
```

### DoubleConfig

```java
DoubleConfig speed = new DoubleConfig(
    "speed",
    Component.translatable("config.my_mod_id.speed"),
    GENERAL,
    1.0,
    0.1,
    5.0
);
speed.set(2.5);
```

### StringConfig

```java
StringConfig mode = new StringConfig(
    "mode",
    Component.translatable("config.my_mod_id.mode"),
    GENERAL,
    "default"
);
mode.set("advanced");
```

### StringListConfig

```java
StringListConfig blacklist = new StringListConfig(
    "blacklist",
    Component.translatable("config.my_mod_id.blacklist"),
    GENERAL,
    List.of("example:block")
);
blacklist.set(List.of("minecraft:stone", "minecraft:dirt"));
```

GUI 中编辑为逗号分隔的字符串。

### HotkeyConfig

见下文 [组合键热键](#组合键热键)。

## 组合键热键

### 定义热键配置项

```java
import com.mojang.blaze3d.platform.InputConstants;
import cn.com.fakeneko.config.impl.keybind.InputKeys;
import cn.com.fakeneko.config.impl.types.HotkeyConfig;
import net.minecraft.resources.Identifier;

public static final HotkeyConfig TOGGLE_HOTKEY = new HotkeyConfig(
    "toggle_hotkey",
    Component.translatable("config.my_mod_id.toggle_hotkey"),
    ADVANCED,
    InputKeys.ofKeys(InputConstants.KEY_LCONTROL, InputConstants.KEY_K),
    Identifier.fromNamespaceAndPath("my_mod_id", "toggle"),
    keybind -> {
        System.out.println("Ctrl+K pressed!");
    }
);
```

### 工厂方法

```java
// 键盘键（兼容旧版 of 别名）
InputKeys keys = InputKeys.of(InputConstants.KEY_K);
InputKeys keys = InputKeys.ofKeys(InputConstants.KEY_LCONTROL, InputConstants.KEY_K);

// 鼠标键
InputKeys mouseKeys = InputKeys.ofMouse(InputConstants.MOUSE_BUTTON_LEFT);

// 混合（不建议直接混合键盘与鼠标，但 API 允许）
InputKeys mixed = InputKeys.ofAll(
    InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LCONTROL),
    InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE)
);
```

### 组合键触发规则

- 组合键中所有键必须**同时被按住**（无序匹配）。
- 当**最新按下的键**是组合键中的最后一个键时，触发 `onPress` 回调。
- 释放组合键中任意一个键时，触发 `onRelease` 回调。

### GUI 输入

在 `ConfigScreen` 中点击热键配置项按钮，进入热键编辑界面：

- 点击上方的按键绑定区域，开始重新录制新的组合键。
- 录制过程中，依次按下要组合的所有键（例如先按住 Ctrl 再按 K）。
- 鼠标按键也会被记录（例如 Ctrl + 鼠标左键）。
- 点击上方按键绑定区域，可记录当前鼠标按键（如左键）。
- 点击外部空白处会停止录制，但保持在热键编辑界面。
- 点击 `Reset`（`重置`）恢复默认值。
- 按 `Enter` 或点击 `Done`（`完成`）保存并返回。
- 按 `Escape` 取消并返回。

## 变更监听

```java
enabled.addListener((config, from, to) -> {
    System.out.println("Enabled changed from " + from + " to " + to);
});

// 链式添加多个
enabled.addListeners(
    (config, from, to) -> LOGGER.info("changed: {}", to),
    (config, from, to) -> someAction()
);
```

监听器在 `set()` 值变更后调用。

## 配置 GUI

### 打开配置界面

```java
import cn.com.fakeneko.config.impl.gui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public static void openConfig(Screen parent) {
    Minecraft.getInstance().gui.setScreen(new ConfigScreen(parent, MyConfig.MANAGER));
}
```

### 界面功能

- 标题：使用 `ConfigManager.displayName()`，由各 mod 自己指定。
- 搜索框：按配置项标识符（`name`）或**翻译后的显示名称**过滤，支持中英文搜索。
- 分类标题：所有配置项按分类分组显示，分类名使用 `Component.translatable`。
- 配置项左侧显示 `Config.displayName()`，支持 `Component.translatable` 翻译。
- 各类型编辑控件：见 [支持类型](#支持类型)。
- 每个配置项右侧有 `Reset`（`重置`）按钮恢复默认值。
- 底部左侧为 `Cancel`（`取消`）直接返回，右侧为 `Done`（`完成`）保存并退出；`Done` 只有在配置发生修改时才能点击。

## 加载与保存

通常只需要在 `ConfigManager` 构造后调用一次 `load()`：

```java
static {
    MANAGER.load();
}
```

`ConfigScreen` 的 `Save` 按钮会自动调用 `MANAGER.save()`。

也可以手动调用：

```java
MANAGER.load();  // 从磁盘读取
MANAGER.save();  // 写入磁盘
MANAGER.resetAll(); // 重置所有配置为默认值
```

## 构建与打包

### 构建整个项目

```bash
./gradlew build
```

### 构建产物位置

- `common/build/libs/fakeneko_config-common-26.2-1.0.0.jar`
- `fabric/build/libs/fakeneko_config-fabric-26.2-1.0.0.jar`
- `neoforge/build/libs/fakeneko_config-neoforge-26.2-1.0.0.jar`

### 在 Fabric 中开发

入口类：

```java
public class MyModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MyConfig.init();
    }
}
```

### 在 NeoForge 中开发

入口类：

```java
@Mod("my_mod_id")
public class MyModNeoForge {
    public MyModNeoForge(IEventBus eventBus) {
        MyConfig.init();
    }
}
```

> 请确保 `@Mod` 中的 modId 与 `ConfigManagerImpl` 中一致，且符合 NeoForge 的命名规范（`^[a-z][a-z0-9_]*$`）。

---

如有问题，请查看 `common/src/test/java/cn/com/fakeneko/config/impl/ConfigSerializationTest.java` 中的示例测试。

作者：`fakeneko`（通过 `gradle.properties` 的 `mod_author` 配置）。
