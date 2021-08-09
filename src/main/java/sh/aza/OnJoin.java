package sh.aza;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

public class OnJoin implements Listener {
    private final Shaza main;
    private Connection conn;
    private String host, database, username, password;
    private int port;

    public OnJoin(Shaza main) {
        this.main = main;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e){
        this.host = main.getCustomConfig().getString("database.host");
        this.port = main.getCustomConfig().getInt("database.port");
        this.database = main.getCustomConfig().getString("database.name");
        this.username = main.getCustomConfig().getString("database.username");
        this.password = main.getCustomConfig().getString("database.password");
        try {
            main.getCustomConfig().save(main.getCustomYml());
        } catch (IOException x) {
            x.printStackTrace();
        }
        String uuid = e.getUniqueId().toString();
        try {
            openConnection();
            System.out.println("SUCCESS: Successfully connected to database.");
        } catch (SQLException x) {
            x.printStackTrace();
            System.out.println("ERROR: Couldn't connect to database.");
        }
        try {
            ResultSet rs = prepareStatement("SELECT COUNT(UUID) FROM usercoin WHERE UUID = '" + uuid + "';").executeQuery();
            rs.next();

            if (rs.getInt(1) == 0){
                prepareStatement("INSERT INTO usercoin(UUID, COIN) VALUES ('" + uuid + "', 0);").executeUpdate();
            }
            ResultSet coin = prepareStatement("SELECT * FROM usercoin WHERE UUID='" + uuid + "';").executeQuery();
            coin.next();
            main.getUserCoin().put(uuid, coin.getInt(2));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        BukkitScheduler scheduler = main.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(main, new BukkitRunnable() {
            @Override
            public void run(){
                if (p == null){
                    this.cancel();
                } else {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BOLD + "" + ChatColor.YELLOW + "Coins: " + ChatColor.WHITE + main.getUserCoin().get(p.getUniqueId().toString())));
                }
            }
        }, 0L, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        try {
            prepareStatement("UPDATE usercoin SET COIN = '" + main.getUserCoin().get(p.getUniqueId().toString()) + "' WHERE UUID = '" + p.getUniqueId().toString() + "'").executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Connection getConn() {
        return conn;
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
