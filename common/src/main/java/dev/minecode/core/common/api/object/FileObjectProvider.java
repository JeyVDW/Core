package dev.minecode.core.common.api.object;

import dev.minecode.core.api.object.FileObject;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;

public class FileObjectProvider implements FileObject {

    private static HashMap<String, FileObject> fileObjects = new HashMap<>();

    // directories
    private String minecodeDirectoryPath;
    private String pluginDirectoryPath;
    private String fileDirectoryPath;

    // file
    private String fileName;
    private String fileStreamPath;
    private File file;

    // Configurate
    private YamlConfigurationLoader loader;
    private ConfigurationNode conf;

    // other
    private boolean stream;

    public FileObjectProvider(String fileName, String pluginName, String... folders) {
        this.minecodeDirectoryPath = "plugins/MineCode";
        this.pluginDirectoryPath = minecodeDirectoryPath + "/" + pluginName;
        this.fileName = fileName;

        StringBuilder foldersStringBuilder = new StringBuilder();
        for (String temp : folders) {
            foldersStringBuilder.append(temp).append("/");
        }

        this.fileDirectoryPath = pluginDirectoryPath + "/" + foldersStringBuilder.toString();
        this.fileStreamPath =  pluginName + "/" + foldersStringBuilder.toString() + fileName;
        this.file = new File(fileDirectoryPath, fileName);
        createFile();
    }

    public FileObjectProvider(String fileName, String pluginName) {
        this.minecodeDirectoryPath = "plugins/MineCode";
        this.pluginDirectoryPath = minecodeDirectoryPath + "/" + pluginName;
        this.fileName = fileName;

        this.fileDirectoryPath = pluginDirectoryPath;
        this.fileStreamPath =  pluginName + "/" + fileName;
        this.file = new File(fileDirectoryPath, fileName);
        createFile();
    }

    public FileObject createFile() {
        new File(fileDirectoryPath).mkdirs();
        if (!file.exists()) {
            InputStream inputStream = getResourceAsStream(fileStreamPath);
            if (inputStream != null) {
                try {
                    Files.copy(inputStream, file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stream = true;
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stream = false;
            }
        }
        reload();
        return this;
    }

    @Override
    public boolean reload() {
        this.loader = YamlConfigurationLoader.builder().file(file).build();
        try {
            conf = loader.load();
            return true;
        } catch (ConfigurateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean save() {
        try {
            loader.save(conf);
            return true;
        } catch (ConfigurateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public YamlConfigurationLoader getLoader() {
        return loader;
    }

    @Override
    public ConfigurationNode getConf() {
        return conf;
    }

    @Override
    public boolean isStream() {
        return stream;
    }

    public InputStream getResourceAsStream(String fileName) {
        try {
            URL url = this.getClass().getClassLoader().getResource(fileName);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }

    public static HashMap<String, FileObject> getFileObjects() {
        return fileObjects;
    }
}
