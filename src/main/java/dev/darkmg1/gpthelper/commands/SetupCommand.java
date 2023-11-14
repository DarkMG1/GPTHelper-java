package dev.darkmg1.gpthelper.commands;

import dev.darkmg1.gpthelper.GPTHelper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class SetupCommand extends ListenerAdapter {


	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		if (e.getName().equals("setup")) {
			e.deferReply().queue();
			if (GPTHelper.getOpenAiService() == null) {
				e.getHook().sendMessageEmbeds(
						GPTHelper.getErrorEmbedBuilder("ChatGPT Unavailable", "openAiService variable is null")
								.build()
				).queue();
				return;
			}
			if (e.getMember() == null) {
				e.getHook().sendMessageEmbeds(
						GPTHelper.getErrorEmbedBuilder("Member null", "Message sender as member is null")
								.build()
				).queue();
				return;
			}
			if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				e.getHook().sendMessageEmbeds(GPTHelper.getNoPermissionEmbedBuilder().build()).queue();
				return;
			}
			Category category = GPTHelper.getServer().getCategoryById(GPTHelper.getStorageManager().getConfiguration().gptCategoryId());
			if (category == null) {
				e.getHook().sendMessageEmbeds(
								GPTHelper.getErrorEmbedBuilder("Channel Category", "Category doesn't exist / variable is null")
										.build())
						.queue();
				return;
			}
			User user = Objects.requireNonNull(e.getOption("user")).getAsUser();
			final AtomicReference<String> channelId = new AtomicReference<>();
			category.createTextChannel("GPT Chat - " + user.getName())
					.setTopic("This channel is for " + user.getName() + " to ask questions to the bot.")
					.setParent(category)
					.setSlowmode(5)
					.addPermissionOverride(e.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
					.addMemberPermissionOverride(user.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
					.queue(newChannel -> channelId.set(newChannel.getId()));
			while (channelId.get() == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}
			}
			TextChannel textChannel = GPTHelper.getServer().getTextChannelById(channelId.get());
			if (textChannel == null) {
				e.getHook().sendMessageEmbeds(
						GPTHelper.getErrorEmbedBuilder("Variable is null", "Text Channel is null.")
								.build()
				).queue();
				return;
			}
			e.getHook().sendMessage("Created channel for " + user.getName() + " at " + textChannel.getAsMention()).queue();
			GPTHelper.getStorageManager().addUser(user, textChannel);
			textChannel.sendMessageEmbeds(
					GPTHelper.getBaseEmbedBuilder()
							.setTitle("Welcome")
							.setDescription("Welcome to GPTHelper. To use the bot, simply use the /question command with the question you want to ask!\n You can specify models as well as tokens to tune your response to your liking. If you want to know what models are available, use the /models command.\n If you want to know how much you owe, use the /billing command.")
							.setColor(Color.BLACK)
							.build()
			).queue(message-> message.pin().queue(), failure -> e.getHook().sendMessageEmbeds(
					GPTHelper.getErrorEmbedBuilder("Error", "Error pinning message.")
							.build()
			).queue());
		}
	}
}
