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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Mission4 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission4(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "¡Sanguinario!";
    }

    @Override
    public String getDescription() {
        return "Elimina a otro jugador en combate.";
    }

    @Override
    public int getMissionNumber() {
        return 4;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(10);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 8);
        ItemStack diamonds = new ItemStack(Material.GOLDEN_CARROT, 20);


        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE,1);
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!missionHandler.isMissionActive(4)) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Verificar que haya un asesino y que sea un jugador
        if (killer == null) return;

        // Evitar que cuente si se mata a sí mismo (opcional)
        if (killer.equals(victim)) return;

        String killerName = killer.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (!data.getBoolean("players." + killerName + ".missions.4.completed", false)) {
            successNotification.showSuccess(killer);

            String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡Has eliminado a " + ChatColor.RED + victim.getName() + "!";
            actionBarHandler.sendActionBar(killer, msg);

            missionHandler.completeMission(killerName, 4);
        }
    }
}