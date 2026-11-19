package net.rageevent.hypixel.player.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import dev.prism.api.entity.Player;
import dev.prism.api.event.player.PlayerChatEvent;
import lombok.SneakyThrows;
import net.rageevent.hypixel.player.UserData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.*;

public class PlayerTracker {

    private final ObjectNode root;
    private final Path rootPath;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private final ConcurrentMap<UUID, UserData> userDataStorage = new ConcurrentHashMap<>();


    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new ParameterNamesModule())
            .registerModule(new AfterburnerModule())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    @SneakyThrows
    public PlayerTracker(Path rootPath) {
        if (!Files.exists(rootPath)) Files.createDirectories(rootPath);
        Path playerData = rootPath.resolve("player-data.json");

        if (!Files.exists(playerData)) {
            Files.createFile(playerData);
            Files.writeString(playerData, "{}");
        }
        this.rootPath = playerData;
        this.root = (ObjectNode) MAPPER.readTree(playerData.toFile());
    }

    public void flushData() {
        io.shutdown();
        try {
            if (!io.awaitTermination(5, TimeUnit.SECONDS)) {
                io.shutdownNow();
            }
        } catch (InterruptedException e) {
            io.shutdownNow();
        }
    }

    public UserData getOrCreate(Player player) {
        UserData userData = loadPlayer(player);
        return userData == null ? userDataStorage.putIfAbsent(player.uuid(), UserData.fresh(player)) : get(player);
    }

    public UserData get(Player player) {
        return userDataStorage.putIfAbsent(player.uuid(), loadPlayer(player));
    }

    public void put(Player player, UserData userData) {
        userDataStorage.put(player.uuid(), userData);
        saveAsync(player);
    }

    public void remove(Player player) {
        saveAsync(player);
        userDataStorage.remove(player.uuid());
    }

    private void saveAsync(Player player) {
        UserData snapshot = userDataStorage.putIfAbsent(player.uuid(), loadPlayer(player));
        System.out.println(snapshot);

        root.set(player.uuid().toString(), MAPPER.valueToTree(snapshot));
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(rootPath.toFile(), root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UserData loadPlayer(Player player) {
        JsonNode userNode = root.get(player.uuid().toString());
        if (userNode == null) //Extracted because the mapper will throw.
            return null;

        return MAPPER.convertValue(userNode, UserData.class);
    }

}
