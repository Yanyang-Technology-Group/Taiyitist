<div align="center">
<img src="src/main/resources/assets/taiyitist/logo.png" alt="logo">
  <h1>Taiyitist 1.20.1</h1>

### The Bukkit/Spigot/Paper API implementation for Fabric
### The project is no longer a Fabric mod, but a standalone program
[![](https://img.shields.io/github/stars/TaiyitstMC/Taiyitst.svg?label=Stars&logo=github)](https://github.com/TaiyitstMC/Taiyitst/stargazers)
[![](https://img.shields.io/badge/JDK-21.0.3-brightgreen.svg?colorB=469C00&logo=java)](https://www.azul.com/downloads/?version=java-21-lts#zulu)
[![](https://img.shields.io/badge/Gradle-8.13-brightgreen.svg?colorB=469C00&logo=gradle)](https://docs.gradle.org/8.13/release-notes.html)
[![](https://img.shields.io/discord/311256119005937665.svg?color=%237289da&label=Discord&logo=discord&logoColor=%237289da)](https://discord.gg/mohistmc)

</div>

| Version | Support     | Stability | Mod compatibility | Plugin compatibility |
|---------|-------------|-----------|-------------------|----------------------| 
| 1.21.7  | Active      | Poor      | Poor              | Poor                 |
| 1.21.1  | Active      | Poor      | Poor              | Poor                 |
| 1.20.1  | Active      | Good      | Good              | Good                 |

## Notice
- Fabric + Bukkit is more vanilla-like than Forge + Bukkit
- Fabric API uses mixins to change minecraft indirectly
- Taiyitst also use mixins to hook Bukkit api as a fabric mod
- There's a little breaking changes
- This version of Taiyitst 1.20.1, supports MC version 1.20.1

## Tips
- If you want to try a different Fabric + Bukkit hybrid server, you can try CardBoard
- It is implements bukkit api by itself,and the author is a pioneer to try a new way to implements Fabric + Bukkit
- Taiyitst is different with Cardboard,you can also try Cardboard as an alternative choice if you want

### Running?
It could be compilable and running, but the compatibility with plugins is poor.

### NMS Support
We do support using Spigot's net.minecraft.server classes. Classes and Fields will automatically remap to their intermediary counterparts in runtime, but it will not change plugins at all,
so don't worried about the plugin files will be changed to unsafe

## Usage
- Download Taiyitst.
- Launch with command java -jar taiyitst-launcher-version.jar

### Interactive console

The console uses a shared JLine 3 terminal, following Leaf/Paper's terminal appender approach.
Logs appear above the command being edited. Warnings/errors and Minecraft plugin colours
(including RGB) are rendered in the terminal; log files and redirected output remain plain text.

| Input | Action |
| --- | --- |
| Tab / repeated Tab | Complete commands and arguments / select among candidates; works on an empty line |
| Up / Down | Recall command history, saved in `.console_history` |
| Ctrl+R | Search history |
| Home / End, Ctrl+A / Ctrl+E | Move to the beginning / end of the line |
| Ctrl+U / Ctrl+K | Clear text before / after the cursor |
| Ctrl+L | Redraw the terminal |
| Ctrl+C | Shut down the server gracefully |
| Ctrl+D | End console input; keep the server running |

Commands may start with `/`. Completion respects the cursor position, preserves Minecraft
quoting/backslashes, and stops waiting after one second if the server is busy. Parsed command
arguments are coloured; unparsed input is red. Mods with unusual parsers can disable this with
`-Dtaiyitist.console.highlighting=false`.

JVM options go **before** `-jar`: `-Dterminal.ansi=false` disables colours,
`-Dterminal.ansi=true` enables colours in a compatible hosting panel, and
`-Dterminal.jline=false` disables line editing. Server options go **after** the JAR:
`--nojline` selects plain input and `--noconsole` disables input while retaining logs.
Without a usable terminal the server automatically uses plain input/output.

Run the console regression tests with Java 21:
`JAVA_HOME=/path/to/jdk-21 ./gradlew :taiyitist-server:test`.

## Discord
- https://discord.gg/stTgbjkJ

## Developer Support
- Download the dev lib jar from GitHub actions.
- Use Fabric Official Template [**Fabric Example Mod**](https://github.com/FabricMC/fabric-example-mod.git).
- Using Mojang Official Mappings in your build.gradle
- Create a directory called lib in your root dir.
- Add dependencies of Taiyitst, such as compileOnly(fileTree("lib/taiyitst-version-dev.jar"))

## Upstream Projects
- [**Bukkit**](https://hub.spigotmc.org/stash/scm/spigot/bukkit.git) - Plugin support.
- [**CraftBukkit**](https://hub.spigotmc.org/stash/scm/spigot/craftbukkit.git) - Plugin support.
- [**Spigot**](https://hub.spigotmc.org/stash/scm/spigot/spigot.git) - Plugin support.
- [**Paper**](https://github.com/PaperMC/Paper.git) - Plugin support.
- [**Arclight**](https://github.com/IzzelAliz/Arclight.git) - Some code.
- [**Mohist**](https://github.com/MohistMC/Mohist.git) - Some code.
- [**StackDeobfuscator**](https://github.com/booky10/StackDeobfuscator) - auto deobfuscate logger crash

## Special Thanks To:
![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](http://www.yourkit.com/), makers of the outstanding java profiler, support open source projects of all kinds with their full-featured [Java](https://www.yourkit.com/java/profiler/index.jsp) and [.NET](https://www.yourkit.com/.net/profiler/index.jsp) application profilers. We thank them for granting Mohist an OSS license so that we can make our software the best it can be.

[<img src="https://user-images.githubusercontent.com/21148213/121807008-8ffc6700-cc52-11eb-96a7-2f6f260f8fda.png" alt="" width="100">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/), creators of the IntelliJ IDEA, supports Paper with one of their [Open Source Licenses](https://www.jetbrains.com/opensource/). IntelliJ IDEA is the recommended IDE for working with Paper, and most of the Paper team uses it.
