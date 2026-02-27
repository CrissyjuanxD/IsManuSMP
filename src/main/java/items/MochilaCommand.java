package items;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MochilaCommand implements CommandExecutor {

    private final EconomyItemsFunctions functions;

    public MochilaCommand(EconomyItemsFunctions functions) {
        this.functions = functions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ismanusmp.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /mochilas <jugador | lost> [give]");
            return true;
        }

        if (args[0].equalsIgnoreCase("lost")) {
            if (sender instanceof Player) {
                functions.openAllBackpacksMenu((Player) sender);
                sender.sendMessage(ChatColor.YELLOW + "Cargando registro de mochilas...");
            } else {
                sender.sendMessage("Solo jugadores.");
            }
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        // Lógica para /mochilas give <jugador>
        if (args.length > 1 && args[0].equalsIgnoreCase("give")) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                return true;
            }
            if (sender instanceof Player) openGiveMenu((Player) sender, target);
            return true;
        }

        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[0]);

        // Verificamos si alguna vez ha entrado al server (tiene datos)
        if (!targetOffline.hasPlayedBefore() && !targetOffline.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Este jugador nunca ha entrado al servidor.");
            return true;
        }

        if (sender instanceof Player) {
            openRecoveryMenu((Player) sender, targetOffline);
        }

        return true;
    }

    private void openGiveMenu(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Dar Mochila a " + target.getName());
        inv.setItem(0, EconomyItems.createNormalMochila());
        inv.setItem(1, EconomyItems.createGreenMochila());
        inv.setItem(2, EconomyItems.createRedMochila());
        inv.setItem(3, EconomyItems.createBlueMochila());
        inv.setItem(4, EconomyItems.createPurpleMochila());
        admin.openInventory(inv);
    }

    private void openRecoveryMenu(Player admin, OfflinePlayer target) {
        admin.sendMessage(ChatColor.YELLOW + "Buscando mochilas en la base de datos para: " + target.getName() + "...");

        // Hacemos la consulta Asíncrona para no congelar el server
        new BukkitRunnable() {
            @Override
            public void run() {
                // 1. Obtener IDs desde la Base de Datos
                List<String> ids = functions.getBackpacksByOwner(target.getUniqueId());

                // 2. Volver al hilo principal para abrir el inventario
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (ids.isEmpty()) {
                            admin.sendMessage(ChatColor.RED + "No se encontraron registros de mochilas para " + target.getName());
                            return;
                        }

                        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Mochilas de: " + target.getName());

                        for (String id : ids) {
                            // Creamos una representación visual.
                            // Usamos una Mochila Nivel 5 (Morada) por defecto para asegurar que quepan los items al recuperar
                            ItemStack icon = EconomyItems.createPurpleMochila();

                            ItemMeta meta = icon.getItemMeta();
                            // Forzamos el nombre para identificarla visualmente
                            meta.setDisplayName(ChatColor.GOLD + "Mochila Registrada");

                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.DARK_GRAY + "ID: " + ChatColor.GRAY + id);
                            lore.add("");
                            lore.add(ChatColor.GREEN + "Click para obtener una COPIA");
                            lore.add(ChatColor.GRAY + "Recupera los items guardados en la DB.");

                            meta.setLore(lore);
                            // IMPORTANTE: Guardamos el ID en el CustomModelData o Lore para que el Listener funcione
                            // Pero tu listener actual lee el Lore "ID: ...", así que con el lore de arriba basta.
                            icon.setItemMeta(meta);

                            inv.addItem(icon);
                        }

                        admin.openInventory(inv);
                        admin.sendMessage(ChatColor.GREEN + "Mostrando " + ids.size() + " mochilas registradas en el historial.");
                    }
                }.runTask(functions.getPlugin());
            }
        }.runTaskAsynchronously(functions.getPlugin());
    }
}