package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Mission14 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    // Mapas temporales (No se guardan en BD porque es un reto instantáneo)
    private final Map<UUID, Double> startY = new HashMap<>();
    private final Map<UUID, Long> startTime = new HashMap<>();

    public Mission14(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "¡Sé que puedo volar!";
    }

    @Override
    public String getDescription() {
        return "Sube 300 bloques de altura en menos de 7 segundos.";
    }

    @Override
    public int getMissionNumber() {
        return 14;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.GOLD_INGOT, 25);
        ItemStack diamonds = new ItemStack(Material.FIREWORK_ROCKET, 64);


        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE, 3);
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
        if (!missionHandler.isMissionActive(14)) return;

        // Optimización: Solo revisar si cambió de bloque Y
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;

        Player player = event.getPlayer();
        if (player.isGliding() || player.isRiptiding() || player.getLocation().getY() > 350) {
            // Solo procesar si está volando, usando tridente o muy alto para ahorrar recursos
            processFlight(player, event.getFrom().getY(), event.getTo().getY());
        } else {
            // Si camina o cae, limpiar datos
            if (event.getTo().getY() < event.getFrom().getY()) {
                startY.remove(player.getUniqueId());
                startTime.remove(player.getUniqueId());
            }
        }
    }

    private void processFlight(Player player, double fromY, double toY) {
        UUID id = player.getUniqueId();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        if (data.getBoolean("players." + player.getName() + ".missions.14.completed", false)) return;

        // Si bajó, resetear
        if (toY < fromY) {
            startY.remove(id);
            startTime.remove(id);
            return;
        }

        // Iniciar rastreo
        if (!startY.containsKey(id)) {
            startY.put(id, fromY);
            startTime.put(id, System.currentTimeMillis());
            return;
        }

        long timeElapsed = System.currentTimeMillis() - startTime.get(id);

        // Si pasaron más de 7 segundos, reiniciar punto de partida al actual
        if (timeElapsed > 7000) {
            startY.put(id, fromY); // Reiniciar desde aquí
            startTime.put(id, System.currentTimeMillis());
            return;
        }

        double heightGained = toY - startY.get(id);

        if (heightGained >= 300) {
            successNotification.showSuccess(player);
            String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡Velocidad supersónica alcanzada!";
            actionBarHandler.sendActionBar(player, msg);
            missionHandler.completeMission(player.getName(), 14);
            startY.remove(id);
            startTime.remove(id);
        }
    }
}