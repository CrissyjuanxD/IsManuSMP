package Handlers;

import Events.MissionSystem.MissionHandler;
import items.Misionesitem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class FirstJoinHandler implements Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler; // Necesario para rellenar misiones

    // Actualiza tu constructor en IsManuSMP para pasar missionHandler
    public FirstJoinHandler(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (!this.plugin.getConfig().getBoolean("HasJoinedBefore." + player.getUniqueId())) {
            this.plugin.getConfig().set("HasJoinedBefore." + player.getUniqueId(), true);
            this.plugin.saveConfig();

            // Asignar equipo
            Team survivorTeam = scoreboard.getTeam("ZMiembro");
            if (survivorTeam != null) {
                survivorTeam.addEntry(player.getName());
            }

            giveWelcomeKit(player);

            // --- RELLENAR MISIONES ACTIVAS ---
            // Esto soluciona que aparezcan "Sin misiones"
            if (missionHandler != null) {
                for (int activeMissionId : missionHandler.getActiveMissions()) {
                    missionHandler.initializePlayerMissionData(player.getName(), activeMissionId);
                }
                plugin.getLogger().info("Misiones activas inicializadas para nuevo jugador: " + player.getName());
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                player.sendMessage("§e۞ Has recibido tu kit de bienvenida!");
            }, 40);
        }
    }

    private void giveWelcomeKit(Player player) {
        ItemStack cookedBeef = new ItemStack(Material.COOKED_BEEF, 25);
        ItemStack misiones = Misionesitem.createMisiones();
        player.getInventory().addItem(cookedBeef);
        player.getInventory().addItem(misiones);
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), cookedBeef);
        }
    }
}