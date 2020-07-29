package me.geek.tom.joinbot.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String BOT_CONFIG_REPO = "Geek202/JoinBot";
    private final Map<String, Config> configs = new HashMap<>();
    private final GitHub gh;

    public ConfigManager(String... servers) throws IOException {
        LOGGER.info("Connecting to GitHub...");
        gh = new GitHubBuilder().build();
        LOGGER.info("Loading server configurations...");
        loadServers(servers);
        LOGGER.info("Loaded config for " + configs.size() + " servers!");
    }

    public void loadServers(String[] servers) throws IOException {
        GHRepository repo = gh.getRepository(BOT_CONFIG_REPO);
        for (String server : servers) {
            LOGGER.info("Loading config for server: " + server + "...");
            try {
                loadConfig(repo, server);
            } catch (FileNotFoundException e) {
                LOGGER.warn("No config for server: " + server);
            }
        }
    }

    private void loadConfig(GHRepository repo, String server) throws IOException {
        Config cfg = Config.readConfig(repo.getFileContent("servers/" + server + ".properties").read());
        if (cfg == null)
            LOGGER.warn("Invalid config: " + server + ".properties!");
        else
            configs.put(server, cfg);
    }

    public Config getServerConfig(String serverId) {
        return configs.get(serverId);
    }

    public void reloadServerConfig(String serverId) throws IOException {
        LOGGER.info("Reloading config for server: " + serverId);
        GHRepository repo = gh.getRepository(BOT_CONFIG_REPO);
        loadConfig(repo, serverId);
    }
}
