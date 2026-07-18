# fakeneko_config

> This document is translated by AI and is for reference only.

A lightweight, MaLiLib-like Minecraft configuration library for Fabric and NeoForge. It provides a pure Java API, Gson JSON serialization, automatic configuration GUI, and combo-key hotkey support.

[中文](README.md)|English

## Features

- Pure Java, only depends on Minecraft and Gson
- Supports Boolean / Integer / Double / String / StringList / Hotkey / Enum config types
- Automatic JSON serialization and deserialization
- Automatic configuration GUI generation
- Supports categories (switchable via tabs), default values, reset, and change callbacks
- Config entries support a description (shown as a tooltip on hover)
- Supports combo-key hotkey binding
- Built-in search and a discard confirmation when cancelling with unsaved changes

## Why fakeneko_config

From a developer-friendliness perspective, `fakeneko_config` is much cleaner than traditional Cloth Config / YACL solutions.

### Comparison

| Aspect | Old ScreenBuilder / ScreenBuilderYacl | fakeneko_config |
| --- | --- | --- |
| Code size | Need to write two GUI implementations (Cloth + YACL) and detect which library is loaded | Register config entries and they render automatically |
| Dependency management | Depends on Cloth Config and YACL, must handle version compatibility and optional/suggests | Unified `fakeneko_config` GUI with fewer external dependencies |
| Config type extension | Need to write GUI bindings for each new type | Library already supports Boolean, Hotkey, etc.; just `new` them |
| Serialization | Write your own Gson save/load | Library saves/loads automatically |
| Hotkeys | Register and listen with `KeyMapping` yourself | Library listens to keyboard/mouse events automatically; `BooleanConfig.withHotkey` binds in one line |
| Config screen entry | Write your own Fabric ModMenu / NeoForge ModList adapter | Use `ConfigScreen` directly |

### Example

Old way to define a boolean config:

```java
public static ConfigOption<Boolean> enabled = new ConfigOption<>("enabled", false);
```

Then you need to write bindings in both ScreenBuilders and manage JSON saving yourself.

New way:

```java
public static BooleanConfig ENABLED = new BooleanConfig(
    "enabled",
    Component.translatable("config.my_mod.enabled"),
    GENERAL,
    true
).withHotkey(...);
```

One declaration handles:

- Config entry
- Display name
- Default value
- Save/load
- Config GUI
- Hotkey binding

So continuing with `fakeneko_config` is the right choice, with significantly lower maintenance cost.

## Table of Contents

