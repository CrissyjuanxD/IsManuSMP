package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import items.excavatorItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mission1 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;
    private final excavatorItem ExcavatorItem;

    public Mission1(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
        this.ExcavatorItem = new excavatorItem(plugin);
    }

    @Override
    public String getName() {
        return "¡Si hay que ser minero!";
    }

    @Override
    public String getDescription() {
        return "Recolecta 5 de\nlos 19 minerales\ndel juego.";
    }

    @Override
    public int getMissionNumber() {
        return 1;
    }

    // Lista de los 19 minerales del juego
    public List<Material> getRequiredOres() {
        return Arrays.asList(
                Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
                Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
                Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
                Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
                Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
                Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
                Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
                Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE,
                Material.ANCIENT_DEBRIS
        );
    }

    // Ahora todos los minerales piden 5
    private int getTargetAmount(Material oreType) {
        return 5;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(20);
        ItemStack excavator = new ItemStack(ExcavatorItem.createExcavator());
        ItemStack en_goldenapple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 5);

        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        for (int i = 0; i < 27; i++) {
            if (i == 11) {
                rewards.add(excavator);
            } else if (i == 13) {
                rewards.add(coins);
            } else if (i == 15) {
                rewards.add(en_goldenapple);
            } else {
                rewards.add(xpFill.clone());
            }
        }

        return rewards;
    }

    @Override
    public void initializePlayerData(String playerName) {
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        MissionData data = missionHandler.getData(player, 1);

        if (!data.isActive() || data.isCompleted()) return;

        Material type = event.getBlock().getType();

        if (!getRequiredOres().contains(type)) return;

        int current = data.getProgressInt("ore_" + type.name());
        int target = getTargetAmount(type);

        if (current < target) {
            // Ya no cancelamos el drop, simplemente sumamos el progreso.
            current++;
            data.setProgressValue("ore_" + type.name(), current);

            boolean allCompleted = true;
            for (Material ore : getRequiredOres()) {
                if (data.getProgressInt("ore_" + ore.name()) < getTargetAmount(ore)) {
                    allCompleted = false;
                    break;
                }
            }

            if (allCompleted) {
                successNotification.showSuccess(player);
                missionHandler.saveData(player, 1, data);
                missionHandler.completeMission(player, 1);
            } else {
                missionHandler.saveData(player, 1, data);

                String oreName = type.name().toLowerCase().replace("deepslate_", "deep. ").replace("_ore", "").replace("_", " ");
                oreName = oreName.substring(0, 1).toUpperCase() + oreName.substring(1);

                String msg = ChatColor.GOLD + "۞ " +
                        ChatColor.of("#FFCC99") + oreName + ": " +
                        ChatColor.of("#FFA07A") + current +
                        ChatColor.of("#FFE4B5") + "/" +
                        ChatColor.of("#FFA07A") + target;
                actionBarHandler.sendActionBar(player, msg);
            }
        }
    }
}