package dev.darkmg1.gpthelper.commands;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.image.ImageResult;
import dev.darkmg1.gpthelper.GPTHelper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.CompletableFuture;

public class DallECommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		if (e.getName().equals("dalle")) {
			e.deferReply().queue();
			CompletableFuture.runAsync(() -> {
				String sizeString = e.getOption("size") != null && e.getOption("size").getAsString() != null
						? e.getOption("size").getAsString()
						: "1024x1024";
				String qualityString = e.getOption("quality") != null && e.getOption("quality").getAsString() != null
						? e.getOption("quality").getAsString()
						: "standard";
				String styleString = e.getOption("style") != null && e.getOption("style").getAsString() != null
						? e.getOption("style").getAsString()
						: "vivid";
				CreateImageRequest createImageRequest = CreateImageRequest.builder()
						.prompt(e.getOption("prompt").getAsString())
						.size(sizeString)
						.quality(qualityString)
						.style(styleString)
						.responseFormat("url")
						.build();
				ImageResult result;
				try {
					result = GPTHelper.getOpenAiService().createImage(createImageRequest);
				} catch (OpenAiHttpException exception) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("OpenAI Error", exception.getMessage())
									.build()
					).queue();
					return;
				}
				for (Image image : result.getData()) {
					e.getHook().sendMessage(image.getUrl()).queue();
				}
			});
		}
	}
}
