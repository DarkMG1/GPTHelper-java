package dev.darkmg1.gpthelper.commands;

import dev.darkmg1.gpthelper.GPTHelper;
import dev.darkmg1.gpthelper.storage.GPTRequest;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BillingCommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		if (e.getName().equals("billing")) {
			e.deferReply(true).queue();
			CompletableFuture.runAsync(() -> {
				long id = e.getUser().getIdLong();
				User user;
				if (e.getMember() == null) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("Member null", "Message sender as member is null")
									.build()
					).queue();
					return;
				}
				if (e.getMember().hasPermission(Permission.ADMINISTRATOR) && e.getOption("user") != null) {
					user = e.getOption("user").getAsUser();
					id = user.getIdLong();
				} else {
					user = e.getUser();
				}
				if (!GPTHelper.getStorageManager().getRequests().containsKey(id)) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getBaseEmbedBuilder()
									.setTitle("No Information")
									.setDescription("The user specified has not made any requests to the bot.")
									.setColor(Color.RED)
									.build()
					).queue();
					return;
				}

				List<GPTRequest> gptRequests = GPTHelper.getStorageManager().getRequests().get(e.getUser().getIdLong());
				double inputCost = 0, outputCost = 0, totalCost = 0;
				for (GPTRequest gptRequest : gptRequests) {
					inputCost = inputCost + ((gptRequest.inputTokens() / 1000.0) * gptRequest.model().getInputCost());
					outputCost = outputCost + ((gptRequest.outputTokens() / 1000.0) * gptRequest.model().getOutputCost());
				}
				BigDecimal inputRounded = new BigDecimal(inputCost).setScale(5, RoundingMode.HALF_UP);
				BigDecimal outputRounded = new BigDecimal(outputCost).setScale(5, RoundingMode.HALF_UP);

				totalCost = inputRounded.doubleValue() + outputRounded.doubleValue();
				e.getHook().sendMessageEmbeds(
						GPTHelper.getBaseEmbedBuilder()
								.setTitle("Billing Information")
								.setDescription("Below is the breakdown for the user's usage of the bot")
								.addField("Username", user.getName(), true)
								.addField("GPT Requests", String.valueOf(gptRequests.size()), true)
								.addField("Input Cost", inputRounded.toPlainString(), true)
								.addField("Output Cost", inputRounded.toPlainString(), true)
								.addField("Total Cost", String.valueOf(totalCost), true)
								.build()
				).queue();
			});
		}
	}
}
