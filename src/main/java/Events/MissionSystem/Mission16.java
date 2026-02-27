package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission16 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission16(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Cazador de Corruptos 2";
    }

    @Override
    public String getDescription() {
        return "Elimina a 20 Guardian Corrupted Skeletons.";
    }

    @Override
    public int getMissionNumber() {
        return 16;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(7);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 5);
        ItemStack diamonds = new ItemStack(Material.NETHERITE_SCRAP, 10);


        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE, 2);
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
        data.set("players." + playerName + ".missions.16.skeletons_killed", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(16)) return;
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // Detección usando la Key definida en tu clase GuardianCorruptedSkeleton
        NamespacedKey key = new NamespacedKey(plugin, "guardian_corrupted_skeleton");

        if (entity instanceof WitherSkeleton && entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {

            String name = killer.getName();
            FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
            if (data.getBoolean("players." + name + ".missions.16.completed", false)) return;

            int killed = data.getInt("players." + name + ".missions.16.skeletons_killed", 0);

            if (killed < 20) {
                killed++;
                data.set("players." + name + ".missions.16.skeletons_killed", killed);

                try {
                    data.save(missionHandler.getMissionFile());
                    if (killed >= 20) {
                        successNotification.showSuccess(killer);
                        missionHandler.completeMission(name, 16);
                    } else {
                        String msg = ChatColor.GOLD + "۞ " +
                                ChatColor.of("#FFCC99") + "Guardian Skeleton: " +
                                ChatColor.of("#FFA07A") + killed +
                                ChatColor.of("#FFE4B5") + "/" +
                                ChatColor.of("#FFA07A") + "20";
                        actionBarHandler.sendActionBar(killer, msg);
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }
}