- [Supported Platforms](#supported-platforms)
- [Supported Types](#supported-types)
- [Dependency Setup](#dependency-setup)
- [Basic Usage](#basic-usage)
- [Config Types](#config-types)
- [Combo-key Hotkeys](#combo-key-hotkeys)
- [Change Listeners](#change-listeners)
- [Config GUI](#config-gui)
- [Load & Save](#load--save)
- [Build & Package](#build--package)
- [Project Structure](#project-structure)
- [Supported Minecraft Versions](#supported-minecraft-versions)
- [License](#license)

## Supported Platforms

Supports both Fabric and NeoForge for Minecraft 26.2. The library only depends on Minecraft and Gson, not on Fabric API or NeoForge API itself.

> **Note on modId naming**: The `fakeneko_config` modId uses an underscore (`fakeneko_config`) because NeoForge modIds do not allow hyphens (`-`). If you use this library as a dependency, please make sure your modId also conforms to NeoForge's `^[a-z][a-z0-9_]*$` pattern.

## Supported Types

| Type | Class | Editor Widget |
| --- | --- | --- |
| Boolean | `BooleanConfig` | Toggle button (green for on, red for off) |
| Integer | `IntegerConfig` | Slider (with range) |
| Double | `DoubleConfig` | Slider (with range) |
| String | `StringConfig` | Text input box |
| String List | `StringListConfig` | Comma-separated text input box |
| Hotkey | `HotkeyConfig` | Hotkey binding button with combo-key support |
| Enum | `EnumConfig` | Cycle button or dropdown menu (2~8 enum values) |

## Dependency Setup

### Option 1: Local Composite Build (recommended for development/testing)

Include the `fakeneko_config` project in your `settings.gradle`:

```gradle
includeBuild('/path/to/fakeneko_config') {
    dependencySubstitution {
        substitute module('cn.com.fakeneko:fakeneko_config') using project(':common')
    }
}
```

Then in your Fabric/NeoForge module:

```gradle
dependencies {
    implementation('cn.com.fakeneko:fakeneko_config')
}
```

### Option 2: Publish to Maven Local

Run in the `fakeneko_config` directory:

```bash
./gradlew publishToMavenLocal
```

Then in your project's `build.gradle`:

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation('cn.com.fakeneko:fakeneko_config-26.2:26.2.0.0')
}
```

> Note: Adjust the artifact name according to the actual `base.archivesName`.

### Option 3: Local JAR Files

After building, JARs are generated in `common/build/libs/`. You can drop them into your project's `libs/` or use them as a `compileOnly` dependency.

## Basic Usage

### 1. Create a ConfigManager

```java
import cn.com.fakeneko.config.api.ConfigManager;
import cn.com.fakeneko.config.impl.ConfigManagerImpl;
import net.minecraft.network.chat.Component;

public class MyConfig {
    public static final ConfigManager MANAGER = new ConfigManagerImpl(
        "my_mod_id",
        Component.translatable("config.my_mod_id.title")  // Config screen title, can be translated in lang files
    );

    static {
        MANAGER.load();
    }

    public static void init() {
        // Called from your mod entrypoint
    }
}
```

`ConfigManagerImpl` saves the config file to `config/my_mod_id.json` by default. The `ConfigScreen` title is displayed from the `displayName`.

The title can be translated with `Component.translatable`, or hardcoded with `Component.literal`:

```java
new ConfigManagerImpl(
    "my_mod_id",
    Component.literal("My Mod Config")  // Fixed text
);
```

If you omit `displayName`, it defaults to the translation key `config.my_mod_id.title`:

```java
public static final ConfigManager MANAGER = new ConfigManagerImpl("my_mod_id");
```

> Make sure your modId conforms to NeoForge naming, e.g. `my_mod_id` instead of `my-mod-id`.

### 2. Create Categories and Register Config Entries

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

Config entries are automatically registered into their category; you do not need to call `addConfig` manually.

## Config Types

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
enabled.withDescription(Component.translatable("config.my_mod_id.enabled.desc")); // Tooltip shown on hover
enabled.toggle();      // Toggle the boolean
boolean value = enabled.get();
enabled.set(false);  // Set directly
enabled.reset();       // Reset to default

// The hotkey automatically calls enabled.toggle() when pressed
// In the GUI, a hotkey button appears on the right of the boolean row.
```

All config types support `withDescription(Component)` to set a description shown as a tooltip when hovering the entry; nothing is shown if unset. Since this method returns the base type, call it separately after declaration (as above) to keep the concrete subtype.

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
// Arguments: name, display name, category, default, min, max
maxCount.set(15);  // Out-of-range values are clamped automatically
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

In the GUI this is edited as a comma-separated string.

### EnumConfig

```java
public enum RenderMode { FANCY, FAST, FABULOUS }

EnumConfig<RenderMode> renderMode = new EnumConfig<>(
    "render_mode",
    Component.translatable("config.my_mod_id.render_mode"),
    GENERAL,
    RenderMode.FANCY
);
// Defaults to CYCLIC button - click to cycle through values

// Use dropdown menu:
EnumConfig<RenderMode> renderMode = new EnumConfig<>(
    "render_mode",
    Component.translatable("config.my_mod_id.render_mode"),
    GENERAL,
    RenderMode.FANCY,
    EnumWidget.DROPDOWN
);

// Custom display text:
EnumConfig<RenderMode> renderMode = new EnumConfig<>(
    "render_mode",
    Component.translatable("config.my_mod_id.render_mode"),
    GENERAL,
    RenderMode.FANCY,
    EnumWidget.CYCLIC,
    value -> Component.translatable("config.my_mod_id.render_mode." + value.name().toLowerCase())
);
renderMode.set(RenderMode.FAST);
```

Enum value count is limited to 2~8. The dropdown menu (`DROPDOWN`) shows all options in a scrollable list, with the current value highlighted in aqua with a `✓` and greyed out.

### HotkeyConfig

See [Combo-key Hotkeys](#combo-key-hotkeys) below.

## Combo-key Hotkeys

### Define a Hotkey Config Entry

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

### Factory Methods

```java
// Keyboard keys (of is an alias for ofKeys for backwards compatibility)
InputKeys keys = InputKeys.of(InputConstants.KEY_K);
InputKeys keys = InputKeys.ofKeys(InputConstants.KEY_LCONTROL, InputConstants.KEY_K);

// Mouse buttons
InputKeys mouseKeys = InputKeys.ofMouse(InputConstants.MOUSE_BUTTON_LEFT);

// Mixed (not recommended, but the API allows it)
InputKeys mixed = InputKeys.ofAll(
    InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LCONTROL),
    InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE)
);
```

### Combo-key Trigger Rules

- All keys in the combo must be held **at the same time** (order does not matter).
- When the **last pressed key** completes the combo, the `onPress` callback is triggered.
- When any key in the combo is released, the `onRelease` callback is triggered.

### GUI Input

In `ConfigScreen`, click the hotkey config entry button to enter the hotkey editing screen:

- Click the key-binding area at the top to start recording a new combo.
- While recording, press all keys you want in the combo (e.g. hold Ctrl then press K).
- Mouse buttons are also recorded (e.g. Ctrl + left mouse button).
- Click the key-binding area again to record the current mouse button (e.g. left click).
- Clicking outside the key-binding area stops recording but stays on the hotkey screen.
- Click `Reset` to restore the default value.
- Press `Enter` or click `Done` to save and return.
- Press `Escape` to cancel and return.

## Change Listeners

```java
enabled.addListener((config, from, to) -> {
    System.out.println("Enabled changed from " + from + " to " + to);
});

// Chain multiple listeners
enabled.addListeners(
    (config, from, to) -> LOGGER.info("changed: {}", to),
    (config, from, to) -> someAction()
);
```

Listeners are called after `set()` changes the value.

## Config GUI

### Open the Config Screen

```java
import cn.com.fakeneko.config.impl.gui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public static void openConfig(Screen parent) {
    Minecraft.getInstance().gui.setScreen(new ConfigScreen(parent, MyConfig.MANAGER));
}
```

### Screen Features

- Title: uses `ConfigManager.displayName()`, each mod can provide its own.
- Category tabs: multiple categories are switched via tabs at the top; the selected tab is highlighted (greyed). The tab row is hidden automatically when there is only one category.
- Search box: filters by config identifier (`name`) or translated display name, supports Chinese and English. Search is **global across all categories**; while searching, all tabs are greyed out and disabled to indicate results are not limited to the current category.
- Entry description: entries with a `description()` set show a tooltip on hover.
- Left side shows `Config.displayName()`, supports `Component.translatable` translations.
- Editor widgets for each type: see [Supported Types](#supported-types).
- Each config entry has a `Reset` button on the right to restore the default value; disabled when already at default.
- Modified config entries are highlighted with a translucent sky-blue background.
- Boolean toggle buttons: green text for on, red text for off.
- Supports Enum cycle buttons or dropdown menus (the dropdown is a scrollable list with the current value `✓`-highlighted).
- Bottom-left is `Cancel`, bottom-right is `Done` (save and exit); `Done` is only enabled when the config has been modified.
- Cancelling or pressing ESC with unsaved changes shows a confirmation (Discard Changes / Keep Editing) to avoid losing edits.

## Load & Save

Usually you only need to call `load()` once after creating the `ConfigManager`:

```java
static {
    MANAGER.load();
}
```

The `Done` button in `ConfigScreen` will automatically call `MANAGER.save()`.

You can also call these manually:

```java
MANAGER.load();     // Read from disk
MANAGER.save();     // Write to disk
MANAGER.resetAll(); // Reset all configs to their default values
```

## Build & Package

### Build the Whole Project

```bash
./gradlew build
```

### Build Output Locations

- `common/build/libs/fakeneko_config-common-26.2-1.0.3.jar`
- `fabric/build/libs/fakeneko_config-fabric-26.2-1.0.3.jar`
- `neoforge/build/libs/fakeneko_config-neoforge-26.2-1.0.3.jar`

### Fabric Development

Entrypoint class:

```java
public class MyModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MyConfig.init();
    }
}
```

### NeoForge Development

Entrypoint class:

```java
@Mod("my_mod_id")
public class MyModNeoForge {
    public MyModNeoForge(IEventBus eventBus) {
        MyConfig.init();
    }
}
```

> Make sure the modId in `@Mod` matches the one in `ConfigManagerImpl` and conforms to NeoForge's `^[a-z][a-z0-9_]*$` pattern.

## Project Structure

```text
fakeneko_config/
├── common/        # Core API and common implementation
├── fabric/        # Fabric loader entrypoint
├── neoforge/      # NeoForge loader entrypoint
├── build-logic/   # Shared Gradle configuration
└── README.md      # Usage guide
```

## Supported Minecraft Versions

- 1.26.2 (Fabric + NeoForge)

## License

MIT License

Copyright (c) 2026 fakeneko

---

For questions, see the example tests in `common/src/test/java/cn/com/fakeneko/config/impl/ConfigSerializationTest.java`.

Author: `fakeneko` (configured via `mod_author` in `gradle.properties`).
