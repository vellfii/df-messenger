package net.velli.df_messenger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MessageData {
    private static final Path DATA_PATH = FabricLoader.getInstance().getConfigDir().resolve("df_messenger_data.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static MessageData instance = new MessageData();

    public HashMap<String, List<String>> messages = new HashMap<>();
    public List<String> playerOrder = new ArrayList<>();

    // Adds player to player list, or bumps to the front if already added.
    public static void addOrBumpPlayer(String name) {
        if (!instance.messages.containsKey(name)) {
            instance.messages.put(name, new ArrayList<>());
            instance.playerOrder.addFirst(name);
        } else {
            instance.playerOrder.remove(name);
            instance.playerOrder.addFirst(name);
        }
    }

    public static void loadMessages() {
        if (Files.exists(DATA_PATH)) {
            try (Reader reader = Files.newBufferedReader(DATA_PATH)) {
                instance = GSON.fromJson(reader, MessageData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else saveMessages();
    }

    public static void saveMessages() {
        try (Writer writer = Files.newBufferedWriter(DATA_PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
