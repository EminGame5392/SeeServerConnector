package ru.gdev.seeserverconnector;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class SeeServerConnector extends JavaPlugin implements Listener {
    private String serverUrl;
    private String secretKey;
    private boolean debugMode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        startHeartbeatTask();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        serverUrl = config.getString("server-url", "http://localhost:5000");
        secretKey = config.getString("secret-key", "default-secret-key");
        debugMode = config.getBoolean("debug-mode", false);
    }

    private void startHeartbeatTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }.runTaskTimer(this, 0L, 20L * 30);
    }

    private void sendHeartbeat() {
        try {
            String postData = String.format("{\"secret_key\":\"%s\",\"online\":%d}", secretKey, Bukkit.getOnlinePlayers().size());
            sendPostRequest(serverUrl + "/api/heartbeat", postData);
        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("Heartbeat error: " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendPlayerUpdate(event.getPlayer(), "join");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sendPlayerUpdate(event.getPlayer(), "quit");
    }

    private void sendPlayerUpdate(Player player, String action) {
        try {
            String postData = String.format("{\"secret_key\":\"%s\",\"player\":\"%s\",\"action\":\"%s\"}",
                    secretKey, player.getName(), action);
            sendPostRequest(serverUrl + "/api/player_update", postData);
        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("Player update error: " + e.getMessage());
            }
        }
    }

    private void sendPostRequest(String urlString, String postData) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (debugMode) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                getLogger().info("Response: " + response.toString());
            }
        }
        conn.disconnect();
    }
}