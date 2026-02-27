package Commands;

import imp.crissyjuanxd.IsManuSMP;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class Homes implements CommandExecutor {

    private final IsManuSMP plugin;
    private File homesFile;
    private FileConfiguration homesConfig;

    public Homes(IsManuSMP plugin) {
        this.plugin = plugin;
        createHomesConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo jugadores.");
            return true;
        }

        Player player = (Player) sender;

        // --- COMANDO: /SETHOME ---
        if (command.getName().equalsIgnoreCase("sethome")) {
            long cooldownTime = 86400000L; // 24 horas en milisegundos
            long lastUsed = homesConfig.getLong("Cooldowns." + player.getName(), 0);
            long timePassed = System.currentTimeMillis() - lastUsed;

            // Verificar Cooldown (Si ya pasaron 24h o es la primera vez)
            if (timePassed < cooldownTime) {
                long waitTime = (cooldownTime - timePassed) / 1000;
                long hours = waitTime / 3600;
                long minutes = (waitTime % 3600) / 60;
                player.sendMessage(ChatColor.RED + "Debes esperar " + hours + "h " + minutes + "m para cambiar tu casa.");
                return true;
            }

            // Guardar ubicación
            Location loc = player.getLocation();
            String worldName = loc.getWorld().getName();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();

            // Formato de guardado: "Mundo, x, y, z"
            // Ejemplo en YML: CrissyjuanxD: world, 172, 21, 2319
            String saveFormat = worldName + ", " + x + ", " + y + ", " + z;

            homesConfig.set("Homes." + player.getName(), saveFormat);
            homesConfig.set("Cooldowns." + player.getName(), System.currentTimeMillis()); // Actualizar cooldown
            saveHomesConfig();

            // Mensaje con colores HEX exactos a tu JSON
            String simbolo = ChatColor.of("#E28B20") + "" + ChatColor.BOLD + "\u06de"; // El símbolo árabe
            String texto1 = ChatColor.of("#F4D990") + " Has establecido tu punto de casa en";
            String coords = ChatColor.of("#DD6110") + "" + ChatColor.BOLD + " " + x + " " + y + " " + z;
            String saltoLinea = "\n";
            String texto2 = ChatColor.of("#F4D990") + "para teletransportarte a este punto, usa";
            String comandoHome = ChatColor.of("#C9DC8A") + "" + ChatColor.BOLD + " /home";

            player.sendMessage(simbolo + texto1 + coords + saltoLinea + texto2 + comandoHome);

            // Sonido de confirmación (opcional, sutil)
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);

            return true;
        }

        // --- COMANDO: /HOME ---
        if (command.getName().equalsIgnoreCase("home")) {
            if (!homesConfig.contains("Homes." + player.getName())) {
                player.sendMessage(ChatColor.RED + "No has establecido un punto de casa. Usa /sethome.");
                return true;
            }

            // Leer datos del YML
            String data = homesConfig.getString("Homes." + player.getName());
            String[] parts = data.split(", ");

            if (parts.length < 4) {
                player.sendMessage(ChatColor.RED + "Error en los datos de casa. Vuelve a usar /sethome.");
                return true;
            }

            // Parsear coordenadas y mundo
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]) + 0.5; // +0.5 para centrar en el bloque
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]) + 0.5;

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(ChatColor.RED + "El mundo de tu casa no existe o no está cargado.");
                return true;
            }

            Location homeLoc = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());

            // --- LÓGICA DE TELETRANSPORTE (Igual a tu SpawnCommand) ---

            // 1. Sonidos
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 0.6f);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 2.0f, 0.6f);

            // 2. Título
            player.sendTitle("§b§lTepea§3§lndote§r§l...", "", 20, 40, 20);

            // 3. Runnable con delay (4 segundos)
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.teleport(homeLoc);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    }
                }
            }.runTaskLater(plugin, 80L);

            return true;
        }

        return false;
    }

    // --- MÉTODOS PARA GESTIONAR EL ARCHIVO HOMES.YML ---

    private void createHomesConfig() {
        homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            homesFile.getParentFile().mkdirs();
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
    }

    private void saveHomesConfig() {
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}