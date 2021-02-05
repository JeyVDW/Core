package dev.minecode.core.bungeecord;

import dev.minecode.core.api.CoreAPI;
import dev.minecode.core.bungeecord.test.Test;
import dev.minecode.core.common.CoreCommon;
import dev.minecode.core.common.api.object.CorePlayerProvider;
import net.md_5.bungee.api.plugin.Plugin;

public class CoreBungeeCord extends Plugin {
    private static CoreBungeeCord instance;

    private Plugin mainClass;

    public CoreBungeeCord(Plugin mainClass) {
        this.mainClass = mainClass;
        makeInstances();
    }

    private void makeInstances() {
        instance = this;
        new CoreCommon(mainClass.getDescription().getName(), mainClass.getDescription().getVersion());
        new Test();
    }

    public void onDisable() {
        for (CorePlayerProvider corePlayer : CorePlayerProvider.getIdCache().values())
            corePlayer.save();

        CoreAPI.getInstance().getFileManager().saveDatas();
        if (CoreAPI.getInstance().isUsingSQL())
            CoreAPI.getInstance().getDatabaseManager().disconnect();
    }

    public static CoreBungeeCord getInstance() {
        return instance;
    }

    public Plugin getMainClass() {
        return mainClass;
    }
}
