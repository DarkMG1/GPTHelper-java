package dev.darkmg1.gpthelper.storage;

import lombok.Getter;

@Getter
public enum Model {
	GPT_4_8K("gpt-4", 0.03, 0.06),
	GPT_4_TURBO_NO_VISION("gpt-4-1106-preview", 0.01, 0.03),
	GPT_4_TURBO_VISION("gpt-4-1106-vision-preview", 0.01, 0.03),
	GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106", 0.001, 0.002),
	GPT_3_5_TURBO_4K("gpt-3.5-turbo", 0.003, 0.006),
	DALL_E_3("dall-e-3", 0.04, 0.08);

	private final String modelName;
	private final double inputCost;
	private final double outputCost;

	Model(String modelName, double inputCost, double outputCost) {
		this.modelName = modelName;
		this.inputCost = inputCost;
		this.outputCost = outputCost;
	}
}
