package Events.MissionSystem;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MissionGUI implements Listener {
    private final JavaPlugin plugin;
    private final MissionHandler missionHandler;

    private final String guiTitle;

    // Paneles para el borde
    private final ItemStack yellowPane;
    private final ItemStack orangePane;

    public MissionGUI(JavaPlugin plugin, MissionHandler missionHandler) {
        this.plugin = plugin;
        this.missionHandler = missionHandler;

        this.guiTitle = ChatColor.of("#FFA500") + "Misiones";

        // Inicializar paneles
        this.yellowPane = createPane(Material.YELLOW_STAINED_GLASS_PANE);
        this.orangePane = createPane(Material.ORANGE_STAINED_GLASS_PANE);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private ItemStack createPane(Material mat) {
        ItemStack pane = new ItemStack(mat);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        // Solo mano principal para evitar doble ejecución
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Verificar si es click derecho (aire o bloque)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();

        // Verificaciones del item: Que no sea null, que sea Filled Map y tenga el ModelData 9999
        if (item == null || item.getType() != Material.FILLED_MAP) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return;

        if (item.getItemMeta().getCustomModelData() == 9999) {
            event.setCancelled(true); // Evitar que se use el mapa o escudo
            openMissionGUI(event.getPlayer());
        }
    }

    public void openMissionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, guiTitle);


        // Índices para el patrón
        ItemStack[] pattern = {yellowPane, orangePane, orangePane, yellowPane, orangePane, yellowPane, orangePane, orangePane, yellowPane};

        // Fila Superior (0-8)
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, pattern[i]);
        }

        // Fila Inferior (36-44)
        for (int i = 0; i < 9; i++) {
            gui.setItem(36 + i, pattern[i]);
        }

        // --- CONTENIDO DE MISIONES ---
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());
        String playerName = player.getName();
        Map<Integer, Mission> allMissions = missionHandler.getMissions();

        // Misiones van desde el slot 9 hasta el 35 (centro de 3 filas)
        // Se asume que hay un máximo de 27 misiones (slots 9 al 35 son 27 espacios)

        for (int missionNum = 1; missionNum <= 27; missionNum++) {
            int slot = 8 + missionNum; // Misión 1 en slot 9

            if (slot > 35) break; // Límite de la zona central

            if (allMissions.containsKey(missionNum)) {
                Mission mission = allMissions.get(missionNum);
                boolean isActive = missionHandler.isMissionActive(missionNum);
                boolean isCompleted = data.getBoolean("players." + playerName + ".missions." + missionNum + ".completed", false);

                gui.setItem(slot, createMissionItem(mission, isActive, isCompleted, playerName));
            } else {
                // Slot vacío o item de "Futuro"
                gui.setItem(slot, new ItemStack(Material.AIR));
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createMissionItem(Mission mission, boolean isActive, boolean isCompleted, String playerName) {
        ItemStack item;

        // Selección de Material según estado
        if (!isActive) {
            item = new ItemStack(Material.FILLED_MAP); // Sin descubrir
        } else if (isCompleted) {
            item = new ItemStack(Material.LIME_DYE); // Completada
        } else {
            item = new ItemStack(Material.FIREWORK_STAR); // Activa / Pendiente
        }

        ItemMeta meta = item.getItemMeta();
        String displayName;

        if (!isActive) {
            displayName = ChatColor.GRAY + "???";
        } else if (isCompleted) {
            displayName = ChatColor.GREEN + mission.getName();
        } else {
            displayName = ChatColor.of("#FFA500") + mission.getName(); // Naranja
        }

        meta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>();

        if (isActive) {
            String[] descriptionLines = mission.getDescription().split("\n");
            for (String line : descriptionLines) {
                lore.add(ChatColor.GRAY + line);
            }

            lore.add(""); // Espacio vacío
            lore.add(isCompleted ? ChatColor.GREEN + "✔ Completada" : ChatColor.RED + "✖ Pendiente");

            addMissionSpecificProgress(mission, playerName, lore);
        } else {
            lore.add(ChatColor.GRAY + "Misión no descubierta");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addMissionSpecificProgress(Mission mission, String playerName, List<String> lore) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionHandler.getMissionFile());

        if (mission instanceof Mission1) {
            // Mision 1: Minar Diamantes
            lore.add("");
            lore.add(ChatColor.of("#FFCC99") + "Progreso:");
            int mined = data.getInt("players." + playerName + ".missions.1.diamonds_mined", 0);
            lore.add(ChatColor.GRAY + "- Diamantes: " + (mined >= 15 ? ChatColor.GREEN : ChatColor.YELLOW) + mined + "/15");
        } else if (mission instanceof Mission2) {
            // Mision 2: Armadura Diamante (Antes Mision 1)
            lore.add("");
            lore.add(ChatColor.of("#FFCC99") + "Piezas:");
            String[] parts = {"helmet", "chestplate", "leggings", "boots"};
            String[] names = {"Casco", "Peto", "Pantalones", "Botas"};
            for (int i = 0; i < parts.length; i++) {
                boolean has = data.getBoolean("players." + playerName + ".missions.2.armor." + parts[i], false);
                lore.add((has ? ChatColor.GREEN : ChatColor.GRAY) + "- " + names[i]);
            }
        } else if (mission instanceof Mission3) {
            // Mision 3: Héroe Dorado
            lore.add("");
            boolean raid = data.getBoolean("players." + playerName + ".missions.3.raid_completed", false);
            int apples = data.getInt("players." + playerName + ".missions.3.apples_crafted", 0);

            lore.add(ChatColor.GRAY + "- Raid: " + (raid ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
            lore.add(ChatColor.GRAY + "- Manzanas: " + (apples >= 15 ? ChatColor.GREEN : ChatColor.YELLOW) + apples + "/15");
        } else if (mission instanceof Mission4) {
            // Mision 4: Sanguinario
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.4.completed", false);
            lore.add(ChatColor.GRAY + "- Kill: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission5) {
            // Mision 5: Arañas Infernales
            lore.add("");
            lore.add(ChatColor.of("#FFCC99") + "Progreso:");
            int killed = data.getInt("players." + playerName + ".missions.5.infernal_spiders_killed", 0);
            lore.add(ChatColor.GRAY + "- Infernal Spiders: " + (killed >= 30 ? ChatColor.GREEN : ChatColor.YELLOW) + killed + "/30");
        } else if (mission instanceof Mission6) {
            // Mision 6: Corruptos
            lore.add("");
            lore.add(ChatColor.of("#FFCC99") + "Progreso:");
            int z = data.getInt("players." + playerName + ".missions.6.zombies_killed", 0);
            int s = data.getInt("players." + playerName + ".missions.6.spiders_killed", 0);
            lore.add(ChatColor.GRAY + "- Zombies Corr.: " + (z >= 30 ? ChatColor.GREEN : ChatColor.YELLOW) + z + "/30");
            lore.add(ChatColor.GRAY + "- Arañas Corr.: " + (s >= 30 ? ChatColor.GREEN : ChatColor.YELLOW) + s + "/30");
        } else if (mission instanceof Mission7) {
            // Mision 7: Salto Nether
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.7.completed", false);
            lore.add(ChatColor.GRAY + "- Salto: " + (completed ? ChatColor.GREEN + "Completado" : ChatColor.RED + "Pendiente"));
        } else if (mission instanceof Mission8) {
            // Mision 8: Warden
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.8.completed", false);
            lore.add(ChatColor.GRAY + "- Desafío Warden: " + (completed ? ChatColor.GREEN + "Completado" : ChatColor.RED + "Pendiente"));
        } else if (mission instanceof Mission9) {
            // Mision 9: Iceologer
            lore.add("");
            boolean spotted = data.getBoolean("players." + playerName + ".missions.9.spotted", false);
            boolean completed = data.getBoolean("players." + playerName + ".missions.9.completed", false);
            lore.add(ChatColor.GRAY + "- Avistado: " + (spotted ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
            lore.add(ChatColor.GRAY + "- Eliminado: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission10) {
            // Mision 10: Reina Abeja
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.10.completed", false);
            lore.add(ChatColor.GRAY + "- Reina Derrotada: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission11) {
            // Mision 11: Amigo Snowman
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.11.completed", false);
            lore.add(ChatColor.GRAY + "- Sacrificio de Amistad: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission12) {
            // Mision 12: Veneno Explosivo
            lore.add("");
            int bees = data.getInt("players." + playerName + ".missions.12.bees_killed", 0);
            int bombs = data.getInt("players." + playerName + ".missions.12.bombitas_killed", 0);
            lore.add(ChatColor.GRAY + "- Corrupted Bees: " + (bees >= 30 ? ChatColor.GREEN : ChatColor.YELLOW) + bees + "/30");
            lore.add(ChatColor.GRAY + "- Bombitas: " + (bombs >= 30 ? ChatColor.GREEN : ChatColor.YELLOW) + bombs + "/30");
        } else if (mission instanceof Mission13) {
            // Mision 13: Stardew Valley (Lista detallada)
            lore.add("");
            lore.add(ChatColor.of("#FFCC99") + "Progreso de flores:");

            Mission13 m13 = (Mission13) mission; // Casteamos para obtener la lista
            int collectedCount = 0;

            // Recorremos la lista definida en la clase Mission13
            for (Material flower : m13.getRequiredFlowers()) {
                boolean has = data.getBoolean("players." + playerName + ".missions.13.collected." + flower.name(), false);
                if (has) collectedCount++;

                // Formatear nombre bonito (EJ: BLUE_ORCHID -> Blue orchid)
                String name = flower.name().toLowerCase().replace('_', ' ');
                name = name.substring(0, 1).toUpperCase() + name.substring(1);

                // Verde si la tiene, Gris si falta
                lore.add((has ? ChatColor.GREEN : ChatColor.GRAY) + "- " + name);
            }

            // Resumen al final
            lore.add("");
            lore.add(ChatColor.of("#FFA07A") + "Total: " + collectedCount + "/" + m13.getRequiredFlowers().size());
        } else if (mission instanceof Mission14) {
            // Mision 14: Volar
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.14.completed", false);
            lore.add(ChatColor.GRAY + "- 300 Bloques en 7s: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission15) {
            // Mision 15: Withers
            lore.add("");
            int killed = data.getInt("players." + playerName + ".missions.15.withers_killed", 0);
            lore.add(ChatColor.GRAY + "- Withers: " + (killed >= 3 ? ChatColor.GREEN : ChatColor.YELLOW) + killed + "/3");
        } else if (mission instanceof Mission16) {
            // Mision 16: Guardian Skeleton
            lore.add("");
            int killed = data.getInt("players." + playerName + ".missions.16.skeletons_killed", 0);
            lore.add(ChatColor.GRAY + "- Guardian C. Skeletons: " + (killed >= 20 ? ChatColor.GREEN : ChatColor.YELLOW) + killed + "/20");
        } else if (mission instanceof Mission17) {
            // Mision 17: Totems
            lore.add("");
            int popped = data.getInt("players." + playerName + ".missions.17.totems_popped", 0);
            lore.add(ChatColor.GRAY + "- Totems Usados: " + (popped >= 5 ? ChatColor.GREEN : ChatColor.YELLOW) + popped + "/5");
        } else if (mission instanceof Mission18) {
            // Mision 18: Pale Garden
            lore.add("");
            int hearts = data.getInt("players." + playerName + ".missions.18.hearts_broken", 0);
            lore.add(ChatColor.GRAY + "- Corazones Rotos: " + (hearts >= 10 ? ChatColor.GREEN : ChatColor.YELLOW) + hearts + "/10");
        } else if (mission instanceof Mission19) {
            // Mision 19: Jugando a ser músico
            lore.add("");
            int broken = data.getInt("players." + playerName + ".missions.19.shriekers_broken", 0);
            lore.add(ChatColor.GRAY + "- Chilladores: " + (broken >= 20 ? ChatColor.GREEN : ChatColor.YELLOW) + broken + "/20");
        } else if (mission instanceof Mission20) {
            // Mision 20: Totem Vacio
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.20.completed", false);
            lore.add(ChatColor.GRAY + "- Desafío: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission21) {
            // Mision 21: Elder Guardians
            lore.add("");
            int killed = data.getInt("players." + playerName + ".missions.21.guardians_killed", 0);
            lore.add(ChatColor.GRAY + "- Elders Guardians: " + (killed >= 3 ? ChatColor.GREEN : ChatColor.YELLOW) + killed + "/3");
        } else if (mission instanceof Mission22) {
            // Mision 22: Piglin Brute
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.22.completed", false);
            lore.add(ChatColor.GRAY + "- Venganza: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission23) {
            // Mision 23: Dragon
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.23.completed", false);
            lore.add(ChatColor.GRAY + "- Tiro Espectral: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission24) {
            // Mision 24: 1 HP
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.24.completed", false);
            lore.add(ChatColor.GRAY + "- Riesgo Mortal: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        } else if (mission instanceof Mission25) {
            // Mision 25: Tocar Pasto (Lista bloques)
            lore.add("");
            lore.add(ChatColor.of("#FFCC99") + "Bloques Rotos:");

            Mission25 m25 = (Mission25) mission;
            for (Material m : m25.getRequiredBlocks()) {
                boolean has = data.getBoolean("players." + playerName + ".missions.25.broken." + m.name(), false);
                String name = m.name().toLowerCase().replace("_", " ");
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                lore.add((has ? ChatColor.GREEN : ChatColor.GRAY) + "- " + name);
            }
        } else if (mission instanceof Mission26) {
            // Mision 26: Spectral Mobs
            lore.add("");
            int creepers = data.getInt("players." + playerName + ".missions.26.creepers_killed", 0);
            int ghasts = data.getInt("players." + playerName + ".missions.26.ghasts_killed", 0);
            lore.add(ChatColor.GRAY + "- Spectral Creepers: " + (creepers >= 15 ? ChatColor.GREEN : ChatColor.YELLOW) + creepers + "/15");
            lore.add(ChatColor.GRAY + "- Spectral Ghasts: " + (ghasts >= 15 ? ChatColor.GREEN : ChatColor.YELLOW) + ghasts + "/15");
        } else if (mission instanceof Mission27) {
            // Mision 27: Manu
            lore.add("");
            boolean completed = data.getBoolean("players." + playerName + ".missions.27.completed", false);
            lore.add(ChatColor.GRAY + "- Objetivo Eliminado: " + (completed ? ChatColor.GREEN + "✔" : ChatColor.RED + "✖"));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);
        }
    }
}