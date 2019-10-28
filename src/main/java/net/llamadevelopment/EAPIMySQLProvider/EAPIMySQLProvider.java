package net.llamadevelopment.EAPIMySQLProvider;

import cn.nukkit.plugin.PluginBase;
import me.onebone.economyapi.EconomyAPI;
import net.llamadevelopment.EAPIMySQLProvider.provider.MySQLProvider;

public class EAPIMySQLProvider extends PluginBase {

    public static EAPIMySQLProvider instance;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        EconomyAPI.getInstance().addProvider("mysql", MySQLProvider.class);
    }

    @Override
    public void onEnable() {

    }

    public static EAPIMySQLProvider getInstance() {
        return instance;
    }
}
