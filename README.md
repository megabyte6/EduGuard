# ClassManager

ClassManager is a PaperMC plugin for Minecraft Java Edition designed to manage class schedules and automate various in-game actions based on those schedules. It provides features such as automatic kicking of players, enabling/disabling the whitelist, and resetting the in-game day time based on predefined class end times.

## Features

- **Automatic Kicking**: Automatically kicks players at the end of class.
- **Whitelist Management**: Enables and disables the whitelist around class end times to prevent rejoins.
- **Day Time Reset**: Resets the in-game day time based on class schedules so each session has a similar experience.
- **Profanity Filter**: Built in profanity filtering for chat messages and usernames.
- **Configurable Settings**: All features are highly configurable through a YAML configuration file.

## Installation

1. [Download](https://github.com/megabyte6/ClassManager/releases/latest) the latest release of ClassManager.
2. Place the `ClassManager.jar` file in your Minecraft server's `plugins` directory.
3. Start your server to generate the default configuration files.
4. Configure the plugin by editing the `config.yml` file located in the `plugins/ClassManager` directory.

## Configuration

The main configuration file is `config.yml`. Here you can enable or disable features and set various parameters such as class end times, auto-kick settings, whitelist settings, and reset day settings.

### Example Configuration

```yaml
auto-kick:
  enabled: true
  message: Server is now closed. Time to exit the Dojo!
  before-end-of-class: 60 # seconds
  showWarning: true
  enable-whitelist-on-kick: true
  disable-whitelist-after: 360 # seconds (six minutes)

reset-day:
  enabled: true
  before-end-of-class: 600 # seconds (ten minutes)
  minecraft-time: 6000 # ticks (Minecraft world time)
  use-absolute-time: false
  minecraft-world-name: "world"

end-of-class-times:
  monday:
  - "15:00"
  - "17:00"
  tuesday:
  - "15:00"
  - "17:00"
  # Add more days and times as needed
profanity-fileter:
  filter-chat: true
  filter-usernames: true
```

## FAQ

### What about Minecraft version...?

You can find a release for the latest version of Minecraft under [Releases](https://github.com/megabyte6/ClassManager/releases). Support for older version can be made available upon request. If there is a version of Minecraft you wish to see supported, [submit an issue](https://github.com/megabyte6/ClassManager/issues/new)

## License

This project is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html)
