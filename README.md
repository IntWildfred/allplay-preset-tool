# AllPlay Preset Tool

My Panasonic SC-PMX150 lost the ability to save new internet radio presets when Qualcomm killed the AllPlay Radio app. The old presets still played, but there was no way to add new ones. Sending a stream over DLNA doesn't help either: the radio acts like it saved the preset, but that button plays nothing later.

So I made this. It plays a station on the speaker over AllPlay, the same way the old app did, and while it's playing you hold a preset button for 4 seconds. That's it, the preset is saved for good.

Tested on my SC-PMX150 with http and https streams. It should work on other AllPlay speakers (Panasonic ALL series, Hitachi, Monster), but I haven't tried them. If you do, let me know in an issue.

## How to use it

You need a Windows 10/11 PC on the same network as the speaker. Java is included, nothing to install.

1. Download the zip from [Releases](../../releases) and extract it. (The `.bat` files in this repo won't run on their own, use the zip.)
2. Make sure your network is set to **Private** in Windows.
3. Turn the speaker on in NETWORK mode and run `1-BUSCAR-RADIO.bat` to check it finds it. If Windows warns you, click **More info > Run anyway**. If the firewall asks, allow **Private networks**.
4. Run `3-PONER-RADIO-NUEVA.bat`, paste a stream address and a name.
5. When it starts playing, hold a free preset button for 4 seconds.

`2-VER-LO-QUE-SUENA.bat` shows what's currently playing, handy to see what an old preset has saved.

## What's in here

- `src/AllPlayTool.java` is the program. It finds the speaker and sends it the station the same way the old app did.
- `src/msajapi_stub.c` is a fake `MSAJApi.dll`. The AllJoyn library I use asks Windows for that file to load, but newer Windows versions don't have it anymore. The library doesn't actually need it (it has its own built-in AllJoyn), so this fake one just exists to let it start.
- `scripts/` has the `.bat` files and the instructions that go inside the zip.

The release zip also has a portable Java, the AllJoyn and tchaikovsky libraries and their licenses, so it runs without installing anything.

## Credits

Uses [tchaikovsky](https://github.com/dominicdesu/tchaikovsky) (Apache-2.0), AllJoyn (ISC) and Eclipse Temurin Java (GPLv2 + Classpath Exception). Their licenses are included in the release zip. Thanks to the openHAB folks who kept AllPlay alive.

Not affiliated with Panasonic or Qualcomm. MIT license, use at your own risk.
