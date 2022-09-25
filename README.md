# RaidBot
A Discord bot built for scheduling events primarily for Destiny 2.

## About RaidBot
### Planned Features
- All necessary configuration done through `config.json` file.
- Schedule events to play with fellow server members. Bot generates an embed based on user provided short name and local time. See `event.json` for full list of supported events in Destiny 2.
- When an event is scheduled the bot will take local time provided and convert to UTC time using user's assigned timezone role. UTC time displayed in an embed automatically displays in each users local time to make coordination easier.
- General Commands:
	- `!lfg`:  Schedule events with provided shortname and local time for user scheduling the event.
	- `!remind`: Ping users who have accepted or are tentative for the event depending on input given to command. Reduces need to manually ping everyone.
	- `!delete`: Deletes a scheduled event and removes it from the backup files.


### Technologies Used
- [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)
- [JDA-Utilities Fork, JDA-Chewtils](https://github.com/Chew/JDA-Chewtils)
- [gson](https://github.com/google/gson)
- [Logback](https://logback.qos.ch/)

## Roadmap
- Upgrade to JDA 5.0 (awaiting JDA 5.0 release)
- Add a Discord thread per event to enable easier coordination (needs JDA 5.0)
