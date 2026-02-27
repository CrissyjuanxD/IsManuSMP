package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Mission10 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission10(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Cazador de Abejas";
    }

    @Override
    public String getDescription() {
        return "Elimina a una Abeja Reina.\nUsa /bosstp para ir a su Dungeon.\nEl altar estará en una caída,\ninteractúa con el panal.";
    }

    @Override
    public int getMissionNumber() {
        return 10;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(10);
        ItemStack goldenApples = new ItemStack(Material.GOLD_BLOCK, 5);
        ItemStack diamonds = new ItemStack(Material.DIAMOND_BLOCK, 5);


        ItemStack xpFill = new ItemStack(Material.HONEY_BOTTLE, 1);
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
    public void onEntityDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(10)) return;
        Entity entity = event.getEntity();

        // Detección por nombre custom (QueenBeeHandler pone "Abeja Reina")
        if (entity instanceof Bee && entity.getCustomName() != null && entity.getCustomName().contains("Abeja Reina")) {
            Player killer = event.getEntity().getKiller();
            if (killer == null) return;

            String name = killer.getName();
            FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

            if (!data.getBoolean("players." + name + ".missions.10.completed", false)) {
                successNotification.showSuccess(killer);
                String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡La Reina ha caído!";
                actionBarHandler.sendActionBar(killer, msg);
                missionHandler.completeMission(name, 10);
            }
        }
    }
}