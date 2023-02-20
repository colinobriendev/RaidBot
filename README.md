# RaidBot
A Discord bot built for scheduling events primarily for Destiny 2.

## About RaidBot
### Features
- All necessary configuration done through `config.json` file.
- Schedule events to play with fellow server members. Bot generates an embed message based on user provided short name and local time. See `event.json` for full list of supported events in Destiny 2.
- When an event is scheduled the bot will take local time provided and convert to UTC time using user's assigned timezone role. UTC time displayed in an embed automatically displays in each users local time to make coordination easier.
- Users can react with the emojis attached to the embed to indicate their interest in the event. The embed will regenerate on each reaction to keep a list of attendees.
	- ✅ - Plans to attend
	- ❓ - Is tentative and may join
	- ❌ - Is unable to join
- The bot will generate a new chat thread on the embed message and add the user who generated it. When users react with ✅ and ❓ they are automatically added to the chat thread.
### Commands:
- `!lfg`:  Schedule events with provided shortname and local time for user scheduling the event.
- `!remind`: Ping users who have accepted or are tentative for the event depending on input given to command. Reduces need to manually ping everyone.
- `!delete`: Deletes a scheduled event, its related chat thread, and removes it from the backup files.


### Technologies Used
- [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)
- [JDA-Utilities Fork, JDA-Chewtils](https://github.com/Chew/JDA-Chewtils)
- [gson](https://github.com/google/gson)
- [Logback](https://logback.qos.ch/)

## Roadmap
- Currently under discussion
