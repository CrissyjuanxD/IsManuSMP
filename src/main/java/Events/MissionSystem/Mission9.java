package Events.MissionSystem;

import Handlers.ActionBarHandler;
import TitleListener.SuccessNotification;
import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission9 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission9(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "¡Qué frialdad!";
    }

    @Override
    public String getDescription() {
        return "Observa a un Iceologer con catalejo y elimínalo.";
    }

    @Override
    public int getMissionNumber() {
        return 9;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(6);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 5);
        ItemStack diamonds = new ItemStack(Material.NETHERITE_SCRAP, 10);


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
        data.set("players." + playerName + ".missions.9.spotted", false);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onSpyglassUse(PlayerInteractEvent event) {
        if (!missionHandler.isMissionActive(9)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.SPYGLASS) return;

        Player player = event.getPlayer();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        if (data.getBoolean("players." + player.getName() + ".missions.9.completed", false)) return;
        if (data.getBoolean("players." + player.getName() + ".missions.9.spotted", false)) return;

        // Raycast de 50 bloques
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 50,
                e -> e instanceof Evoker && e.getPersistentDataContainer().has(new NamespacedKey(plugin, "iceologer"), PersistentDataType.BYTE));

        if (result != null && result.getHitEntity() != null) {
            data.set("players." + player.getName() + ".missions.9.spotted", true);
            try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }

            String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡Iceologer avistado! Ahora elimínalo.";
            actionBarHandler.sendActionBar(player, msg);
            successNotification.showSuccess(player); // Feedback sonoro
        }
    }

    @EventHandler
    public void onIceologerDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(9)) return;
        Entity entity = event.getEntity();

        if (!(entity instanceof Evoker) || !entity.getPersistentDataContainer().has(new NamespacedKey(plugin, "iceologer"), PersistentDataType.BYTE)) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String name = killer.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (data.getBoolean("players." + name + ".missions.9.completed", false)) return;

        if (data.getBoolean("players." + name + ".missions.9.spotted", false)) {
            successNotification.showSuccess(killer);
            missionHandler.completeMission(name, 9);
        }
    }
}