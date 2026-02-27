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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission5 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission5(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Aracnofobia Infernal";
    }

    @Override
    public String getDescription() {
        return "Mata 30 Corrupted Infernal Spiders.";
    }

    @Override
    public int getMissionNumber() {
        return 5;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(6);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 10);
        ItemStack diamonds = new ItemStack(Material.NETHERITE_SCRAP, 6);


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
    public void initializePlayerData(String playerName) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        data.set("players." + playerName + ".missions.5.infernal_spiders_killed", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(5)) return;

        Entity entity = event.getEntity();
        Player killer = ((LivingEntity) entity).getKiller();
        if (killer == null) return;

        // Detección exacta usando la Key de tu clase CorruptedInfernalSpider
        NamespacedKey key = new NamespacedKey(plugin, "corruptedinfernalspider");
        boolean isInfernal = entity instanceof Spider && entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE);

        if (!isInfernal) return;

        String playerName = killer.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + playerName + ".missions.5.completed", false)) return;

        int killed = data.getInt("players." + playerName + ".missions.5.infernal_spiders_killed", 0);
        int target = 30;

        if (killed < target) {
            killed++;
            data.set("players." + playerName + ".missions.5.infernal_spiders_killed", killed);

            try {
                data.save(missionHandler.getMissionFile());

                if (killed >= target) {
                    successNotification.showSuccess(killer);
                    missionHandler.completeMission(playerName, 5);
                } else {
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Arañas Inf: " +
                            ChatColor.of("#FFA07A") + killed +
                            ChatColor.of("#FFE4B5") + "/" +
                            ChatColor.of("#FFA07A") + target;
                    actionBarHandler.sendActionBar(killer, msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}