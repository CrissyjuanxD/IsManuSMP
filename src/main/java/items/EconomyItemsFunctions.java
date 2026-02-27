package items;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyItemsFunctions implements Listener {

    private final JavaPlugin plugin;
    private Connection connection;

    // Mapas de control
    private final Map<UUID, String> mochilasAbiertas = new ConcurrentHashMap<>();
    private final Map<String, ItemStack[]> mochilasCache = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> mochilasGuardando = new ConcurrentHashMap<>();
    private final Set<UUID> cooldownGancho = ConcurrentHashMap.newKeySet();

    private final Object dbLock = new Object();


    public EconomyItemsFunctions(JavaPlugin plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    // --- BASE DE DATOS OPTIMIZADA ---
    private void setupDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder + "/mochilas.db");

            try (Statement stmt = connection.createStatement()) {
                // 1. Crear tabla básica (si es nueva instalación)
                stmt.execute("CREATE TABLE IF NOT EXISTS mochilas (" +
                        "id TEXT PRIMARY KEY, " +
                        "items TEXT, " +
                        "owner TEXT)"); // Agregamos owner aquí para nuevas DB

                try {
                    stmt.execute("ALTER TABLE mochilas ADD COLUMN owner TEXT");
                    plugin.getLogger().info("Base de datos actualizada: Columna 'owner' agregada.");
                } catch (SQLException ignored) {
                    // La columna ya existe, no pasa nada.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error CRÍTICO al configurar la base de datos de mochilas!");
        }
    }

    // --- LOGICA DE ITEMS ---

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !event.getAction().toString().contains("RIGHT")) return;

        Player player = event.getPlayer();

        // Evitar interacciones si se está guardando datos (Anti-Exploit)
        if (mochilasGuardando.getOrDefault(player.getUniqueId(), false)) {
            player.sendMessage(ChatColor.RED + "Tus datos se están guardando, espera un momento...");
            event.setCancelled(true);
            return;
        }

        // Detectores
        if (isEnderBag(item)) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
            player.openInventory(player.getEnderChest());
            return;
        }

        if (isMochila(item)) {
            event.setCancelled(true);
            abrirMochila(player, item);
            return;
        }

        if (isGancho(item)) {
            event.setCancelled(true);
            usarGancho(player, item);
            return;
        }

        // Yunques y Manzanas
        if (checkAndUseYunque(player, item)) {
            event.setCancelled(true);
            return;
        }

        if (isManzanaPanico(item)) {
            event.setCancelled(true);
            usarManzanaPanico(player, item);
            return;
        }
    }

    // --- DETECTORES DE ITEMS ---

    public boolean isMochila(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        // Rango de mochilas: 2020 a 2027
        return cmd >= 2020 && cmd <= 2027;
    }

    private int getBackpackSize(int modelData) {
        switch (modelData) {
            case 2020: return 18; // Nvl 1 (2 filas)
            case 2021: return 27; // Nvl 2 (3 filas)
            case 2022: return 36; // Nvl 3 (4 filas)
            case 2023: return 45; // Nvl 4 (5 filas)
            case 2024: return 54; // Nvl 5 (6 filas)
            default: return 27;   // Default 3 filas
        }
    }

    private boolean isEnderBag(ItemStack item) {
        return checkModelData(item, 2030);
    }

    private boolean isGancho(ItemStack item) {
        return checkModelData(item, 10) && item.getType() == Material.FISHING_ROD;
    }

    private boolean isManzanaPanico(ItemStack item) {
        return checkModelData(item, 10) && item.getType() == Material.APPLE;
    }

    // Helper para reducir código repetitivo
    private boolean checkModelData(ItemStack item, int id) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == id;
    }

    private boolean checkAndUseYunque(Player player, ItemStack item) {
        if (!item.getType().toString().contains("SPAWN_EGG")) return false; // Optimización rápida
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        if (cmd == 2040) { // Nivel 1
            usarYunque(player, item, 0.25);
            return true;
        } else if (cmd == 2050) { // Nivel 2
            usarYunque(player, item, 1.0);
            return true;
        }
        return false;
    }

    // --- SISTEMA DE MOCHILAS ---

    private void abrirMochila(Player player, ItemStack mochila) {
        ItemMeta meta = mochila.getItemMeta();
        int modelData = meta.getCustomModelData();
        int size = getBackpackSize(modelData);

        String mochilaId = getMochilaId(mochila);

        // Asignar ID si es nueva
        if (mochilaId == null) {
            mochilaId = UUID.randomUUID().toString();
            setMochilaId(mochila, mochilaId);
            if (player.getInventory().getItemInMainHand().isSimilar(mochila)) {
                player.getInventory().setItemInMainHand(mochila);
            } else if (player.getInventory().getItemInOffHand().isSimilar(mochila)) {
                player.getInventory().setItemInOffHand(mochila);
            }
        }

        mochilasAbiertas.put(player.getUniqueId(), mochilaId);
        ChatColor color = getMochilaColor(mochila);

        // Título dinámico
        String guiTitle = color + "" + ChatColor.BOLD + "Mochila (Nvl " + getLevelByModel(modelData) + ")";
        Inventory mochilaInv = Bukkit.createInventory(null, size, guiTitle);

        try {
            ItemStack[] contents;
            // 1. Intentar cargar de caché (RAM)
            if (mochilasCache.containsKey(mochilaId)) {
                contents = mochilasCache.get(mochilaId);
            } else {
                // 2. Si no está en RAM, cargar de DB
                contents = cargarMochila(mochilaId);
                // Cachear inmediatamente
                if (contents != null) mochilasCache.put(mochilaId, contents);
            }

            // Ajustar contenido al tamaño actual de la mochila (por si subió de nivel o es nueva)
            if (contents != null) {
                // Si la mochila es más grande que los items guardados, copiamos lo que hay
                ItemStack[] resizedContents = new ItemStack[size];
                for (int i = 0; i < Math.min(contents.length, size); i++) {
                    resizedContents[i] = contents[i];
                }
                mochilaInv.setContents(resizedContents);
            }

        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Error crítico al cargar mochila. Contacta a un admin.");
            plugin.getLogger().severe("Error DB cargar: " + e.getMessage());
            mochilasAbiertas.remove(player.getUniqueId());
            return;
        }

        player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1.0f, 1.0f);
        player.openInventory(mochilaInv);
    }

    private int getLevelByModel(int model) {
        if (model == 2020) return 1;
        if (model == 2021) return 2;
        if (model == 2022) return 3;
        if (model == 2023) return 4;
        if (model == 2024) return 5;
        return 1;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (mochilasAbiertas.containsKey(playerId)) {
            String mochilaId = mochilasAbiertas.get(playerId);
            Inventory inv = event.getInventory();
            ItemStack[] contents = inv.getContents();

            // Actualizamos caché inmediatamente (memoria RAM)
            mochilasCache.put(mochilaId, contents);
            mochilasGuardando.put(playerId, true);

            // Guardar en DB (Async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = false;
                try {
                    guardarMochila(mochilaId, contents, playerId.toString());
                    success = true;
                } catch (SQLException e) {
                    plugin.getLogger().severe("ERROR FATAL guardando mochila de " + player.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    boolean finalSuccess = success;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            mochilasGuardando.remove(playerId);
                            mochilasAbiertas.remove(playerId); // Liberamos "abierta"

                            if (!finalSuccess) {
                                // SI FALLÓ EL GUARDADO: AVISAR AL JUGADOR
                                if (player.isOnline()) {
                                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "¡ADVERTENCIA!");
                                    player.sendMessage(ChatColor.RED + "Hubo un error guardando tu mochila en la base de datos.");
                                    player.sendMessage(ChatColor.RED + "Tus ítems están en memoria temporal. Por favor, intenta abrir y cerrar la mochila de nuevo en unos segundos.");
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                                }
                                // NOTA: No borramos de mochilasCache aquí, para que si la abre de nuevo, los ítems sigan ahí.
                            }
                        }
                    }.runTask(plugin);
                }
            });

            player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_REMOVE_ONE, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (mochilasAbiertas.containsKey(player.getUniqueId())) {
            String id = mochilasAbiertas.get(player.getUniqueId());
            Inventory top = player.getOpenInventory().getTopInventory();
            ItemStack[] content = top.getContents();
            mochilasCache.put(id, content);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    // Guardamos con el UUID del que se desconectó
                    guardarMochila(id, content, player.getUniqueId().toString());
                } catch (Exception ignored) {}
            });

            mochilasAbiertas.remove(player.getUniqueId());
        }
    }

    // --- PREVENCIÓN DE ANIDAMIENTO ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack current = event.getCurrentItem();

        if (title.equals(ChatColor.DARK_GRAY + "Registro de Mochilas (Lost)")) {
            event.setCancelled(true);
            if (current == null || current.getType() == Material.AIR) return;

            // Obtener ID del nombre del item (Paper)
            if (current.hasItemMeta() && current.getItemMeta().hasDisplayName()) {
                String displayName = current.getItemMeta().getDisplayName();
                // Formato esperado: "ID: <uuid>"
                String idRaw = ChatColor.stripColor(displayName).replace("ID: ", "").trim();

                // Crear una mochila física NUEVA (Usamos Nivel 5 por seguridad para que quepa todo)
                ItemStack recoveredBag = EconomyItems.createPurpleMochila();

                // Forzamos el ID en el item
                setMochilaId(recoveredBag, idRaw);

                // Agregamos lore visual de recuperado
                ItemMeta meta = recoveredBag.getItemMeta();
                List<String> lore = meta.getLore();
                lore.add(ChatColor.RED + "" + ChatColor.BOLD + "[RECUPERADA]");
                meta.setLore(lore);
                recoveredBag.setItemMeta(meta);

                player.getInventory().addItem(recoveredBag);
                player.sendMessage(ChatColor.GREEN + "Has generado una copia de la mochila ID: " + idRaw);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                player.closeInventory();
            }
            return;
        }

        // 2. LÓGICA DEL MENÚ DE RECUPERACIÓN (ADMIN)
        if (title.startsWith(ChatColor.DARK_RED + "Mochilas de: ")) {
            event.setCancelled(true); // No permitir mover items en el menú de vista previa

            if (current == null || !isMochila(current)) return;

            // A) Shift + Click Derecho = DUPLICAR (Recuperar para el admin)
            if (event.isShiftClick() && event.isRightClick()) {
                ItemStack clone = current.clone();

                // Limpiamos el lore de "(En EnderChest)" si lo tiene para que sea un item limpio
                ItemMeta meta = clone.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.removeIf(line -> line.contains("(En EnderChest)"));
                    meta.setLore(lore);
                    clone.setItemMeta(meta);
                }

                player.getInventory().addItem(clone);
                player.sendMessage(ChatColor.GREEN + "Has recuperado (duplicado) esta mochila a tu inventario.");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                player.closeInventory();
            }
            // B) Click Normal = ABRIR CONTENIDO (Editar)
            else {
                player.closeInventory(); // Cerramos el menú de selección
                // Abrimos la mochila real (la base de datos es la misma, así que editarás los items reales)
                abrirMochila(player, current);
                player.sendMessage(ChatColor.YELLOW + "Editando contenido de la mochila del jugador...");
            }
            return;
        }

        // 3. PREVENCIÓN DE ANIDAMIENTO (Tu lógica anterior)
        // Si el título contiene "Mochila" (la interfaz de la mochila abierta), prohibir meter otras mochilas
        if (title.contains("Mochila (Nvl")) {
            if (isMochila(event.getCurrentItem()) || isMochila(event.getCursor())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "No puedes anidar mochilas.");
            }
            // Prohibir números (hotbar swap)
            if (event.getClick().toString().contains("NUMBER")) {
                event.setCancelled(true);
            }
        }
    }

    public List<String> getBackpacksByOwner(UUID ownerId) {
        List<String> ids = new ArrayList<>();
        if (connection == null) return ids;

        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM mochilas WHERE owner = ?")) {
            ps.setString(1, ownerId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    // --- BASE DE DATOS MÉTODOS ---

    private ItemStack[] cargarMochila(String mochilaId) throws SQLException {
        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT items FROM mochilas WHERE id = ?")) {
                ps.setString(1, mochilaId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String data = rs.getString("items");
                    if (data != null && !data.isEmpty()) {
                        return ItemSerializer.deserialize(data);
                    }
                }
            }
        }
        return null;
    }


    private void guardarMochila(String mochilaId, ItemStack[] items, String ownerUUID) throws SQLException {
        if (items == null || connection == null || connection.isClosed()) return;

        // SERIALIZACIÓN SEGURA:
        // Si la serialización falla y devuelve vacío, NO guardes nada para no borrar los datos viejos.
        String serialized = ItemSerializer.serialize(items);
        if (serialized == null || serialized.isEmpty()) {
            plugin.getLogger().warning("Intento de guardar mochila vacía o corrupta cancelado para ID: " + mochilaId);
            return;
        }

        // BLOQUE SINCRONIZADO: Solo un hilo puede entrar aquí a la vez
        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT OR REPLACE INTO mochilas (id, items, owner) VALUES (?, ?, ?)")) {
                    ps.setString(1, mochilaId);
                    ps.setString(2, serialized);
                    ps.setString(3, ownerUUID != null ? ownerUUID : "unknown");
                    ps.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                // Si falla, hacemos rollback para no dejar la DB corrupta
                try { connection.rollback(); } catch (SQLException ignored) {}
                throw e; // Re-lanzamos el error para que onInventoryClose sepa que falló
            } finally {
                try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public void openAllBackpacksMenu(Player admin) {
        // Ejecutar query async para no congelar el server si hay muchas mochilas
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> ids = new ArrayList<>();
            // Limitamos a 54 por simplicidad (las más recientes si SQLite respeta orden de inserción/update)
            // Si quieres ver más, habría que hacer sistema de páginas, pero esto cubre emergencias recientes.
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM mochilas LIMIT 54")) {
                while (rs.next()) {
                    ids.add(rs.getString("id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Volver al hilo principal para abrir GUI
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Registro de Mochilas (Lost)");

                    for (String id : ids) {
                        ItemStack icon = new ItemStack(Material.PAPER);
                        ItemMeta meta = icon.getItemMeta();
                        meta.setDisplayName(ChatColor.GOLD + "ID: " + ChatColor.WHITE + id);
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY + "Click para generar una");
                        lore.add(ChatColor.GRAY + "mochila física con este ID.");
                        lore.add("");
                        lore.add(ChatColor.RED + "¡CUIDADO! " + ChatColor.GRAY + "Esto crea un duplicado");
                        lore.add(ChatColor.GRAY + "si la original aún existe.");
                        meta.setLore(lore);
                        icon.setItemMeta(meta);
                        inv.addItem(icon);
                    }
                    admin.openInventory(inv);
                }
            }.runTask(plugin);
        });
    }

    private String getOwner(String mochilaId) {
        if (connection == null) return "unknown";
        try (PreparedStatement ps = connection.prepareStatement("SELECT owner FROM mochilas WHERE id = ?")) {
            ps.setString(1, mochilaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("owner");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    // --- OTROS ITEMS ---

    private void usarGancho(Player player, ItemStack gancho) {
        if (cooldownGancho.contains(player.getUniqueId())) return;

        cooldownGancho.add(player.getUniqueId());
        player.setCooldown(Material.FISHING_ROD, 40);

        new BukkitRunnable() {
            @Override
            public void run() { cooldownGancho.remove(player.getUniqueId()); }
        }.runTaskLater(plugin, 40);

        // Desgaste
        if (gancho.getDurability() < gancho.getType().getMaxDurability()) {
            gancho.setDurability((short) (gancho.getDurability() + 1));
        } else {
            player.getInventory().removeItem(gancho);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }

        // Física
        Vector direction = player.getLocation().getDirection().normalize().multiply(1.6);
        player.setVelocity(direction);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.5f);

        // Partículas (simplificado)
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
    }

    private void usarYunque(Player player, ItemStack yunque, double porcentaje) {
        repararArmadura(player, porcentaje);
        yunque.setAmount(yunque.getAmount() - 1);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
    }

    private void usarManzanaPanico(Player player, ItemStack manzana) {
        EconomyItems.applyPanicAppleEffects(player);
        manzana.setAmount(manzana.getAmount() - 1);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
    }

    private void repararArmadura(Player player, double porcentaje) {
        ItemStack[] armadura = player.getInventory().getArmorContents();
        for (ItemStack item : armadura) {
            if (item != null && item.getType() != Material.AIR && item.getDurability() > 0) {
                int repairAmount = (int) (item.getType().getMaxDurability() * porcentaje);
                item.setDurability((short) Math.max(0, item.getDurability() - repairAmount));
            }
        }
        player.getInventory().setArmorContents(armadura);
    }

    // --- HELPERS ID / LORE ---

    private String getMochilaId(ItemStack mochila) {
        if (!mochila.hasItemMeta()) return null;
        List<String> lore = mochila.getItemMeta().getLore();
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("ID:")) {
                return ChatColor.stripColor(line).split(":")[1].trim();
            }
        }
        return null;
    }

    private void setMochilaId(ItemStack mochila, String id) {
        ItemMeta meta = mochila.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "ID: " + ChatColor.GRAY + id);
        meta.setLore(lore);
        mochila.setItemMeta(meta);
        // Nota: Actualizar inventario si es necesario
    }

    public ChatColor getMochilaColor(ItemStack mochila) {
        int cmd = mochila.getItemMeta().getCustomModelData();
        switch (cmd) {
            case 2021: return ChatColor.GREEN;
            case 2022: return ChatColor.RED;
            case 2023: return ChatColor.BLUE;
            case 2024: return ChatColor.DARK_PURPLE;
            default: return ChatColor.of("#ffffcc");
        }
    }

    public void onDisable() {
        // Guardado forzoso al apagar
        for (Map.Entry<String, ItemStack[]> entry : mochilasCache.entrySet()) {
            try {
                String id = entry.getKey();
                ItemStack[] items = entry.getValue();

                // 1. Recuperar el dueño actual de la DB para no perderlo
                String currentOwner = getOwner(id);

                // 2. Guardar usando el dueño recuperado
                guardarMochila(id, items, currentOwner);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try { if (connection != null) connection.close(); } catch (SQLException e) {}
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}