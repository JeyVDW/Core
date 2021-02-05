package dev.minecode.core.spigot;

import dev.minecode.core.api.CoreAPI;
import dev.minecode.core.common.CoreCommon;
import dev.minecode.core.common.api.object.CorePlayerProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CoreSpigot {
    private static CoreSpigot instance;

    private JavaPlugin mainClass;

    public CoreSpigot(JavaPlugin mainClass) {
        this.mainClass = mainClass;
        makeInstances();
    }

    private void makeInstances() {
        instance = this;
        new CoreCommon(mainClass.getDescription().getName(), mainClass.getDescription().getVersion());
    }

    public void onDisable() {
        for (CorePlayerProvider corePlayer : CorePlayerProvider.getIdCache().values())
            corePlayer.save();

        CoreAPI.getInstance().getFileManager().saveDatas();
        if (CoreAPI.getInstance().isUsingSQL())
            CoreAPI.getInstance().getDatabaseManager().disconnect();
    }

    public static CoreSpigot getInstance() {
        return instance;
    }

    public JavaPlugin getMainClass() {
        return mainClass;
    }

}