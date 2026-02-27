package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Mission7 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    // Guardamos la altura Y estable (cuando estaba en el suelo)
    private final Map<UUID, Double> lastGroundY = new HashMap<>();

    public Mission7(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Salto de fe ardiente";
    }

    @Override
    public String getDescription() {
        return "Cae 115 bloques de altura en el Nether y sobrevive el impacto.";
    }

    @Override
    public int getMissionNumber() {
        return 7;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 3);
        ItemStack diamonds = new ItemStack(Material.DIAMOND_BLOCK, 3);


        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        for (int i = 0; i < 27; i++) {
            if (i == 11) {
                rewards.add(goldenApples);
            } else if (i == 13) {
                rewards.add(coins);
            } else if (i == 15) {
                rewards.add(diamonds);
            } else {
                rewards.add(xpFill.clone());
            }
        }

        return rewards;
    }

    @Override
    public void initializePlayerData(String playerName) {}

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!missionHandler.isMissionActive(7)) return;

        Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() != World.Environment.NETHER) return;

        // Optimización: Solo calcular si cambió de bloque Y
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;

        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        if (data.getBoolean("players." + player.getName() + ".missions.7.completed", false)) return;

        UUID id = player.getUniqueId();
        double currentY = event.getTo().getY();

        // Lógica de caída
        // 1. Si está en el suelo (o escaleras/enredaderas), actualizamos su "Punto de partida"
        //    isOnGround a veces falla en el aire, así que usamos una comprobación básica.
        if (player.isOnGround() || player.getLocation().getBlock().getType() != Material.AIR) {
            lastGroundY.put(id, currentY);
            return;
        }

        // 2. Si está en el aire y tenemos un punto de partida
        if (lastGroundY.containsKey(id)) {
            double startY = lastGroundY.get(id);

            // Si está subiendo (elytras/cohetes), reseteamos la altura de caída al punto actual
            if (currentY > event.getFrom().getY()) {
                lastGroundY.put(id, currentY);
                return;
            }

            // Calcular distancia caída
            double distanceFallen = startY - currentY;

            if (distanceFallen >= 115) {

                successNotification.showSuccess(player);
                String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡Salto Infernal: " + (int)distanceFallen + "m!";
                actionBarHandler.sendActionBar(player, msg);

                missionHandler.completeMission(player.getName(), 7);
                lastGroundY.remove(id);
            }
        }
    }
}