# GPTHelper

This is just a small project I made in my free time to enable people to access GPT-4 for a fraction of the monthly cost OpenAI charges.

# Note
This project has been abandoned due to the library I was using has not been updated. Instead, I am working on converting this to Python, with more features, due to OpenAI supporting Python.

## Summary

1. [How it works](#how-it-works)
2. [Installation](#installation)
3. [Usage](#usage)


## How it works
This bot uses the [OpenAI API](https://beta.openai.com/docs/introduction) to generate text. It uses the [DiscordJDA](https://github.com/discord-jda/JDA) to interact with Discord. The bot is written in Java.

## Installation
Currently, I haven't made a release so if you are interested in using it, you will have to build it yourself. Here are the steps necessary for doing this.
1. Clone the repository `git clone https://github.com/DarkMG1/GPTHelper`
2. Download Java 17 from Oracle (or any other source)
3. Go to the root directory of the project and run `./gradlew shadowJar build`
4. The jar file will be located in `target/`
5. Go to the [Discord Developer Portal](https://discord.com/developers/applications) and create a new application.
6. Create a bot for the application and enable all the Privileged Intents.
7. Go to the OAuth2 -> URL Generator tab and select the `bot` and `applications.commands` scopes. Then select the `Administrator` permission.
8. Copy the URL given and invite the bot to your server.
9. Go to the Bot tab and copy the token. Keep it aside for now.
10. Go to the [OpenAI API](https://beta.openai.com/docs/introduction) and create an account.
11. Go to the [API Keys](https://platform.openai.com/api-keys) tab and add a API Key. Copy the secret key and keep it aside for now.
12. Ensure your account has money loaded into it. You can do this by going to the [Billing](https://platform.openai.com/account/billing/overview) tab.
13. Go back to the directory where the jar file is located and run the jar file using `java -jar GPTHelper-0.0.1-BETA.jar`. This will create a `config.json` file.
14. Open the `config.json` file and fill in the `discordToken` and `openAiToken` fields with the bot token and open ai secret key you copied earlier.
15. Ensure you have "developer mode" turned on in Discord. You can do this by going to Settings -> Advanced -> Developer Mode.
16. Right-click on the server you want to use the bot in and click on "Copy ID". Paste this ID into the `guildId` field in the `config.json` file.
17. Create a category where you want the gpt-channels to be stored. Copy the ID of this category and paste it into the `gptCategoryId` field in the `config.json` file.
18. Run the jar file once again using `java -jar GPTHelper-0.0.1-BETA.jar`. This will run the bot and create the necessary slash commands.

## Usage
The bot has 3 slash commands:
1. /question - This command will ask you for a question and then generate an answer for it. You can specify optional fields such as model and max tokens.
2. /setup - This command will create a channel in the category you specified in the `config.json` file. This channel will be used to answer gpt questions.
3. /models - This command will list all the models available for use. You can specify the model you want to use in the /question command.
4. /dalle - This command requires a prompt and will generate a image based on the prompt. You can specify optional field such as quality, size, and style.
5. /billing - This command will show a user their current balance and the amount of money they have spent on the bot.
6. /delete - This command will delete a user's data from the bot. This includes their balance, spent, and gpt channels. This command is irreversible.

Note: /setup and /delete can only be used by users with the administrator permission. In addition, users with the administrator permission can specify users with the /billing command to see how much other users owe. /question and /dalle cannot be used in channels other than the gpt channels created by the bot. /billing and /models can be used anywhere.

## Contributing
If you want to contribute to this project, feel free to open a pull request. I will review it as soon as possible. If you have any questions, feel free to open an issue or contact me on Discord at darkmg1.
I work on this project whenever I feel like it, so don't expect a quick response. I'm currently debating if I should make a bot open for public use, so if you are interested in using it, let me know.


