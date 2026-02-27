package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission15 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission15(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Estado en Descomposición";
    }

    @Override
    public String getDescription() {
        return "Derrota a 3 Withers.";
    }

    @Override
    public int getMissionNumber() {
        return 15;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(10);
        ItemStack goldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4);
        ItemStack diamonds = new ItemStack(Material.WITHER_SKELETON_SKULL, 6);


        ItemStack xpFill = new ItemStack(Material.WITHER_ROSE, 1);
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
    public void initializePlayerData(String playerName) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        data.set("players." + playerName + ".missions.15.withers_killed", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(15)) return;
        if (event.getEntityType() != EntityType.WITHER) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String name = killer.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        if (data.getBoolean("players." + name + ".missions.15.completed", false)) return;

        int killed = data.getInt("players." + name + ".missions.15.withers_killed", 0);

        if (killed < 3) {
            killed++;
            data.set("players." + name + ".missions.15.withers_killed", killed);
            try {
                data.save(missionHandler.getMissionFile());

                if (killed >= 3) {
                    successNotification.showSuccess(killer);
                    missionHandler.completeMission(name, 15);
                } else {
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Withers: " +
                            ChatColor.of("#FFA07A") + killed +
                            ChatColor.of("#FFE4B5") + "/" +
                            ChatColor.of("#FFA07A") + "3";
                    actionBarHandler.sendActionBar(killer, msg);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}