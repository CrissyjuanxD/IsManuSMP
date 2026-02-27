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
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission8 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission8(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Nervios de Acero";
    }

    @Override
    public String getDescription() {
        return "Golpea a un Warden con un proyectil y elimínalo.";
    }

    @Override
    public int getMissionNumber() {
        return 8;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(7);
        ItemStack goldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 3);
        ItemStack diamonds = new ItemStack(Material.EMERALD_BLOCK, 10);


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
        data.set("players." + playerName + ".missions.8.hit_projectile", false);
        data.set("players." + playerName + ".missions.8.killed_warden", false);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    // Objetivo 1: Golpear con proyectil
    @EventHandler
    public void onHitWarden(EntityDamageByEntityEvent event) {
        if (!missionHandler.isMissionActive(8)) return;
        if (event.getEntityType() != EntityType.WARDEN) return;

        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            updateProgress(player, "hit_projectile");
        }
    }

    // Objetivo 2: Matar al Warden
    @EventHandler
    public void onWardenDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(8)) return;
        if (event.getEntityType() != EntityType.WARDEN) return;

        if (event.getEntity().getKiller() != null) {
            updateProgress(event.getEntity().getKiller(), "killed_warden");
        }
    }

    private void updateProgress(Player player, String objectiveKey) {
        String name = player.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + name + ".missions.8.completed", false)) return;
        if (data.getBoolean("players." + name + ".missions.8." + objectiveKey, false)) return; // Ya cumplido

        data.set("players." + name + ".missions.8." + objectiveKey, true);

        try {
            data.save(missionHandler.getMissionFile());

            boolean hit = data.getBoolean("players." + name + ".missions.8.hit_projectile", false);
            boolean killed = data.getBoolean("players." + name + ".missions.8.killed_warden", false);

            if (hit && killed) {
                successNotification.showSuccess(player);
                missionHandler.completeMission(name, 8);
            } else {
                // Mensaje de progreso
                String hitStatus = hit ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖";
                String killStatus = killed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖";

                String msg = ChatColor.GOLD + "۞ " +
                        ChatColor.of("#FFCC99") + "Warden: " +
                        ChatColor.GRAY + "Hit " + hitStatus +
                        ChatColor.of("#FFE4B5") + " | " +
                        ChatColor.GRAY + "Kill " + killStatus;

                actionBarHandler.sendActionBar(player, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}