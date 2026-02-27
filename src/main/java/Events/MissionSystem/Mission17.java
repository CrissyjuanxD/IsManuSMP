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
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission17 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;

    public Mission17(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
    }

    @Override
    public String getName() {
        return "Jugando a ser Dios";
    }

    @Override
    public String getDescription() {
        return "Activa 5 Tótems de la Inmortalidad.";
    }

    @Override
    public int getMissionNumber() {
        return 17;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(7);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 20);
        ItemStack diamonds = new ItemStack(Material.TOTEM_OF_UNDYING, 2);


        ItemStack xpFill = new ItemStack(Material.GOLD_INGOT, 1);
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
        data.set("players." + playerName + ".missions.17.totems_popped", 0);
        try { data.save(missionHandler.getMissionFile()); } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void checkCompletion(String playerName) {}

    @EventHandler
    public void onTotemPop(EntityResurrectEvent event) {
        if (!missionHandler.isMissionActive(17)) return;
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        // Verificar que realmente usó un totem (aunque el evento suele ser suficiente)
        // Spigot lanza este evento justo antes de consumir el totem.

        String name = player.getName();
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        if (data.getBoolean("players." + name + ".missions.17.completed", false)) return;

        int popped = data.getInt("players." + name + ".missions.17.totems_popped", 0);

        if (popped < 5) {
            popped++;
            data.set("players." + name + ".missions.17.totems_popped", popped);

            try {
                data.save(missionHandler.getMissionFile());
                if (popped >= 5) {
                    successNotification.showSuccess(player);
                    missionHandler.completeMission(name, 17);
                } else {
                    String msg = ChatColor.GOLD + "۞ " +
                            ChatColor.of("#FFCC99") + "Tótems activados: " +
                            ChatColor.of("#FFA07A") + popped +
                            ChatColor.of("#FFE4B5") + "/" +
                            ChatColor.of("#FFA07A") + "5";
                    actionBarHandler.sendActionBar(player, msg);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}