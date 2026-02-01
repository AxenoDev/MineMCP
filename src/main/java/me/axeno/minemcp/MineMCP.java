package me.axeno.minemcp;

import org.bukkit.plugin.java.JavaPlugin;

public final class MineMCP extends JavaPlugin
{

    static MineServer server;
    private static MineMCP instance;

    public static MineMCP getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;
        saveDefaultConfig();

        String token = getConfig().getString("server.token", "changeme");

        int port = getConfig().getInt("server.port", 3000);

        server = new MineServer(token);
        server.start(port);

        getSLF4JLogger().info("MineMCP has been enabled!");
    }

    @Override
    public void onDisable()
    {
        server.stop();
        getSLF4JLogger().info("MineMCP has been disabled!");
    }
}
