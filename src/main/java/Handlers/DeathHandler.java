package Handlers;

import items.EconomyItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathHandler implements Listener {

    private final JavaPlugin plugin;
    // Mapa para registrar el cooldown de 30 minutos de cada víctima
    private final Map<UUID, Long> coinCooldowns = new HashMap<>();

    public DeathHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Verificamos si el PvP está activado en el mundo donde murió
        boolean pvpEnabled = victim.getWorld().getPVP();

        // CONDICIÓN: Si fue asesinado por un jugador y el PvP está activado
        if (killer != null && pvpEnabled) {

            // El jugador pierde el inventario y la experiencia (se dropea todo normalmente)
            event.setKeepInventory(false);
            event.setKeepLevel(false);

            // Verificación del cooldown de 30 minutos (30 min * 60 s * 1000 ms)
            long currentTime = System.currentTimeMillis();
            long cooldownTime = 30 * 60 * 1000L;
            UUID victimId = victim.getUniqueId();

            if (!coinCooldowns.containsKey(victimId) || (currentTime - coinCooldowns.get(victimId)) > cooldownTime) {

                // Generar 15 ManuCoins y añadirlas a los items que caen al suelo
                ItemStack manuCoins = EconomyItems.createVithiumCoin();
                manuCoins.setAmount(15);
                event.getDrops().add(manuCoins);

                // Mensaje al asesino en dorado pastel
                killer.sendMessage(ChatColor.of("#fcefa7") + "۞ Has matado al jugador " + victim.getName() + ", has obtenido +15 Manucoins");

                // Registrar el tiempo de muerte para el cooldown de esta víctima
                coinCooldowns.put(victimId, currentTime);
            }

        } else {
            // CONDICIÓN: Muerte normal (PvE, caídas, lava) o PvP Desactivado

            // Se queda con su inventario y experiencia
            event.setKeepInventory(true);
            event.setKeepLevel(true);

            // Limpiamos los drops para que no se dupliquen items
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
}