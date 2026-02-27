package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
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

public class Mission19 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    private static final int REQUIRED_AMOUNT = 20;

    public Mission19(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Jugando a ser músico";
    }

    @Override
    public String getDescription() {
        return "Rompe 20 chilladores (Sculk Shriekers) en el bioma Deep Dark.";
    }

    @Override
    public int getMissionNumber() {
        return 19;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(8);
        ItemStack goldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 3);
        ItemStack diamonds = new ItemStack(Material.ENDER_PEARL, 16);


        ItemStack xpFill = new ItemStack(Material.ECHO_SHARD, 1);
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
        data.set("players." + playerName + ".missions.19.shriekers_broken", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!missionHandler.isMissionActive(19)) return;

        Block block = event.getBlock();

        // Verificar Material
        if (block.getType() != Material.SCULK_SHRIEKER) return;

        // Verificar Bioma (Deep Dark)
        if (block.getBiome() != Biome.DEEP_DARK) return;

        Player player = event.getPlayer();
        String name = player.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + name + ".missions.19.completed", false)) return;

        int broken = data.getInt("players." + name + ".missions.19.shriekers_broken", 0);

        if (broken < REQUIRED_AMOUNT) {
            broken++;
            data.set("players." + name + ".missions.19.shriekers_broken", broken);

            try {
                data.save(missionHandler.getMissionFile());

                event.setDropItems(false);

                if (broken >= REQUIRED_AMOUNT) {
                    successNotification.showSuccess(player);
                    missionHandler.completeMission(name, 19);
                } else {
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Chilladores: " +
                            ChatColor.of("#FFA07A") + broken +
                            ChatColor.of("#FFE4B5") + "/" +
                            ChatColor.of("#FFA07A") + REQUIRED_AMOUNT;
                    actionBarHandler.sendActionBar(player, msg);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}