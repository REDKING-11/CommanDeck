package xyz.redking.commanddeck.other;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import xyz.redking.commanddeck.data.ActionButtonData;
import xyz.redking.commanddeck.data.ActionButtonDataJson;
import xyz.redking.commanddeck.platform.Platform;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class ActionButtonDataHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ACTION_LIST_TYPE = new TypeToken<List<ActionButtonDataJson>>() {
    }.getType();

    public static final List<ActionButtonData> actions = new ArrayList<>();

    private ActionButtonDataHandler() {
    }

    private static File configFile() {
        return Platform.getConfigDir().resolve("commanddeck_data.json").toFile();
    }

    public static void initialize() {
        load();
    }

    public static void add(ActionButtonData action) {
        actions.add(action);
        save();
    }

    public static void remove(ActionButtonData action) {
        actions.remove(action);
        save();
    }

    public static void load() {
        File file = configFile();
        if (!file.exists()) {
            return;
        }

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            List<ActionButtonDataJson> jsonList = GSON.fromJson(content, ACTION_LIST_TYPE);
            actions.clear();
            if (jsonList != null) {
                for (ActionButtonDataJson json : jsonList) {
                    actions.add(json.toActionButtonData());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            List<ActionButtonDataJson> jsonList = new ArrayList<>();
            for (ActionButtonData action : actions) {
                jsonList.add(ActionButtonDataJson.fromActionButtonData(action));
            }
            Files.createDirectories(configFile().toPath().getParent());
            Files.writeString(configFile().toPath(), GSON.toJson(jsonList, ACTION_LIST_TYPE), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
