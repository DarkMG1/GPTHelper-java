package dev.darkmg1.gpthelper.commands;

import dev.darkmg1.gpthelper.GPTHelper;
import dev.darkmg1.gpthelper.storage.Model;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModelsCommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
		if (e.getName().equals("models")) {
			CompletableFuture.runAsync(() -> {
				EmbedBuilder modelEmbed = GPTHelper.getBaseEmbedBuilder()
						.setTitle("Models Supported")
						.setDescription("Below are the models this bot currently supports. When using the bot, and wanting to specify a specific model, use the exact model name used below!");
				for (Model model : Model.values()) {
					modelEmbed.addField(model.getModelName(), "Input: $" + model.getInputCost() + "\nOutput: $" + model.getOutputCost(), true);
				}
				e.replyEmbeds(modelEmbed.build()).setEphemeral(true).queue();
			});
		}
	}
}
