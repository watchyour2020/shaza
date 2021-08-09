package sh.aza;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public final class Shaza extends JavaPlugin {
    private File customYml;
    private FileConfiguration customConfig;
    private HashMap<String, Integer> userCoin;
    private Connection conn;
    private String password,username,database,host;
    private int port;

    @Override
    public void onEnable() {
        this.customYml = new File(getDataFolder()+"/database.yml");
        this.customConfig = YamlConfiguration.loadConfiguration(customYml);
        this.host = getCustomConfig().getString("database.host");
        this.port = getCustomConfig().getInt("database.port");
        this.database = getCustomConfig().getString("database.name");
        this.username = getCustomConfig().getString("database.username");
        this.password = getCustomConfig().getString("database.password");
        this.userCoin = new HashMap<String, Integer>();
        getServer().getPluginManager().registerEvents(new OnJoin(this), this);
        getCommand("setcoin").setExecutor(new SetCoin(this));
        if (!customYml.exists()){
            try {
                customYml.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            customConfig.set("database.host", "localhost");
            customConfig.set("database.port", 3306);
            customConfig.set("database.username", "root");
            customConfig.set("database.name", "minecraft");
            customConfig.set("database.password", "");
        }
        getLogger().info("Shaza -- Enabled");
    }

    public File getCustomYml() {
        return customYml;
    }

    public FileConfiguration getCustomConfig() {
        return customConfig;
    }

    public HashMap<String, Integer> getUserCoin() {
        return userCoin;
    }

    @Override
    public void onDisable() {
        try {
            openConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Player p: Bukkit.getOnlinePlayers()){
            try {
                prepareStatement("UPDATE usercoin SET COIN = '" + getUserCoin().get(p.getUniqueId().toString()) + "' WHERE UUID = '" + p.getUniqueId().toString() + "'").executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        getLogger().info("Shaza -- Goodbye!");
    }

    public void openConnection() throws SQLException {
        if (conn != null && !conn.isClosed()){
            return;
        }

        conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
    }

    public PreparedStatement prepareStatement(String query){
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            System.out.println("SUCCESS: Successfully prepared statement.");
        } catch (SQLException x) {
            x.printStackTrace();
            System.out.println("ERROR: Couldn't prepare statement.");
        }
        return ps;
    }
}
