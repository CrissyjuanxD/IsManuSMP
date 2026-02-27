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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission3 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission3(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "El Héroe Dorado"; // Combinación de nombres sugerida
    }

    @Override
    public String getDescription() {
        return "Completa una Raid y fabrica 15 manzanas de oro.";
    }

    @Override
    public int getMissionNumber() {
        return 3;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.DIAMOND, 5);
        ItemStack diamonds = new ItemStack(Material.TOTEM_OF_UNDYING, 1);


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
    public void initializePlayerData(String playerName) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        data.set("players." + playerName + ".missions.3.raid_completed", false);
        data.set("players." + playerName + ".missions.3.apples_crafted", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    // OBJETIVO 1: RAID
    @EventHandler
    public void onRaidFinish(RaidFinishEvent event) {
        if (!missionHandler.isMissionActive(3)) return;
        if (event.getRaid().getStatus() != org.bukkit.Raid.RaidStatus.VICTORY) return;

        for (Player player : event.getWinners()) {
            updateProgress(player, "raid_completed", true);
        }
    }

    // OBJETIVO 2: CRAFTEO
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!missionHandler.isMissionActive(3)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (result.getType() != Material.GOLDEN_APPLE) return;

        int amount = result.getAmount();
        if (event.isShiftClick()) {
            int max = Integer.MAX_VALUE;
            for (ItemStack i : event.getInventory().getMatrix()) {
                if (i != null && i.getType() != Material.AIR) max = Math.min(max, i.getAmount());
            }
            if (max == Integer.MAX_VALUE) max = 0;
            amount = max * result.getAmount();
        }

        if (amount > 0) {
            updateProgress(player, "apples_crafted", amount);
        }
    }

    private void updateProgress(Player player, String type, Object value) {
        String name = player.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + name + ".missions.3.completed", false)) return;

        boolean updated = false;

        if (type.equals("raid_completed")) {
            if (!data.getBoolean("players." + name + ".missions.3.raid_completed", false)) {
                data.set("players." + name + ".missions.3.raid_completed", true);
                updated = true;
            }
        } else if (type.equals("apples_crafted")) {
            int current = data.getInt("players." + name + ".missions.3.apples_crafted", 0);
            int amount = (int) value;
            if (current < 15) {
                int newTotal = Math.min(15, current + amount); // Tope de 15 para estética
                data.set("players." + name + ".missions.3.apples_crafted", newTotal);
                updated = true;
            }
        }

        if (updated) {
            try {
                data.save(missionHandler.getMissionFile());

                boolean raidDone = data.getBoolean("players." + name + ".missions.3.raid_completed", false);
                int apples = data.getInt("players." + name + ".missions.3.apples_crafted", 0);

                if (raidDone && apples >= 15) {
                    successNotification.showSuccess(player);
                    missionHandler.completeMission(name, 3);
                } else {
                    // Action Bar con estado de ambos objetivos
                    String raidStatus = raidDone ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖";
                    String appleStatus = (apples >= 15 ? ChatColor.GREEN : ChatColor.of("#FFA07A")) + String.valueOf(apples) + "/15";

                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Raid: " + raidStatus +
                            ChatColor.GRAY + " | " +
                            ChatColor.of("#FFCC99") + "Manzanas: " + appleStatus;

                    actionBarHandler.sendActionBar(player, msg);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}