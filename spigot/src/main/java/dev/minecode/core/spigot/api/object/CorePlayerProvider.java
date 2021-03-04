package dev.minecode.core.spigot.api.object;

import dev.minecode.core.api.CoreAPI;
import dev.minecode.core.api.object.CorePlayer;
import dev.minecode.core.api.object.FileObject;
import dev.minecode.core.api.object.Language;
import dev.minecode.core.common.CoreCommon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class CorePlayerProvider implements CorePlayer {

    private static UUID consoleUUID = new UUID(0, 0);
    private static int consoleID = 1;
    private static String consoleName = "CONSOLE";

    private static FileObject dataFileObject = CoreAPI.getInstance().getFileManager().getPlayers();
    private static ConfigurationNode dataConf;

    private int id;
    private UUID uuid;
    private String name;
    private Language language;
    private boolean exists = false;
    private Statement statement;
    private ResultSet resultSet;

    public CorePlayerProvider(int id) {
        makeInstances();

        this.id = id;
        load();
    }

    public CorePlayerProvider(UUID uuid) {
        makeInstances();

        this.id = getID(uuid);
        this.uuid = uuid;
        load();
    }

    public CorePlayerProvider(String name) {
        makeInstances();

        this.id = getID(name);
        if (id != 0) {
            this.name = getName(id);
            exists = true;
            reload();
        } else {
            this.name = name;
            load();
        }
    }

    private static boolean create(int id, UUID uuid, String name, Language language) {
        try {
            String isocode = null;
            if (language != null)
                isocode = language.getIsocode();

            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                if (isocode != null)
                    isocode = "'" + language + "'";
                CoreAPI.getInstance().getDatabaseManager().getStatement().executeUpdate("INSERT INTO minecode_players (ID, UUID, NAME, LANGUAGE) VALUES (" + id + ",'" + uuid.toString() + "', '" + name + "', " + isocode + ")");
                return true;
            }

            dataConf.node(String.valueOf(id), "uuid").set(uuid.toString());
            dataConf.node(String.valueOf(id), "name").set(name);
            dataConf.node(String.valueOf(id), "language").set(isocode);
            return dataFileObject.save();
        } catch (SQLException | SerializationException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static int getID(UUID uuid) {
        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                ResultSet resultSet = CoreAPI.getInstance().getDatabaseManager().getStatement().executeQuery("SELECT ID FROM minecode_players WHERE UUID = '" + uuid.toString() + "'");
                if (resultSet.next())
                    return resultSet.getInt("ID");
            } else
                for (Map.Entry<Object, ? extends ConfigurationNode> uuidNode : dataConf.childrenMap().entrySet())
                    if (!uuidNode.getValue().empty())
                        if (uuidNode.getValue().node("uuid").getString().equalsIgnoreCase(uuid.toString()))
                            return Integer.parseInt((String) uuidNode.getValue().key());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public static int getID(String name) {
        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                ResultSet resultSet = CoreAPI.getInstance().getDatabaseManager().getStatement().executeQuery("SELECT ID FROM minecode_players WHERE UPPER(NAME) = UPPER('" + name + "')");
                if (resultSet.next())
                    return resultSet.getInt("ID");
            } else
                for (Map.Entry<Object, ? extends ConfigurationNode> uuidNode : dataConf.childrenMap().entrySet())
                    if (!uuidNode.getValue().empty())
                        if (uuidNode.getValue().node("name").getString().equalsIgnoreCase(name))
                            return Integer.parseInt((String) uuidNode.getValue().key());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public static UUID getUuid(int id) {
        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                ResultSet resultSet = CoreAPI.getInstance().getDatabaseManager().getStatement().executeQuery("SELECT UUID FROM minecode_players WHERE ID = '" + id + "'");
                if (resultSet.next())
                    return UUID.fromString(resultSet.getString("UUID"));
            } else {
                String temp = dataConf.node(String.valueOf(id), "name").getString();
                if (temp != null) return UUID.fromString(temp);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static UUID getUuid(String name) {
        Player player;
        if ((player = Bukkit.getPlayer(name)) != null)
            return player.getUniqueId();

        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                ResultSet resultSet = CoreAPI.getInstance().getDatabaseManager().getStatement().executeQuery("SELECT UUID FROM minecode_players WHERE UPPER(NAME) = UPPER('" + name + "')");
                if (resultSet.next())
                    return UUID.fromString(resultSet.getString("UUID"));
            } else
                for (Map.Entry<Object, ? extends ConfigurationNode> uuidNode : dataConf.childrenMap().entrySet())
                    if (!uuidNode.getValue().empty())
                        if (uuidNode.getValue().node("uuid").getString().equalsIgnoreCase(name))
                            return UUID.fromString(uuidNode.getValue().node("uuid").getString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return CoreCommon.getInstance().getUuidFetcher().getUUID(name);
    }

    public static String getName(int id) {
        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                ResultSet resultSet = CoreAPI.getInstance().getDatabaseManager().getStatement().executeQuery("SELECT NAME FROM minecode_players WHERE ID = '" + id + "'");
                if (resultSet.next())
                    return resultSet.getString("NAME");
            } else {
                String temp = dataConf.node(String.valueOf(id), "name").getString();
                if (temp != null) return temp;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static String getName(UUID uuid) {
        Player player;
        if ((player = Bukkit.getPlayer(uuid)) != null)
            return player.getName();

        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                ResultSet resultSet = CoreAPI.getInstance().getDatabaseManager().getStatement().executeQuery("SELECT NAME FROM minecode_players WHERE UUID = '" + uuid + "'");
                if (resultSet.next())
                    return resultSet.getString("NAME");
            } else {
                for (Map.Entry<Object, ? extends ConfigurationNode> uuidNode : dataConf.childrenMap().entrySet())
                    if (!uuidNode.getValue().empty())
                        if (uuidNode.getValue().node("uuid").getString().equalsIgnoreCase(uuid.toString()))
                            return uuidNode.getValue().node("name").getString();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return CoreCommon.getInstance().getUuidFetcher().getName(uuid);
    }

    public static boolean isAvailableID(int id) {
        return getUuid(id) != null && id > 0;
    }

    private static int generateNewID() {
        int id;
        do {
            id = new Random().nextInt(Integer.MAX_VALUE - 1);
        } while (isAvailableID(id));
        return id;
    }

    public void makeInstances() {
        if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
            statement = CoreAPI.getInstance().getDatabaseManager().getStatement();
            return;
        }

        dataFileObject.reload();
        dataConf = dataFileObject.getConf();
    }

    public void load() {
        try {
            if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                resultSet = statement.executeQuery("SELECT * FROM minecode_players WHERE ID = " + id + "");
                exists = resultSet.next();
            } else
                exists = !dataConf.node(String.valueOf(id)).empty();

            if (!exists) {
                System.out.println(1);
                if (id == consoleID || uuid == consoleUUID || Objects.equals(name, consoleName)) {
                    id = consoleID;
                    uuid = consoleUUID;
                    name = consoleName;
                    exists = true;
                    return;
                }

                if (uuid == null && name == null) return;
                if (uuid == null && (uuid = getUuid(name)) == null) return;
                name = getName(uuid);
                id = generateNewID();
                if (id != 0 && uuid != null && name != null) {
                    create(id, uuid, name, null);
                    exists = true;
                }
            } else reload();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public boolean reload() {
        if (exists) {
            try {
                if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                    resultSet = statement.executeQuery("SELECT * FROM minecode_players WHERE ID = " + id + "");
                    if (resultSet.next()) {
                        uuid = UUID.fromString(resultSet.getString("UUID"));
                        name = resultSet.getString("NAME");
                        language = CoreAPI.getInstance().getLanguageManager().getLanguage(resultSet.getString("LANGUAGE"));
                        return true;
                    } else {
                        load();
                        return exists;
                    }
                } else {
                    uuid = UUID.fromString(Objects.requireNonNull(dataConf.node(String.valueOf(id), "uuid").getString()));
                    name = dataConf.node(String.valueOf(id), "name").getString();
                    language = CoreAPI.getInstance().getLanguageManager().getLanguage(dataConf.node(String.valueOf(id), "language").getString());
                    return true;

                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean save() {
        if (exists) {
            try {
                if (CoreAPI.getInstance().getPluginManager().isUsingSQL()) {
                    resultSet.updateString("UUID", uuid.toString());
                    resultSet.updateString("NAME", name);
                    resultSet.updateString("LANGUAGE", language != null ? language.getIsocode() : null);
                    resultSet.updateRow();
                } else {
                    dataConf.node(String.valueOf(id), "uuid").set(uuid.toString());
                    dataConf.node(String.valueOf(id), "name").set(name);
                    dataConf.node(String.valueOf(id), "language").set(language != null ? language.getIsocode() : null);
                    dataFileObject.save();
                }
                return true;
            } catch (SQLException | SerializationException throwables) {
                throwables.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public boolean setID(int id) {
        if (!isAvailableID(id)) return false;

        this.id = id;
        return true;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean setUuid(UUID uuid) {
        if (CoreCommon.getInstance().getUuidFetcher().getName(uuid) == null) return false;

        this.uuid = uuid;
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean setName(String name) {
        if (CoreCommon.getInstance().getUuidFetcher().getUUID(name) == null) return false;

        this.name = name;
        return true;
    }

    @Override
    public Language getLanguage() {
        return language != null ? language : CoreAPI.getInstance().getLanguageManager().getDefaultLanguage();
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public boolean isLanguageEmpty() {
        return language == null;
    }

    @Override
    public boolean isExists() {
        return exists;
    }
}
