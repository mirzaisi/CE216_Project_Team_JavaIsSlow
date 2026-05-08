# Building the Windows Installer

This document explains how to build a real Windows installer (`.msi`) for PlayForge Manager. The build itself has to run on a Windows machine because `jpackage` produces a native installer that is platform-specific.

If you only want to run the app on Windows without packaging it, you can also do that with `mvn javafx:run` from a checked-out copy of the repo. The installer is only needed for distributing the app to someone who does not have Java set up.

## What you need on the Windows machine

You need three things installed and on the system PATH.

The first one is the JDK. JDK 17 or any newer LTS works. The standard OpenJDK from Adoptium (`https://adoptium.net`) is fine. Check it with `java -version` in a Command Prompt window. You also need `jpackage` to be available, which ships with the JDK starting from JDK 14, so any modern JDK is fine.

The second one is Apache Maven. Download from `https://maven.apache.org/`, unzip, and add the `bin` folder to PATH. Check it with `mvn -version`.

The third one is WiX Toolset, version 3.x. The `jpackage` tool uses WiX to actually produce the `.msi` file. Download WiX 3.14 from `https://wixtoolset.org/releases/`, install it, and make sure the WiX `bin` folder is on PATH. Check it with `candle -?` in a Command Prompt. WiX 4.x will not work, you specifically need 3.x.

## Building the installer

Open a Command Prompt window in the project folder and run:

```
mvn clean -P installer package
```

The `-P installer` part activates the installer profile, which is the part that runs `jpackage`. Maven will compile the code, run the tests, copy the runtime dependencies into a staging folder, and then run `jpackage` to produce the installer.

When the build finishes, the installer file is in `target\installer\`. The file name will be something like `PlayForgeManager-1.0.0.msi`. Double-click it to install the app.

## What gets installed

The installer puts the app under the user's local AppData by default. You get a Start Menu entry called PlayForgeManager and an optional desktop shortcut. The installer asks where to install, so you can pick a different folder if you want. The app has its own bundled JRE, so the user does not need to have Java installed to run the installed app.

## Uninstalling

The app shows up in the Windows "Apps and features" list under the name PlayForgeManager. Uninstall it from there or from the Start Menu entry.

## Troubleshooting

If you see an error like `WiX Tools not found` or `candle.exe is not on the PATH`, install WiX Toolset 3.x and add its `bin` folder to your PATH, then open a fresh Command Prompt and try again.

If you see `Error: Invalid or unsupported type: [msi]`, your JDK does not have `jpackage` available. Use a different JDK distribution that includes it.

If the build complains about JavaFX runtime errors after install, make sure you ran `mvn clean -P installer package` and not just `mvn package`, so the dependency staging picks up the Windows-flavoured JavaFX JARs.

## Building for testing without an installer

If you just want to make sure the project runs on the Windows machine before packaging, run:

```
mvn javafx:run
```

This launches the app directly without producing an installer. It needs Java and Maven, but not WiX.
