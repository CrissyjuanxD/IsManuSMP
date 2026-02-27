package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.*;

public class Mission25 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    private final Set<Material> requiredBlocks = new HashSet<>(Arrays.asList(
            Material.IRON_BLOCK,
            Material.GOLD_BLOCK,
            Material.EMERALD_BLOCK,
            Material.DIAMOND_BLOCK
    ));

    public Mission25(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Ve a tocar pasto";
    }

    @Override
    public String getDescription() {
        return "Rompe bloques de Hierro, Oro, Esmeralda y Diamante con Fatiga Minera III.";
    }

    @Override
    public int getMissionNumber() {
        return 25;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(8);
        ItemStack goldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 5);
        ItemStack diamonds = new ItemStack(Material.END_CRYSTAL, 30);


        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE, 3);
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
        for (Material block : requiredBlocks) {
            data.set("players." + playerName + ".missions.25.broken." + block.name(), false);
        }
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    // Método público para GUI
    public Set<Material> getRequiredBlocks() {
        return requiredBlocks;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!missionHandler.isMissionActive(25)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (requiredBlocks.contains(block.getType())) {
            // Verificar Mining Fatigue >= 3 (Amplifier 2)
            PotionEffect effect = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
            if (effect != null && effect.getAmplifier() >= 2) {

                String name = player.getName();
                FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
                if (data.getBoolean("players." + name + ".missions.25.completed", false)) return;

                String path = "players." + name + ".missions.25.broken." + block.getType().name();

                if (!data.getBoolean(path, false)) {
                    data.set(path, true);
                    try {
                        data.save(missionHandler.getMissionFile());

                        // Check completion
                        boolean all = true;
                        int count = 0;
                        for (Material m : requiredBlocks) {
                            if (data.getBoolean("players." + name + ".missions.25.broken." + m.name(), false)) {
                                count++;
                            } else {
                                all = false;
                            }
                        }

                        if (all) {
                            successNotification.showSuccess(player);
                            missionHandler.completeMission(name, 25);
                        } else {
                            String blockName = block.getType().name().toLowerCase().replace("_", " ");
                            // Capitalize
                            blockName = blockName.substring(0, 1).toUpperCase() + blockName.substring(1);

                            String msg = ChatColor.GOLD + "۞ " +
                                    ChatColor.of("#FFCC99") + "Roto: " + ChatColor.GREEN + blockName + " " +
                                    ChatColor.of("#FFA07A") + count +
                                    ChatColor.of("#FFE4B5") + "/" +
                                    ChatColor.of("#FFA07A") + "4";
                            actionBarHandler.sendActionBar(player, msg);
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }
        }
    }
}