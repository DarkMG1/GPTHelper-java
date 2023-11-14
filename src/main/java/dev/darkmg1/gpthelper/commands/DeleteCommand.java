package dev.darkmg1.gpthelper.commands;

import dev.darkmg1.gpthelper.GPTHelper;
import dev.darkmg1.gpthelper.storage.GPTUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Duration;
import java.util.Optional;

public class DeleteCommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		if (e.getName().equals("delete")) {
			e.deferReply(true).queue();
			if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				e.getHook().sendMessageEmbeds(GPTHelper.getNoPermissionEmbedBuilder().build()).delay(Duration.ofSeconds(5)).queue();
				return;
			}
			User user = e.getOption("user").getAsUser();
			Optional<GPTUser> optionalGPTUser = GPTHelper.getStorageManager().getGptUsers().stream().filter(gptUser -> gptUser.userId() == user.getIdLong()).findAny();
			if (optionalGPTUser.isEmpty()) {
				e.getHook().sendMessageEmbeds(GPTHelper.getErrorEmbedBuilder("User not found", "The user specified was not found. Please try again.").build()).queue();
				return;
			}
			GPTUser gptUser = optionalGPTUser.get();
			GPTHelper.getStorageManager().getGptUsers().remove(gptUser);
			GPTHelper.getStorageManager().saveUsersFile();
			GPTHelper.getStorageManager().getRequests().remove(user.getIdLong());
			GPTHelper.getStorageManager().saveRequestsFile();
			TextChannel textChannel = e.getJDA().getTextChannelById(gptUser.channelId());
			if (textChannel != null) {
				textChannel.delete().queue();
			}
			e.getHook().sendMessageEmbeds(
					GPTHelper.getBaseEmbedBuilder()
							.setTitle("Successfully Deleted User")
							.setDescription("User specified has been deleted from requests map and gpt users map.")
							.setColor(Color.GREEN)
							.build()
			).queue();
		}
	}
}
