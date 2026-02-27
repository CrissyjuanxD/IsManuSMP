package Events.MissionSystem;

import Handlers.DayHandler;
import TitleListener.RuletaAnimation;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MissionHandler implements Listener {
    private final JavaPlugin plugin;
    private final DayHandler dayHandler;
    private final File missionFile;
    private final Map<Integer, Mission> missions = new HashMap<>();
    private final Set<Integer> activeMissions = new HashSet<>();
    private final RuletaAnimation ruletaAnimation; // Añadido para la animación

    public MissionHandler(JavaPlugin plugin, DayHandler dayHandler) {
        this.plugin = plugin;
        this.dayHandler = dayHandler;
        this.missionFile = new File(plugin.getDataFolder(), "misiones.yml");
        this.ruletaAnimation = new RuletaAnimation(plugin); // Inicializar animación

        registerMissions();
        ensureFileExists();
        loadMissionData();
    }

    private void registerMissions() {
        // Asegúrate de registrar todas tus misiones aquí
        missions.put(1, new Mission1(plugin, this));

        missions.put(2, new Mission2(plugin, this));

        missions.put(3, new Mission3(plugin, this));

        missions.put(4, new Mission4(plugin, this));

        missions.put(5, new Mission5(plugin, this));

        missions.put(6, new Mission6(plugin, this));

        missions.put(7, new Mission7(plugin, this));

        missions.put(8, new Mission8(plugin, this));

        missions.put(9, new Mission9(plugin, this));

        missions.put(10, new Mission10(plugin, this));

        missions.put(11, new Mission11(plugin, this));

        missions.put(12, new Mission12(plugin, this));

        missions.put(13, new Mission13(plugin, this));

        missions.put(14, new Mission14(plugin, this));

        missions.put(15, new Mission15(plugin, this));

        missions.put(16, new Mission16(plugin, this));

        missions.put(17, new Mission17(plugin, this));

        missions.put(18, new Mission18(plugin, this));

        missions.put(19, new Mission19(plugin, this));

        missions.put(20, new Mission20(plugin, this));

        missions.put(21, new Mission21(plugin, this));

        missions.put(22, new Mission22(plugin, this));

        missions.put(23, new Mission23(plugin, this));

        missions.put(24, new Mission24(plugin, this));

        missions.put(25, new Mission25(plugin, this));

        missions.put(26, new Mission26(plugin, this));

        missions.put(27, new Mission27(plugin, this));
    }

    public void registerAllMissionListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Registrar listener de cada misión individual
        for (Mission mission : missions.values()) {
            if (mission instanceof Listener) {
                plugin.getServer().getPluginManager().registerEvents((Listener) mission, plugin);
            }
        }
        plugin.getLogger().info("Sistema de misiones: Listeners registrados correctamente.");
    }

    private void ensureFileExists() {
        try {
            if (!missionFile.exists()) {
                missionFile.getParentFile().mkdirs();
                missionFile.createNewFile();
                YamlConfiguration.loadConfiguration(missionFile).save(missionFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error creando archivo de misiones: " + e.getMessage());
        }
    }

    public void activateMission(CommandSender sender, int missionNumber) {
        if (!missions.containsKey(missionNumber)) {
            sender.sendMessage(ChatColor.RED + "La misión " + missionNumber + " no existe.");
            return;
        }

        if (activeMissions.contains(missionNumber)) {
            sender.sendMessage(ChatColor.RED + "La misión " + missionNumber + " ya está activada.");
            return;
        }

        activeMissions.add(missionNumber);

        // Inicializar datos para todos los jugadores registrados
        FileConfiguration config = plugin.getConfig();
        Set<String> allPlayers = config.getConfigurationSection("HasJoinedBefore") != null ?
                config.getConfigurationSection("HasJoinedBefore").getKeys(false) : new HashSet<>();

        for (String uuid : allPlayers) {
            String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
            if (playerName != null) {
                initializePlayerMissionData(playerName, missionNumber);
            }
        }

        saveMissionData();
        sender.sendMessage(ChatColor.GREEN + "Misión " + missionNumber + " activada correctamente.");

        // --- ANIMACIÓN DE RULETA PARA TODOS ---
        String missionName = missions.get(missionNumber).getName();
        String missionDesc = missions.get(missionNumber).getDescription();

        // JSON Modificado: Se agregaron saltos de linea extra y el texto gris al final
        String safeDescription = missionDesc.replace("\"", "\\\"").replace("\n", "\\n");

        String jsonMessage = String.format(
                "[\"\",{\"text\":\"\\n۞ \",\"bold\":true,\"color\":\"#ffaa00\"}," +
                        "{\"text\":\"NUEVA MISIÓN DESBLOQUEADA\",\"bold\":true,\"color\":\"#FFA500\"}," +
                        "{\"text\":\"\\n[\",\"color\":\"white\"}," +
                        "{\"text\":\"%s\",\"bold\":true,\"color\":\"#dda0dd\"," +
                        "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"%s\",\"color\":\"gray\"}}}," +
                        "{\"text\":\"]\\n\\n\",\"color\":\"white\"}," +
                        "{\"text\":\"usa /misiones para abrir su interfaz o usa el item de Misiones\",\"color\":\"gray\"}]",
                missionName,
                safeDescription // Usamos la variable con los reemplazos hechos
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            ruletaAnimation.playAnimation(online, jsonMessage);
        }
    }

    public void deactivateMission(CommandSender sender, int missionNumber) {
        if (!missions.containsKey(missionNumber)) {
            sender.sendMessage(ChatColor.RED + "La misión " + missionNumber + " no existe.");
            return;
        }

        activeMissions.remove(missionNumber);
        // Nota: No borramos los datos de los jugadores para mantener historial,
        // pero la misión deja de estar "activa" en el servidor.

        saveMissionData(); // Guardar estado de misiones activas
        sender.sendMessage(ChatColor.GREEN + "Misión " + missionNumber + " desactivada.");
    }

    public void initializePlayerMissionData(String playerName, int missionNumber) {
        if (!missions.containsKey(missionNumber)) return;

        FileConfiguration data = YamlConfiguration.loadConfiguration(missionFile);

        // Si ya existen datos de esa misión para ese jugador, no los sobrescribimos para evitar reinicios accidentales
        if (data.contains("players." + playerName + ".missions." + missionNumber)) {
            return;
        }

        if (!data.contains("players." + playerName)) {
            data.set("players." + playerName + ".completed", 0);
        }

        data.set("players." + playerName + ".missions." + missionNumber + ".completed", false);
        data.set("players." + playerName + ".missions." + missionNumber + ".token_received", false);
        data.set("players." + playerName + ".missions." + missionNumber + ".reward_claimed", false);

        missions.get(missionNumber).initializePlayerData(playerName);

        try {
            data.save(missionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al inicializar datos de misión: " + e.getMessage());
        }
    }

    public boolean completeMission(String playerName, int missionNumber) {
        if (!activeMissions.contains(missionNumber)) return false;

        FileConfiguration data = YamlConfiguration.loadConfiguration(missionFile);

        if (data.getBoolean("players." + playerName + ".missions." + missionNumber + ".completed", false)) {
            return false;
        }

        data.set("players." + playerName + ".missions." + missionNumber + ".completed", true);
        int completed = data.getInt("players." + playerName + ".completed", 0);
        data.set("players." + playerName + ".completed", completed + 1);

        try {
            data.save(missionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar progreso: " + e.getMessage());
            return false;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            giveMissionToken(player, missionNumber);
            String missionName = missions.get(missionNumber).getName();

            // Mensaje de completado (Simplificado para Vanilla)
            String jsonMessage = String.format(
                    "[\"\",{\"text\":\"\\n۞ \",\"bold\":true,\"color\":\"#ffaa00\"}," +
                            "{\"text\":\"%s\",\"bold\":true,\"color\":\"#87ceeb\"}," +
                            "{\"text\":\" ha completado la misión \",\"color\":\"#7eaee4\"}," +
                            "{\"text\":\"[\",\"color\":\"white\"}," +
                            "{\"text\":\"%s\",\"bold\":true,\"color\":\"#dda0dd\"}," +
                            "{\"text\":\"]\\n\",\"color\":\"white\"}]",
                    player.getName(),
                    missionName
            );

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a " + jsonMessage);

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            player.sendMessage(ChatColor.GREEN + "Progreso: " + ChatColor.GOLD + (completed + 1) +
                    ChatColor.GREEN + "/" + ChatColor.GOLD + activeMissions.size() +
                    ChatColor.GREEN + " misiones completadas");
        }
        return true;
    }

    // Métodos de utilidad
    private void giveMissionToken(Player player, int missionNumber) {
        ItemStack token = createMissionToken(missionNumber);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(token);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(ChatColor.of("#FFA07A") + "¡Inventario lleno! Token dropeado al suelo.");
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(missionFile);
        data.set("players." + player.getName() + ".missions." + missionNumber + ".token_received", true);
        try { data.save(missionFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public ItemStack createMissionToken(int missionNumber) {
        // Cambio de material a POPPED_CHORUS_FRUIT
        ItemStack token = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = token.getItemMeta();

        // Título
        meta.setDisplayName(ChatColor.GOLD + "Ficha de Misión #" + missionNumber);
        meta.setCustomModelData(3000 + missionNumber);

        // Encantamiento visual (Glow)
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

        // Obtener nombre de la misión
        String missionName = "Misión Desconocida";
        if (missions.containsKey(missionNumber)) {
            missionName = missions.get(missionNumber).getName();
        }

        // Lore actualizado
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Misión Completada:");
        lore.add(ChatColor.of("#FFCC99") + "Target: " + ChatColor.WHITE + missionName); // Nombre de la misión en el centro
        lore.add("");
        lore.add(ChatColor.GRAY + "Entrégalo en el spawn.");
        lore.add(ChatColor.GRAY + "> Click Derecho a la:");
        lore.add(ChatColor.of("#FFB347") + "Estatua de Recompensas");

        meta.setLore(lore);
        token.setItemMeta(meta);
        return token;
    }

    public void saveMissionData() {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionFile);
        data.set("activeMissions", new ArrayList<>(activeMissions));
        try {
            data.save(missionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error guardando misiones.yml: " + e.getMessage());
        }
    }

    public void loadMissionData() {
        if (!missionFile.exists()) return;

        FileConfiguration data = YamlConfiguration.loadConfiguration(missionFile);
        List<Integer> savedActiveMissions = data.getIntegerList("activeMissions");

        activeMissions.clear();
        activeMissions.addAll(savedActiveMissions);

        plugin.getLogger().info("Misiones cargadas activas: " + activeMissions.toString());
    }

    // Getters y Setters
    public Map<Integer, Mission> getMissions() { return missions; }
    public Set<Integer> getActiveMissions() { return activeMissions; }
    public File getMissionFile() { return missionFile; }
    public boolean isMissionActive(int missionNumber) { return activeMissions.contains(missionNumber); }
    public DayHandler getDayHandler() { return dayHandler; }

    // Métodos addMissionToPlayer y removeMissionFromPlayer se mantienen igual que tu versión original
    // ya que son administrativos.
    public void addMissionToPlayer(CommandSender sender, String playerName, int missionNumber) {
        if (completeMission(playerName, missionNumber)) {
            sender.sendMessage(ChatColor.GREEN + "Misión forzada completada para " + playerName);
        }
    }

    public void removeMissionFromPlayer(CommandSender sender, String playerName, int missionNumber) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(missionFile);
        data.set("players." + playerName + ".missions." + missionNumber, null);
        int completed = data.getInt("players." + playerName + ".completed", 0);
        data.set("players." + playerName + ".completed", Math.max(0, completed - 1));
        initializePlayerMissionData(playerName, missionNumber);
        try { data.save(missionFile); } catch (IOException e) { e.printStackTrace(); }
        sender.sendMessage(ChatColor.GREEN + "Misión removida de " + playerName);
    }
}