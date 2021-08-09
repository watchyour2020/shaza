package sh.aza;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCoin implements CommandExecutor {
    private Shaza shaza;

    public SetCoin(Shaza shaza) {
        this.shaza = shaza;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args.length >= 1 && sender.hasPermission("shaza.coin.set")) {
            Player p = (Player) sender;
            try {
                if (shaza.getUserCoin().containsKey(p.getUniqueId().toString())){
                    shaza.getUserCoin().replace(p.getUniqueId().toString(), Integer.parseInt(args[0]));
                } else {
                    shaza.getUserCoin().put(p.getUniqueId().toString(), Integer.parseInt(args[0]));
                }
            } catch (Exception x){
                x.printStackTrace();
            }
        } else if (sender instanceof Player) {
            sender.sendMessage(ChatColor.BOLD + ">> Hmm. That didn't work out. Try again.");
        } else {
            shaza.getLogger().warning(">> Hmm. That didn't work out. Try again.");
        }
        return false;
    }
}
