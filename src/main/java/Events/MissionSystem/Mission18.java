package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
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

public class Mission18 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission18(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Vida Opaca";
    }

    @Override
    public String getDescription() {
        return "Rompe 10 Creaking Hearts en un Pale Garden.";
    }

    @Override
    public int getMissionNumber() {
        return 18;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();
        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(12);
        rewards.add(coins);
        rewards.add(new ItemStack(Material.PALE_OAK_LOG, 32));
        return rewards;
    }

    @Override
    public void initializePlayerData(String playerName) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        data.set("players." + playerName + ".missions.18.hearts_broken", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!missionHandler.isMissionActive(18)) return;

        // Material CREAKING_HEART (Disponible en 1.21.4)
        if (event.getBlock().getType() != Material.CREAKING_HEART) return;

        // Bioma PALE_GARDEN (Disponible en 1.21.4)
        if (event.getBlock().getBiome() != Biome.PALE_GARDEN) return;

        Player player = event.getPlayer();
        String name = player.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + name + ".missions.18.completed", false)) return;

        int broken = data.getInt("players." + name + ".missions.18.hearts_broken", 0);

        if (broken < 10) {
            broken++;
            data.set("players." + name + ".missions.18.hearts_broken", broken);

            try {
                data.save(missionHandler.getMissionFile());
                if (broken >= 10) {
                    successNotification.showSuccess(player);
                    missionHandler.completeMission(name, 18);
                } else {
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Creaking Hearts: " +
                            ChatColor.of("#FFA07A") + broken +
                            ChatColor.of("#FFE4B5") + "/" +
                            ChatColor.of("#FFA07A") + "10";
                    actionBarHandler.sendActionBar(player, msg);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}