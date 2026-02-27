package Commands;

import Events.AchievementParty.AchievementGUI;
import Events.AchievementParty.AchievementPartyHandler;
import Events.ItemParty.ItemPartyHandler;
import Events.Skybattle.EventoHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventsCommands implements TabExecutor {

    private final EventoHandler eventoHandler;
    private final AchievementPartyHandler achievementPartyHandler;
    private final ItemPartyHandler itemPartyHandler;
    private final AchievementGUI achievementGUI;

    public EventsCommands(EventoHandler eventoHandler, AchievementPartyHandler achievementPartyHandler,
                          ItemPartyHandler itemPartyHandler, AchievementGUI achievementGUI) {
        this.eventoHandler = eventoHandler;
        this.achievementPartyHandler = achievementPartyHandler;
        this.itemPartyHandler = itemPartyHandler;
        this.achievementGUI = achievementGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Quitamos la restricción general de args.length == 0 aquí porque
        // algunos comandos como /logros pueden no tener argumentos.
        // Lo manejaremos dentro de cada case o helper si es necesario.

        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "start":
                return handleStartCommand(sender, args);
            case "end":
                return handleEndCommand(sender, args);
            case "evento1":
                return handleEvento1Command(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "logros":
                return handleLogrosCommand(sender);
            case "reloadevent":
                return handleReloadEventCommand(sender, args);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleStartCommand(CommandSender sender, String[] args) {
        // CORREGIDO: Se requiere al menos 1 argumento (el nombre del evento)
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /start <evento1|skybattle|force|reglas|resetpurple|logros|itemparty>");
            return true;
        }

        String eventType = args[0].toLowerCase(); // CORREGIDO: args[0] es el primer argumento
        switch (eventType) {
            case "evento1":
                eventoHandler.iniciarEvento();
                sender.sendMessage(ChatColor.GREEN + "Evento 1 iniciado.");
                break;
            case "skybattle":
                eventoHandler.iniciarSecuenciaInicioSkyBattle();
                sender.sendMessage(ChatColor.GREEN + "SkyBattle iniciado.");
                break;
            case "force":
                eventoHandler.forzarEvento();
                sender.sendMessage(ChatColor.GREEN + "Evento forzado.");
                break;
            case "reglas":
                eventoHandler.espera1();
                sender.sendMessage(ChatColor.GREEN + "Mostrando reglas...");
                break;
            case "resetpurple":
                eventoHandler.eliminarPurpleConcrete();
                sender.sendMessage(ChatColor.GREEN + "Purple concrete eliminado.");
                break;
            case "logros":
                achievementPartyHandler.startEvent(sender);
                break;
            case "itemparty":
                itemPartyHandler.iniciarEvento();
                sender.sendMessage(ChatColor.GREEN + "ItemParty iniciado.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Evento no reconocido. Usa: evento1, skybattle, force, reglas, resetpurple, logros, itemparty");
        }
        return true;
    }

    private boolean handleEndCommand(CommandSender sender, String[] args) {
        // CORREGIDO: Se requiere 1 argumento
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /end <evento1|logros|itemparty>");
            return true;
        }

        String eventType = args[0].toLowerCase(); // CORREGIDO: args[0]
        switch (eventType) {
            case "evento1":
                eventoHandler.terminarEvento();
                sender.sendMessage(ChatColor.GREEN + "Evento 1 terminado.");
                break;
            case "logros":
                achievementPartyHandler.endEvent(sender);
                break;
            case "itemparty":
                itemPartyHandler.terminarEvento();
                sender.sendMessage(ChatColor.GREEN + "ItemParty terminado.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Evento no reconocido. Usa: evento1, logros, itemparty");
        }
        return true;
    }

    private boolean handleEvento1Command(CommandSender sender, String[] args) {
        // CORREGIDO: args[0] es "participantes"
        if (args.length > 0 && args[0].equalsIgnoreCase("participantes")) {
            // Pasamos los argumentos restantes a gestionarParticipantes
            // Si el comando es /evento1 participantes add pepe
            // args original: [participantes, add, pepe]
            // Arrays.copyOfRange(args, 1, args.length) -> [add, pepe]
            String[] participantArgs = Arrays.copyOfRange(args, 1, args.length);
            eventoHandler.gestionarParticipantes(sender, participantArgs);
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Uso: /evento1 participantes <list|add|remove> [jugador]");
        return true;
    }

    private boolean handleResetCommand(CommandSender sender, String[] args) {
        // CORREGIDO: args[0]
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /reset <logros|itempartyplayers>");
            return true;
        }

        String resetType = args[0].toLowerCase();
        switch (resetType) {
            case "logros":
                achievementPartyHandler.resetEvent(sender);
                sender.sendMessage(ChatColor.GREEN + "Logros reseteados.");
                break;
            case "itempartyplayers":
                itemPartyHandler.resetPlayersFile();
                sender.sendMessage(ChatColor.GREEN + "Jugadores de ItemParty reseteados.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Tipo de reset no reconocido. Usa: logros, itempartyplayers");
        }
        return true;
    }

    private boolean handleLogrosCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        Player player = (Player) sender;
        if (achievementGUI != null) {
            achievementGUI.openAchievementGUI(player);
        } else {
            player.sendMessage(ChatColor.RED + "El sistema de logros no está disponible en este momento.");
        }
        return true;
    }

    private boolean handleReloadEventCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /reloadevent <itempartyconfig>");
            return true;
        }

        if (args[0].equalsIgnoreCase("itempartyconfig")) {
            itemPartyHandler.reloadConfig();
            sender.sendMessage("§aConfiguración de ItemParty recargada correctamente.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Configuración no reconocida. Usa: itempartyconfig");
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Comando no reconocido. Comandos disponibles:");
        sender.sendMessage(ChatColor.GRAY + "/start, /end, /evento1, /reset, /logros, /reloadevent");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "start":
                // args.length es 1 cuando escribes "/start " (el cursor espera el primer argumento)
                if (args.length == 1) {
                    List<String> startOptions = Arrays.asList("evento1", "skybattle", "force", "reglas", "resetpurple", "logros", "itemparty");
                    StringUtil.copyPartialMatches(args[0], startOptions, completions);
                }
                break;

            case "end":
                if (args.length == 1) {
                    List<String> endOptions = Arrays.asList("evento1", "logros", "itemparty");
                    StringUtil.copyPartialMatches(args[0], endOptions, completions);
                }
                break;

            case "evento1":
                if (args.length == 1) {
                    List<String> eventoOptions = Collections.singletonList("participantes");
                    StringUtil.copyPartialMatches(args[0], eventoOptions, completions);
                } else if (args.length == 2 && args[0].equalsIgnoreCase("participantes")) {
                    List<String> participantOptions = Arrays.asList("list", "add", "remove");
                    StringUtil.copyPartialMatches(args[1], participantOptions, completions);
                } else if (args.length == 3 && args[0].equalsIgnoreCase("participantes") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                    // Autocompletar nombres de jugadores si es add o remove
                    return null; // Null por defecto devuelve lista de jugadores online
                }
                break;

            case "reset":
                if (args.length == 1) {
                    List<String> resetOptions = Arrays.asList("logros", "itempartyplayers");
                    StringUtil.copyPartialMatches(args[0], resetOptions, completions);
                }
                break;

            case "reloadevent":
                if (args.length == 1) {
                    List<String> reloadOptions = Collections.singletonList("itempartyconfig");
                    StringUtil.copyPartialMatches(args[0], reloadOptions, completions);
                }
                break;
        }

        Collections.sort(completions);
        return completions;
    }
}