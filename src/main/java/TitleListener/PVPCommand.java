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
    private static final List<String> COMMANDS = Arrays.asList("activate", "deactivate", "debug");

    public PVPCommand(PVPAnimation pvpAnimation) {
        this.pvpAnimation = pvpAnimation;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("plugin.pvp.toggle")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUso: /pvp <activate|deactivate|debug>");
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "activate":
                if (pvpAnimation.isPvPEnabled()) {
                    sender.sendMessage("§cEl PVP ya está activado!");
                    return true;
                }
                // Activación Manual
                pvpAnimation.togglePVP(true, true);
                sender.sendMessage("§aPVP activado manualmente. No se desactivará automáticamente.");
                break;

            case "deactivate":
                if (!pvpAnimation.isPvPEnabled()) {
                    sender.sendMessage("§cEl PVP ya está desactivado!");
                    return true;
                }
                // Desactivación Manual
                pvpAnimation.togglePVP(false, true);
                sender.sendMessage("§aPVP desactivado manualmente. El contador de 2 horas se ha reiniciado.");
                break;

            case "debug":
                // Muestra la información de debug
                sender.sendMessage(pvpAnimation.getDebugInfo());
                break;

            default:
                sender.sendMessage("§cArgumento inválido. Usa /pvp <activate|deactivate|debug>");
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