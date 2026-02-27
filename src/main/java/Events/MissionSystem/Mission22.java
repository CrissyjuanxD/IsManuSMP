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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Mission22 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission22(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Con su propia medicina";
    }

    @Override
    public String getDescription() {
        return "Mata a un Piglin Brute usando un Hacha de Oro y llevando al menos una pieza de oro.";
    }

    @Override
    public int getMissionNumber() {
        return 22;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 20);
        ItemStack diamonds = new ItemStack(Material.NETHERITE_INGOT, 2);


        ItemStack xpFill = new ItemStack(Material.EXPERIENCE_BOTTLE, 2);
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
    public void initializePlayerData(String playerName) {}

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(22)) return;
        if (event.getEntityType() != EntityType.PIGLIN_BRUTE) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        if (data.getBoolean("players." + killer.getName() + ".missions.22.completed", false)) return;

        // Verificar Hacha de Oro en mano
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon.getType() != Material.GOLDEN_AXE) return;

        // Verificar Armadura de Oro
        if (hasGoldenArmor(killer)) {
            successNotification.showSuccess(killer);
            String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡Justicia dorada!";
            actionBarHandler.sendActionBar(killer, msg);
            missionHandler.completeMission(killer.getName(), 22);
        }
    }

    private boolean hasGoldenArmor(Player player) {
        EntityEquipment eq = player.getEquipment();
        if (eq == null) return false;

        ItemStack[] armor = eq.getArmorContents();
        for (ItemStack item : armor) {
            if (item != null && item.getType().name().contains("GOLDEN_")) {
                return true;
            }
        }
        return false;
    }
}