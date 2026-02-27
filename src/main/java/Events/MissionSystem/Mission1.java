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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission1 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission1(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "¡Si hay que ser minero!";
    }

    @Override
    public String getDescription() {
        return "Mina 15 menas de diamante.";
    }

    @Override
    public int getMissionNumber() {
        return 1;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 3);
        ItemStack diamonds = new ItemStack(Material.DIAMOND, 5);


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
        data.set("players." + playerName + ".missions.1.diamonds_mined", 0);
        try {
            data.save(missionHandler.getMissionFile());
        } catch (IOException e) {
            plugin.getLogger().severe("Error init Misión 1: " + e.getMessage());
        }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!missionHandler.isMissionActive(1)) return;

        Material type = event.getBlock().getType();
        if (type != Material.DIAMOND_ORE && type != Material.DEEPSLATE_DIAMOND_ORE) return;

        String playerName = player.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + playerName + ".missions.1.completed", false)) return;

        int current = data.getInt("players." + playerName + ".missions.1.diamonds_mined", 0);
        int target = 15;

        if (current < target) {
            current++;
            data.set("players." + playerName + ".missions.1.diamonds_mined", current);

            try {
                data.save(missionHandler.getMissionFile());

                if (current >= target) {
                    successNotification.showSuccess(player);
                    missionHandler.completeMission(playerName, 1);
                } else {
                    // Action Bar Pastel
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Diamantes: " +
                            ChatColor.of("#FFA07A") + current +
                            ChatColor.of("#FFE4B5") + "/" +
                            ChatColor.of("#FFA07A") + target;
                    actionBarHandler.sendActionBar(player, msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}