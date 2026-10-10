NEW PRESETS FOR THE PANASONIC SC-PMX150 RADIO

What it's for
This program makes the radio play any internet station, just like the old AllPlay Radio app did. When the station is playing, you save it to one of the radio's PRESET buttons and it stays saved for good. Nothing to install and nothing to pay.

First time
1. Right-click the .zip file and choose Extract all. For example, to C:\AllPlayTool.
2. The computer and the radio must be on the same network, and that network must be set as a Private network. Change it in Settings, Network & internet, your network, Network profile type: Private.
3. Turn on the radio in NETWORK mode.
4. Open 1-BUSCAR-RADIO.bat. A line starting with Speaker and your radio's name should appear.
5. If Windows shows "Windows protected your PC", click More info and then Run anyway.
6. If Windows Firewall asks about "OpenJDK Platform binary", tick Private networks and click Allow access. Then close the window and open 1-BUSCAR-RADIO.bat again.

How to add a station
1. Open 3-PONER-RADIO-NUEVA.bat.
2. Paste the station's address. It must start with http:// or https://. Both work.
3. Type the station's name. The description and logo are optional: if you don't have them, press Enter.
4. When the radio starts playing, hold down a free PRESET button for 4 seconds.
5. To check, switch to another source on the radio and come back to that PRESET. The new station should play.

See what's playing
2-VER-LO-QUE-SUENA.bat shows the name and address of whatever the radio is playing. For example, it tells you which station a PRESET has saved.

If it can't find the radio
Check that the radio is on in NETWORK mode, that the network is set as Private, and that the firewall allows "OpenJDK Platform binary" on Private networks. If you closed the firewall prompt without allowing it, go to Control Panel, Windows Defender Firewall, Allow an app, Change settings, and tick "OpenJDK Platform binary" in the Private column.
