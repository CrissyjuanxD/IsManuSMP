package Handlers;

import Events.MissionSystem.MissionHandler;
import Handlers.Teams.TeamType;
import items.EconomyItems;
import items.Misionesitem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FirstJoinHandler implements Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final DatabaseManager databaseManager;
    private final TeamsHandler teamsHandler;

    public FirstJoinHandler(JavaPlugin plugin, MissionHandler missionHandler, DatabaseManager databaseManager, TeamsHandler teamsHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.databaseManager = databaseManager;
        this.teamsHandler = teamsHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!databaseManager.hasJoinedBefore(player.getUniqueId())) {
            databaseManager.registerPlayer(player.getUniqueId(), player.getName(), TeamType.Z_MIEMBRO.getId());
            teamsHandler.addPlayerToTeam(player, TeamType.Z_MIEMBRO);

            handleWelcomeLogistics(player);
        }
    }

    private void handleWelcomeLogistics(Player player) {
        ItemStack cookedBeef = new ItemStack(Material.COOKED_BEEF, 25);
        ItemStack misiones = Misionesitem.createMisiones();
        ItemStack mochila = EconomyItems.createNormalMochila();
        player.getInventory().addItem(cookedBeef);
        player.getInventory().addItem(misiones);
        player.getInventory().addItem(mochila);
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), cookedBeef);
        }
    }
}