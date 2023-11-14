package dev.darkmg1.gpthelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theokanning.openai.service.OpenAiService;
import dev.darkmg1.gpthelper.commands.*;
import dev.darkmg1.gpthelper.storage.StorageManager;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class GPTHelper {

	@Getter
	private static OpenAiService openAiService;
	@Getter
	private static JDA bot;
	@Getter
	private static Guild server;
	@Getter
	private static StorageManager storageManager;
	private static Gson prettyGson;

	public static void main(String... args) {
		storageManager = new StorageManager();
		if (!storageManager.isReady) {
			System.out.println("Error occurred while loading one of the data files. Shutting down bot.");
		} else {
			if (storageManager.getConfiguration().discordToken().equals("Placeholder")) {
				System.out.println("Please enter your Discord bot token in the config.json file.");
				return;
			}
			if (storageManager.getConfiguration().guildId() == 1L) {
				System.out.println("Please enter your Discord guild ID in the config.json file.");
				return;
			}
			if (storageManager.getConfiguration().openAiToken().equals("Placeholder")) {
				System.out.println("Please enter your OpenAI token in the config.json file.");
				return;
			}
			if (storageManager.getConfiguration().gptCategoryId() == 1L) {
				System.out.println("Please enter your GPT category ID in the config.json file.");
				return;
			}
			JDABuilder builder = JDABuilder.createDefault(storageManager.getConfiguration().discordToken())
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.enableIntents(GatewayIntent.GUILD_MEMBERS,
							GatewayIntent.MESSAGE_CONTENT,
							GatewayIntent.GUILD_PRESENCES,
							GatewayIntent.GUILD_MESSAGES,
							GatewayIntent.GUILD_MESSAGE_REACTIONS);
			bot = builder.addEventListeners(
							new QuestionCommand(),
							new ModelsCommand(),
							new SetupCommand(),
							new BillingCommand(),
							new DallECommand(),
							new DeleteCommand())
					.build();


			System.out.println("Successfully loaded data file.");
			TimerTask setupCommands = new TimerTask() {
				@Override
				public void run() {
					server = bot.getGuildById(storageManager.getConfiguration().guildId());
					if (server == null) {
						System.out.println("Error occurred while loading server. Shutting down bot.");
						bot.shutdownNow();
						return;
					}
					server.upsertCommand(
							Commands.slash("question", "Pass a question to GPT")
									.addOption(OptionType.STRING, "question", "The question you want to ask", true)
									.addOption(OptionType.INTEGER, "tokens", "The number of tokens to use", false)
									.addOption(OptionType.STRING, "model", "The model to use", false)
					).queue();
					server.upsertCommand(Commands.slash("models", "Show all the models available")).queue();
					server.upsertCommand(
							Commands.slash("setup", "Setup a user with a channel to use the bot")
									.addOption(OptionType.USER, "user", "The user to setup", true)
									.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
					).queue();
					server.upsertCommand(
							Commands.slash("delete", "Deletes a user's data and channel from the bot")
									.addOption(OptionType.USER, "user", "The user to delete", true)
									.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
					).queue();
					server.upsertCommand(
							Commands.slash("billing", "Show how much you or a specified user owes for using the bot")
									.addOption(OptionType.USER, "user", "The user to check", false)
					).queue();
					server.upsertCommand(
							Commands.slash("dalle", "Create an image based on a prompt")
									.addOption(OptionType.STRING, "prompt", "The prompt to use", true)
									.addOption(OptionType.STRING, "quality", "The quality of the image (standard/hd)", false)
									.addOption(OptionType.STRING, "size", "The size of the image (1024x1024/1792x1024/1024x1792)", false)
									.addOption(OptionType.STRING, "style", "The style of the image (vivid/natural)", false)
					).queue();
				}
			};

			Timer timer = new Timer();
			timer.schedule(setupCommands, 5000);

			openAiService = new OpenAiService(storageManager.getConfiguration().openAiToken(), Duration.ofSeconds(90));
		}
	}

	public static Gson getPrettyGson() {
		if (prettyGson == null)
			prettyGson = new GsonBuilder()
					.setPrettyPrinting()
					.disableHtmlEscaping()
					.create();
		return prettyGson;
	}

	public static EmbedBuilder getBaseEmbedBuilder() {
		return new EmbedBuilder()
				.setAuthor("GPTHelper")
				.setTimestamp(Instant.now())
				.setUrl("https://github.com/DarkMG1/GPTHelper");
	}

	public static EmbedBuilder getErrorEmbedBuilder(String fieldName, String fieldDescription) {
		return getBaseEmbedBuilder()
				.setTitle("Unavailable")
				.setDescription("Error has occurred. Please see reason below.")
				.addField(fieldName, fieldDescription, false)
				.setColor(Color.RED);
	}

	public static EmbedBuilder getNoPermissionEmbedBuilder() {
		return getBaseEmbedBuilder()
				.setTitle("No Permission")
				.setColor(Color.RED)
				.setDescription("You do not have the permission to execute this command!");
	}
}