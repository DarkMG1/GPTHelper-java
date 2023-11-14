package dev.darkmg1.gpthelper.storage;

import com.google.gson.reflect.TypeToken;
import dev.darkmg1.gpthelper.GPTHelper;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

	public boolean isReady = false;
	@Getter
	private ConcurrentHashMap<Long, List<GPTRequest>> requests;
	@Getter
	private List<GPTUser> gptUsers;
	@Getter
	private Configuration configuration;
	private File requestsFile = null;
	private File usersFile = null;
	private File configurationFile = null;

	public StorageManager() {
		String path;
		try {
			path = URLDecoder.decode(GPTHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			System.out.println("Error occurred while getting path.");
			e.printStackTrace();
			return;
		}
		File tempFile = new File(path);
		path = tempFile.getParentFile().getPath();

		if (!loadConfiguration(path)) {
			isReady = false;
			return;
		}

		if (!loadRequestsMap(path)) {
			isReady = false;
			return;
		}

		if (!loadUserList(path)) {
			isReady = false;
			return;
		}
		isReady = true;
	}

	private boolean loadConfiguration(String path) {
		configurationFile = new File(path, "config.json");

		if (!configurationFile.getParentFile().exists() && !configurationFile.getParentFile().mkdirs()) {
			System.out.println(("Failed to create directory " + configurationFile.getParentFile()));
		}

		System.out.println("Attempting to create file at: " + configurationFile.getPath());
		boolean fileCreated;
		try {
			fileCreated = configurationFile.createNewFile();
		} catch (IOException e) {
			System.out.println("Error occurred while creating file.");
			e.printStackTrace();
			return false;
		}

		if (fileCreated) {
			System.out.println("Successfully created data file...");
		} else {
			System.out.println("Loading previously saved data...");
		}

		Configuration loadedConfiguration;
		try (FileReader reader = new FileReader(configurationFile)) {
			loadedConfiguration = GPTHelper.getPrettyGson().fromJson(reader, (new TypeToken<Configuration>() {
			}).getType());
		} catch (Exception e) {
			System.out.println("Error occurred while reading " + configurationFile.getName() + ".");
			e.printStackTrace();
			return false;
		}

		if (loadedConfiguration != null) {
			configuration = loadedConfiguration;
		} else {
			configuration = new Configuration("Placeholder", "Placeholder", 1L, 1L);
			saveConfigurationFile();
		}
		return true;
	}


	private boolean loadUserList(String path) {
		usersFile = new File(path, "users.json");

		if (!usersFile.getParentFile().exists() && !usersFile.getParentFile().mkdirs()) {
			System.out.println(("Failed to create directory " + usersFile.getParentFile()));
		}

		System.out.println("Attempting to create file at: " + usersFile.getPath());
		boolean fileCreated;
		try {
			fileCreated = usersFile.createNewFile();
		} catch (IOException e) {
			System.out.println("Error occurred while creating file.");
			e.printStackTrace();
			return false;
		}

		if (fileCreated) {
			System.out.println("Successfully created data file...");
		} else {
			System.out.println("Loading previously saved data...");
		}

		List<GPTUser> loadedGptUsers = Collections.synchronizedList(new ArrayList<>());
		try (FileReader reader = new FileReader(usersFile)) {
			loadedGptUsers = GPTHelper.getPrettyGson().fromJson(reader, (new TypeToken<List<GPTUser>>() {
			}).getType());
		} catch (Exception e) {
			System.out.println("Error occurred while reading " + usersFile.getName() + ".");
			e.printStackTrace();
			return false;
		}

		if (loadedGptUsers != null) {
			gptUsers = loadedGptUsers;
		} else {
			gptUsers = Collections.synchronizedList(new ArrayList<>());
		}
		return true;
	}

	private boolean loadRequestsMap(String path) {
		requestsFile = new File(path, "requests.json");

		if (!requestsFile.getParentFile().exists() && !requestsFile.getParentFile().mkdirs()) {
			System.out.println(("Failed to create directory " + requestsFile.getParentFile()));
		}

		System.out.println("Attempting to create file at: " + requestsFile.getPath());
		boolean fileCreated;
		try {
			fileCreated = requestsFile.createNewFile();
		} catch (IOException e) {
			System.out.println("Error occurred while creating file.");
			e.printStackTrace();
			return false;
		}

		if (fileCreated) {
			System.out.println("Successfully created data file...");
		} else {
			System.out.println("Loading previously saved data...");
		}

		ConcurrentHashMap<Long, List<GPTRequest>> loadedRequests;
		try (FileReader reader = new FileReader(requestsFile)) {
			loadedRequests = GPTHelper.getPrettyGson().fromJson(reader, (new TypeToken<ConcurrentHashMap<Long, List<GPTRequest>>>() {
			}).getType());
		} catch (Exception e) {
			System.out.println("Error occurred while reading " + requestsFile.getName() + ".");
			e.printStackTrace();
			return false;
		}

		if (loadedRequests != null) {
			requests = loadedRequests;
		} else {
			requests = new ConcurrentHashMap<>();
		}
		return true;
	}

	public void saveRequestsFile() {
		try (FileWriter writer = new FileWriter(requestsFile)) {
			writer.write(GPTHelper.getPrettyGson().toJson(requests, (new TypeToken<ConcurrentHashMap<Long, GPTRequest>>() {
			}).getType()));
		} catch (IOException e) {
			System.out.println("Error occurred while saving requests file.");
			e.printStackTrace();
		}
	}

	public void saveConfigurationFile() {
		try (FileWriter writer = new FileWriter(configurationFile)) {
			writer.write(GPTHelper.getPrettyGson().toJson(configuration, (new TypeToken<Configuration>() {
			}).getType()));
		} catch (IOException e) {
			System.out.println("Error occurred while saving configuration file.");
			e.printStackTrace();
		}
	}

	public void saveUsersFile() {
		try (FileWriter writer = new FileWriter(usersFile)) {
			writer.write(GPTHelper.getPrettyGson().toJson(gptUsers, (new TypeToken<List<GPTUser>>() {
			}).getType()));
		} catch (IOException e) {
			System.out.println("Error occurred while saving users file.");
			e.printStackTrace();
		}
	}

	public void addRequest(User user, GPTRequest gptRequest) {
		List<GPTRequest> gptRequests = new ArrayList<>();
		if (requests.containsKey(user.getIdLong())) {
			gptRequests = requests.get(user.getIdLong());
		}
		gptRequests.add(gptRequest);
		requests.put(user.getIdLong(), gptRequests);
		saveRequestsFile();
	}

	public void addUser(User user, TextChannel textChannel) {
		GPTUser gptUser = new GPTUser(user.getIdLong(), textChannel.getIdLong());
		gptUsers.add(gptUser);
		saveUsersFile();
	}
}