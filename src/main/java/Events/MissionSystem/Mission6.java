package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission6 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission6(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Cazador de Corruptos";
    }

    @Override
    public String getDescription() {
        return "Mata 30 Zombies Corruptos y 30 Arañas Corruptas";
    }

    @Override
    public int getMissionNumber() {
        return 6;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(6);
        ItemStack goldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2);
        ItemStack diamonds = new ItemStack(Material.GOLD_BLOCK, 3);


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
        data.set("players." + playerName + ".missions.6.zombies_killed", 0);
        data.set("players." + playerName + ".missions.6.spiders_killed", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(6)) return;

        Entity entity = event.getEntity();
        Player killer = ((LivingEntity) entity).getKiller();
        if (killer == null) return;

        boolean isZ = entity instanceof Zombie && entity.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "corrupted_zombie"), PersistentDataType.BYTE);
        boolean isS = entity instanceof Spider && entity.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "corruptedspider"), PersistentDataType.BYTE);

        if (!isZ && !isS) return;

        String playerName = killer.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + playerName + ".missions.6.completed", false)) return;

        boolean updated = false;
        int zKilled = data.getInt("players." + playerName + ".missions.6.zombies_killed", 0);
        int sKilled = data.getInt("players." + playerName + ".missions.6.spiders_killed", 0);

        if (isZ && zKilled < 30) {
            zKilled++;
            data.set("players." + playerName + ".missions.6.zombies_killed", zKilled);
            updated = true;
        } else if (isS && sKilled < 30) {
            sKilled++;
            data.set("players." + playerName + ".missions.6.spiders_killed", sKilled);
            updated = true;
        }

        if (updated) {
            try {
                data.save(missionHandler.getMissionFile());

                if (zKilled >= 30 && sKilled >= 30) {
                    successNotification.showSuccess(killer);
                    missionHandler.completeMission(playerName, 6);
                } else {
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Zombies: " + ChatColor.of("#FFA07A") + zKilled + ChatColor.of("#FFE4B5") + "/" + ChatColor.of("#FFA07A") + "30" +
                            ChatColor.GRAY + " | " +
                            ChatColor.of("#FFCC99") + "Arañas: " + ChatColor.of("#FFA07A") + sKilled + ChatColor.of("#FFE4B5") + "/" + ChatColor.of("#FFA07A") + "30";
                    actionBarHandler.sendActionBar(killer, msg);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}