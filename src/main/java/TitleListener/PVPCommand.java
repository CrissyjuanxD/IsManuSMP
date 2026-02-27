package TitleListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PVPCommand implements TabCompleter, CommandExecutor {
    private final PVPAnimation pvpAnimation;
    private static final List<String> COMMANDS = Arrays.asList("activate", "deactivate");

    public PVPCommand(PVPAnimation pvpAnimation) {
        this.pvpAnimation = pvpAnimation;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("plugin.pvp.toggle")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUso: /pvp <activate|deactivate>");
            return true;
        }

        if (!sender.hasPermission("plugin.pvp.toggle")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }


        String action = args[0].toLowerCase();

        switch (action) {
            case "activate":
                if (pvpAnimation.isPvPEnabled()) {
                    sender.sendMessage("§cEl PVP ya está activado!");
                    return true;
                }
                pvpAnimation.togglePVP(true);
                sender.sendMessage("§aPVP activado correctamente!");
                break;

            case "deactivate":
                if (!pvpAnimation.isPvPEnabled()) {
                    sender.sendMessage("§cEl PVP ya está desactivado!");
                    return true;
                }
                pvpAnimation.togglePVP(false);
                sender.sendMessage("§aPVP desactivado correctamente!");
                break;

            default:
                sender.sendMessage("§cArgumento inválido. Usa /pvp <activate|deactivate>");
                return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], COMMANDS, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}