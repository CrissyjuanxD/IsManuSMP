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
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mission11 implements Mission, Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;
    private final SuccessNotification successNotification;
    private final ActionBarHandler actionBarHandler;
    private final NamespacedKey friendKey;

    public Mission11(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;
        this.successNotification = new SuccessNotification(plugin);
        this.actionBarHandler = new ActionBarHandler(plugin);
        this.friendKey = new NamespacedKey(plugin, "mission_friend_snowman");
    }

    @Override
    public String getName() {
        return "Los mejores amigos";
    }

    @Override
    public String getDescription() {
        return "Crea un Snow Golem en Warped Forest, quítale la calabaza y espera a que muera.";
    }

    @Override
    public int getMissionNumber() {
        return 11;
    }

    @Override
    public List<ItemStack> getRewards() {
        List<ItemStack> rewards = new ArrayList<>();

        ItemStack coins = EconomyItems.createVithiumCoin();
        coins.setAmount(5);
        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 5);
        ItemStack diamonds = new ItemStack(Material.PUMPKIN_PIE, 64);


        ItemStack xpFill = new ItemStack(Material.SNOW_BLOCK, 1);
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

    // --- NUEVO: Protección temporal para que no muera al instate ---
    @EventHandler
    public void onEnvironmentDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Snowman snowman)) return;

        // Solo proteger en Warped Forest
        if (snowman.getLocation().getBlock().getBiome() != Biome.WARPED_FOREST) return;

        // isDerp() devuelve true si NO tiene calabaza.
        // Queremos protegerlo MIENTRAS TIENE CALABAZA (false).
        if (!snowman.isDerp()) {
            if (event.getCause() == EntityDamageEvent.DamageCause.MELTING ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
            }
        }
    }

    // Paso 1: Interactuar con tijeras
    @EventHandler
    public void onShearSnowman(PlayerInteractEntityEvent event) {
        if (!missionHandler.isMissionActive(11)) return;
        if (!(event.getRightClicked() instanceof Snowman snowman)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.SHEARS) return;

        if (player.getWorld().getBiome(player.getLocation()) != Biome.WARPED_FOREST) return;

        // Si ya fue esquilado, no hacer nada
        if (snowman.isDerp()) return;

        // Marcar este golem como "amigo"
        snowman.getPersistentDataContainer().set(friendKey, PersistentDataType.STRING, player.getName());

        String msg = ChatColor.GOLD + "۞ " + ChatColor.of("#FFCC99") + "¡Adiós calabaza! Ahora espera su triste final...";
        actionBarHandler.sendActionBar(player, msg);

        // Al cortarle la calabaza, snowman.isDerp() pasará a true automáticamente,
        // por lo que el evento de onEnvironmentDamage dejará de protegerlo y morirá solo.
    }

    // Paso 2: Detectar muerte
    @EventHandler
    public void onSnowmanDeath(EntityDeathEvent event) {
        if (!missionHandler.isMissionActive(11)) return;
        if (!(event.getEntity() instanceof Snowman snowman)) return;

        if (!snowman.getPersistentDataContainer().has(friendKey, PersistentDataType.STRING)) return;

        EntityDamageEvent damageEvent = snowman.getLastDamageCause();
        if (damageEvent == null) return;

        if (damageEvent.getCause() == EntityDamageEvent.DamageCause.MELTING ||
                damageEvent.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                damageEvent.getCause() == EntityDamageEvent.DamageCause.FIRE) {

            String playerName = snowman.getPersistentDataContainer().get(friendKey, PersistentDataType.STRING);
            Player player = plugin.getServer().getPlayer(playerName);

            if (player != null && player.isOnline()) {
                FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
                if (!data.getBoolean("players." + playerName + ".missions.11.completed", false)) {
                    successNotification.showSuccess(player);
                    missionHandler.completeMission(playerName, 11);
                }
            }
        }
    }
}