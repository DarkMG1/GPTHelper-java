package dev.darkmg1.gpthelper.storage;

public record Configuration(String discordToken, String openAiToken, long guildId, long gptCategoryId, int timeout) {

}
