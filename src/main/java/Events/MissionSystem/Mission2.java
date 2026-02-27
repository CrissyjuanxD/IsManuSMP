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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission2 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission2(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Armadura Básica";
    }

    @Override
    public String getDescription() {
        return "Equípate una armadura completa de diamante.";
    }

    @Override
    public int getMissionNumber() {
        return 2;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
        ItemStack diamonds = new ItemStack(Material.GOLDEN_APPLE, 3);


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
        String[] parts = {"helmet", "chestplate", "leggings", "boots"};
        for (String p : parts) data.set("players." + playerName + ".missions.2.armor." + p, false);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && missionHandler.isMissionActive(2)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> checkArmor(player), 1L);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (missionHandler.isMissionActive(2)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> checkArmor(event.getPlayer()), 1L);
        }
    }

    private void checkArmor(Player player) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        String name = player.getName();
        if (data.getBoolean("players." + name + ".missions.2.completed", false)) return;

        ItemStack h = player.getInventory().getHelmet();
        ItemStack c = player.getInventory().getChestplate();
        ItemStack l = player.getInventory().getLeggings();
        ItemStack b = player.getInventory().getBoots();

        boolean hasH = h != null && h.getType() == Material.DIAMOND_HELMET;
        boolean hasC = c != null && c.getType() == Material.DIAMOND_CHESTPLATE;
        boolean hasL = l != null && l.getType() == Material.DIAMOND_LEGGINGS;
        boolean hasB = b != null && b.getType() == Material.DIAMOND_BOOTS;

        boolean updated = false;
        if (hasH && !data.getBoolean("players." + name + ".missions.2.armor.helmet")) {
            data.set("players." + name + ".missions.2.armor.helmet", true); updated = true;
        }
        if (hasC && !data.getBoolean("players." + name + ".missions.2.armor.chestplate")) {
            data.set("players." + name + ".missions.2.armor.chestplate", true); updated = true;
        }
        if (hasL && !data.getBoolean("players." + name + ".missions.2.armor.leggings")) {
            data.set("players." + name + ".missions.2.armor.leggings", true); updated = true;
        }
        if (hasB && !data.getBoolean("players." + name + ".missions.2.armor.boots")) {
            data.set("players." + name + ".missions.2.armor.boots", true); updated = true;
        }

        if (updated) {
            try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }

            if (hasH && hasC && hasL && hasB) {
                successNotification.showSuccess(player);
                missionHandler.completeMission(name, 2);
            } else {
                int count = (hasH?1:0) + (hasC?1:0) + (hasL?1:0) + (hasB?1:0);
                String msg = ChatColor.GOLD + "۞ " +
                        ChatColor.of("#FFCC99") + "Prog. Armadura: " +
                        ChatColor.of("#FFA07A") + count +
                        ChatColor.of("#FFE4B5") + "/" +
                        ChatColor.of("#FFA07A") + "4";
                actionBarHandler.sendActionBar(player, msg);
            }
        }
    }
}