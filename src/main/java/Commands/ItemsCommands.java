package Commands;

import Dificultades.CustomMobs.CustomBoat;
import Dificultades.DayOneChanges;
import Habilidades.HabilidadesBook;
import imp.crissyjuanxd.IsManuSMP;
import items.CorruptedGoldenApple;
import items.DoubleLifeTotem;
import items.EconomyItems;
import items.Misionesitem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemsCommands implements CommandExecutor, TabCompleter {

    private final IsManuSMP plugin;
    private final DoubleLifeTotem doubleLifeTotem;
    private final CustomBoat customBoat;

    public ItemsCommands(IsManuSMP plugin) {
        this.plugin = plugin;
        this.doubleLifeTotem = new DoubleLifeTotem(plugin);
        this.customBoat = new CustomBoat(plugin);
        plugin.getCommand("givevct").setExecutor(this);
        plugin.getCommand("givevct").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUso: /givevct <item> [cantidad] [jugador]");
            return true;
        }

        String itemName = args[0].toLowerCase();
        int cantidad = 1;
        Player target = null;

        if (args.length > 1) {
            try {
                cantidad = Integer.parseInt(args[1]);
                if (cantidad <= 0) {
                    sender.sendMessage("§cLa cantidad debe ser mayor a 0.");
                    return true;
                }
            } catch (NumberFormatException e) {
                target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("§cEl jugador '" + args[1] + "' no está en línea.");
                    return true;
                }
            }
        }

        if (args.length > 2) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage("§cEl jugador '" + args[2] + "' no está en línea.");
                return true;
            }
        }

        if (target == null) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("§cDebes especificar un jugador si ejecutas el comando desde la consola.");
                return true;
            }
        }

        ItemStack item;
        switch (itemName) {
            case "doubletotem":
                item = doubleLifeTotem.createDoubleLifeTotem();
                item.setAmount(cantidad);
                break;
            case "corrupted_steak":
                item = DayOneChanges.corruptedSteak();
                item.setAmount(cantidad);
                break;
            case "customboat":
                item = customBoat.createBoatItem(target);
                item.setAmount(cantidad);
                break;
            case "fuel":
                item = customBoat.createFuelItem();
                item.setAmount(cantidad);
                break;
            case "corrupted_golden_apple":
                item = CorruptedGoldenApple.createCorruptedGoldenApple();
                item.setAmount(cantidad);
                break;
            case "libro_habilidades":
                item = HabilidadesBook.createHabilidadesBook();
                item.setAmount(cantidad);
                break;
            case "manucoins":
                item = EconomyItems.createVithiumCoin();
                item.setAmount(cantidad);
                break;
            case "manu_fichas":
                item = EconomyItems.createVithiumToken();
                item.setAmount(cantidad);
                break;
            case "mochila_nivel_1":
                item = EconomyItems.createNormalMochila();
                item.setAmount(cantidad);
                break;
            case "mochila_nivel_2":
                item = EconomyItems.createGreenMochila();
                item.setAmount(cantidad);
                break;
            case "mochila_nivel_3":
                item = EconomyItems.createRedMochila();
                item.setAmount(cantidad);
                break;
            case "mochila_nivel_4":
                item = EconomyItems.createBlueMochila();
                item.setAmount(cantidad);
                break;
            case "mochila_nivel_5":
                item = EconomyItems.createPurpleMochila();
                item.setAmount(cantidad);
                break;
            case "enderbag":
                item = EconomyItems.createEnderBag();
                item.setAmount(cantidad);
                break;
            case "gancho":
                item = EconomyItems.createGancho();
                item.setAmount(cantidad);
                break;
            case "panic_apple":
                item = EconomyItems.createManzanaPanico();
                item.setAmount(cantidad);
                break;
            case "artefacto_nivel_1":
                item = EconomyItems.createYunqueReparadorNivel1();
                item.setAmount(cantidad);
                break;
            case "artefacto_nivel_2":
                item = EconomyItems.createYunqueReparadorNivel2();
                item.setAmount(cantidad);
                break;
            case "misiones":
                item = Misionesitem.createMisiones();
                item.setAmount(cantidad);
                break;
            default:
                sender.sendMessage("§cEse item no existe.");
                return true;
        }

        target.getInventory().addItem(item);
        sender.sendMessage("§aHas dado " + cantidad + "x " + itemName + " a " + target.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("doubletotem");
            completions.add("corrupted_steak");
            completions.add("customboat");
            completions.add("fuel");
            completions.add("corrupted_golden_apple");
            completions.add("libro_habilidades");
            completions.add("manucoins");
            completions.add("manu_fichas");
            completions.add("mochila_nivel_1");
            completions.add("mochila_nivel_2");
            completions.add("mochila_nivel_3");
            completions.add("mochila_nivel_4");
            completions.add("mochila_nivel_5");
            completions.add("enderbag");
            completions.add("gancho");
            completions.add("panic_apple");
            completions.add("artefacto_nivel_1");
            completions.add("artefacto_nivel_2");
            completions.add("misiones");
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}
