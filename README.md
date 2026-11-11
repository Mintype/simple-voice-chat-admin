# Simple Voice Chat Admin

A [Fabric](https://fabricmc.net/) mod for Minecraft 26.2 that adds server admin tools for [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat), such as globally muting players and gathering everyone into a single "Global Meeting" group.

## Commands

| Command | Description |
|---------|-------------|
| `/vcadmin muteall` | Mutes all non-OP players, saves their current groups, and moves everyone into the persistent "Global Meeting" group. |
| `/vcadmin unmuteall` | Unmutes all players and restores them to their previous groups. |

Only operators (or the server console) can run these commands. While global mute is active, players cannot leave the Global Meeting group and non-OP microphone audio is dropped.

## Requirements

- Minecraft 26.2
- Fabric Loader >= 0.20.1
- Fabric API
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) (API >= 2.6.0)
- Java 25

## Building

```bash
./gradlew build
```

The built jar can be found in `build/libs/`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
