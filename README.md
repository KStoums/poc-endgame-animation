# POC - EndGame Animation Plugin

A simple proof of concept for a Minecraft plugin (Paper 1.21.4) that shows an endgame animation using NPCs and camera switching.

## Description

This plugin adds a basic endgame animation:
- Countdown before the animation starts.
- The player is switched to spectator mode and placed in front of a podium.
- NPCs representing the top 3 players appear one by one with lightning effects.
- The player's camera is switched to a podium-facing NPC using ProtocolLib.
- At the end, the player is teleported back to the lobby and returned to survival mode.

## Requirements

- Minecraft server running **Paper 1.21.4**
- **ProtocolLib** (e.g., version 5.3.0)
- **Citizens** plugin (for NPCs)

## Setup

1. Install Paper, ProtocolLib, and Citizens on your server.
2. Modify the coordinates of the location in front of the podium and the 3 podium locations in EndGameAnimation.java
3. Compile and place the plugin JAR in the `plugins/` folder.
4. Use the command: /endgame_animation

## Notes
- This is a proof of concept, not a production-ready plugin.
- NPC names and locations are hardcoded for testing purposes.
