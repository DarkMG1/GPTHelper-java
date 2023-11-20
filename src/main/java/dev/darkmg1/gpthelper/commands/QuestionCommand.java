package dev.darkmg1.gpthelper.commands;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.utils.TikTokensUtil;
import dev.darkmg1.gpthelper.GPTHelper;
import dev.darkmg1.gpthelper.storage.GPTRequest;
import dev.darkmg1.gpthelper.storage.Model;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QuestionCommand extends ListenerAdapter {

	public static String[] splitStringEvery(String s, int interval) {
		int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
		String[] result = new String[arrayLength];

		int j = 0;
		int lastIndex = result.length - 1;
		for (int i = 0; i < lastIndex; i++) {
			result[i] = s.substring(j, j + interval);
			j += interval;
		} // Add the last bit
		result[lastIndex] = s.substring(j);

		return result;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
		if (e.getName().equals("question")) {
			e.deferReply().queue();
			CompletableFuture.runAsync(() -> {
				if (GPTHelper.getStorageManager().getGptUsers().stream().noneMatch(gptUser -> gptUser.userId() == e.getUser().getIdLong())) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getNoPermissionEmbedBuilder().build()
					).queue();
					return;
				}
				if (GPTHelper.getOpenAiService() == null) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("ChatGPT Unavailable", "openAiService variable is null")
									.build()
					).queue();
					return;
				}
				String question = e.getOption("question").getAsString();
				String modelString = e.getOption("model") != null && e.getOption("model").getAsString() != null
						? e.getOption("model").getAsString()
						: "gpt-4-1106-preview";
				if (Arrays.stream(Model.values()).noneMatch(modelEnum -> modelEnum.getModelName().equals(modelString))) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("Model not found", "The model you specified was not found. Please try again.")
									.build()
					).setEphemeral(true).queue();
					return;
				}
				Model model = Arrays.stream(Model.values()).filter(modelEnum -> modelEnum.getModelName().equals(modelString)).findFirst().get();
				if (model.getModelName().equals("dall-e-3")) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getBaseEmbedBuilder()
									.setTitle("Dall-E-3")
									.setDescription("Use the /dalle command to use this model.")
									.setColor(Color.BLACK)
									.build()
					).setEphemeral(true).queue();
					return;
				}
				String maxTokens = e.getOption("max-tokens") != null && e.getOption("max-tokens").getAsString() != null
						? e.getOption("max-tokens").getAsString()
						: "NONE";
				int tokens;
				try {
					if (maxTokens.equals("NONE")) {
						tokens = 0;
					} else {
						tokens = Integer.parseInt(maxTokens);
					}
				} catch (NumberFormatException numberFormatException) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("Invalid Number", "The number you specified was invalid. Please try again.")
									.build()
					).setEphemeral(true).queue();
					return;
				}
				final List<ChatMessage> messages = new ArrayList<>();
				final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), question);
				messages.add(systemMessage);
				ChatCompletionRequest chatCompletionRequest;
				if (tokens == 0) {
					chatCompletionRequest = ChatCompletionRequest.builder()
							.model(model.getModelName())
							.messages(messages)
							.build();
				} else {
					chatCompletionRequest = ChatCompletionRequest.builder()
							.model(model.getModelName())
							.messages(messages)
							.maxTokens(tokens)
							.build();
				}
				ChatMessage responseMessage;
				try {
					responseMessage = GPTHelper.getOpenAiService().createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
				} catch (OpenAiHttpException exception) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("OpenAI Error", exception.getMessage())
									.build()
					).queue();
					return;
				}
				int inputTokens = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_4_1106_preview.getName(), question);
				int outputTokens = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_4_1106_preview.getName(), responseMessage.getContent());
				GPTHelper.getStorageManager().addRequest(e.getUser(), new GPTRequest(model, inputTokens, outputTokens));
				if (responseMessage.getContent().length() > 2000) {
					e.getHook().sendMessage("Question: \n" + question).queue();
					String[] split = splitStringEvery(responseMessage.getContent(), 2000);
					for (String s : split) {
						e.getHook().sendMessage(s).queue();
					}
				} else {
					e.getHook().sendMessage("**Question**: \n" + question + "\n\n**GPT Response**: \n" + responseMessage.getContent()).queue();
				}
			}).orTimeout(GPTHelper.getStorageManager().getConfiguration().timeout(), TimeUnit.SECONDS).exceptionally(throwable -> {
				if (throwable instanceof TimeoutException) {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("Timeout", "The request timed out. Please try again.")
									.build()
					).queue();
				} else {
					e.getHook().sendMessageEmbeds(
							GPTHelper.getErrorEmbedBuilder("Error", "An error occurred: " + throwable.getMessage() + ". Please try again.")
									.build()
					).queue();
					throwable.printStackTrace();
				}
				return null;
			});
		}
	}
}
