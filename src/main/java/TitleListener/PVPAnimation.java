package TitleListener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PVPAnimation {
    private final JavaPlugin plugin;
    private boolean pvpEnabled;

    // Variables de seguimiento para el Debug
    private boolean isManualMode = false;
    private long targetTime = -1; // Guarda la hora en milisegundos en la que ocurrirá el siguiente cambio

    // Tareas de Bukkit para manejar los tiempos
    private BukkitTask autoEnableTask;
    private BukkitTask autoDisableTask;

    public PVPAnimation(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pvpEnabled = plugin.getConfig().getBoolean("pvp-enabled", false);

        // Aplicar el estado guardado como gamerules
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.PVP, pvpEnabled);
        }

        // Si el servidor inicia y el PvP está desactivado, arranca el contador de 2 horas
        if (!pvpEnabled) {
            startAutoTimer();
        }
    }

    public void togglePVP(boolean enable, boolean isManual) {
        this.pvpEnabled = enable;
        this.isManualMode = isManual;

        plugin.getConfig().set("pvp-enabled", enable);
        plugin.saveConfig();

        if (autoEnableTask != null) autoEnableTask.cancel();
        if (autoDisableTask != null) autoDisableTask.cancel();

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.PVP, enable);
        }

        String jsonTitle;
        Sound[] sounds;

        if (enable) {
            jsonTitle = "[\"\",{\"text\":\"\u2620\",\"color\":\"#83786F\"},{\"text\":\" PvP\",\"bold\":true,\"color\":\"#B73F40\"},{\"text\":\" Activado\",\"bold\":true,\"color\":\"#F65F70\"},{\"text\":\" \u2620\",\"color\":\"#83786F\"}]";
            sounds = new Sound[]{Sound.ENTITY_ALLAY_DEATH, Sound.ENTITY_BLAZE_DEATH};

            // --- MENSAJE DE AVISO (TELLRAW) ---
            String jsonMessage = "[\"\",{\"text\":\"\u26a0\",\"color\":\"#FF292B\"},{\"text\":\" Cuidado\",\"bold\":true,\"color\":\"#F5862E\"},{\"text\":\" \u25ba\",\"color\":\"gray\"},{\"text\":\"\\n\\n\"}," +
                    "{\"text\":\"Con el \",\"color\":\"#F9BF35\"},{\"text\":\"PvP activado\",\"bold\":true,\"color\":\"red\"},{\"text\":\" puedes perder tu inventario si te mata un jugador.\\n\\nAl conseguir una kill, el jugador recibirá +\",\"color\":\"#F9BF35\"}," +
                    "{\"text\":\"15\",\"bold\":true,\"color\":\"gold\"},{\"text\":\" ManuCoins\",\"bold\":true,\"color\":\"yellow\"},{\"text\":\".\\n\\nEl jugador que muera no volverá a soltar \",\"color\":\"#F9BF35\"}," +
                    "{\"text\":\"ManuCoins\",\"bold\":true,\"color\":\"yellow\"},{\"text\":\"\\nhasta que hayan pasado \",\"color\":\"#F9BF35\"},{\"text\":\"30 minutos\",\"bold\":true,\"color\":\"gold\"},{\"text\":\".\",\"color\":\"#F9BF35\"}]";

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a " + jsonMessage);

            if (!isManual) {
                targetTime = System.currentTimeMillis() + (30 * 60 * 1000L);
                autoDisableTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        togglePVP(false, false);
                    }
                }.runTaskLater(plugin, 36000L);
            } else {
                targetTime = -1;
            }
        } else {
            jsonTitle = "[\"\",{\"text\":\"\u2600\",\"color\":\"#83786F\"},{\"text\":\" PvP\",\"bold\":true,\"color\":\"#B73F40\"},{\"text\":\" Desactivado\",\"bold\":true,\"color\":\"#87F071\"},{\"text\":\" \u2600\",\"color\":\"#83786F\"}]";
            sounds = new Sound[]{Sound.BLOCK_NOTE_BLOCK_CHIME, Sound.ENTITY_EXPERIENCE_ORB_PICKUP};
            startAutoTimer();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " title " + jsonTitle);
            for (Sound sound : sounds) {
                player.playSound(player.getLocation(), sound, 10.0f, 0.7f);
            }
        }
    }

    private void startAutoTimer() {
        if (autoEnableTask != null) autoEnableTask.cancel();

        this.isManualMode = false; // Empezamos timer automático
        this.targetTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000L); // 2 horas en ms

        autoEnableTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Activar PvP de forma automática
                togglePVP(true, false);
            }
        }.runTaskLater(plugin, 144000L); // 2 horas = 144000 ticks
    }

    public boolean isPvPEnabled() {
        return pvpEnabled;
    }

    // Método que genera el texto para el comando /pvp debug
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("§8[§c!§8] §e§lINFO DE PVP §8[§c!§8]\n");
        sb.append("§7Estado actual: ").append(pvpEnabled ? "§aActivado" : "§cDesactivado").append("\n");
        sb.append("§7Modo actual: ").append(isManualMode ? "§6Manual" : "§bAutomático").append("\n");

        if (targetTime != -1 && !isManualMode) {
            long remainingMs = targetTime - System.currentTimeMillis();
            if (remainingMs > 0) {
                long totalSecs = remainingMs / 1000;
                long hours = totalSecs / 3600;
                long mins = (totalSecs % 3600) / 60;
                long secs = totalSecs % 60;

                String actionText = pvpEnabled ? "§cdesactivará" : "§aactivará";
                sb.append("§7Se ").append(actionText).append(" §7automáticamente en: §e")
                        .append(hours).append("h ").append(mins).append("m ").append(secs).append("s");
            } else {
                sb.append("§7Cambiando estado ahora mismo...");
            }
        } else {
            sb.append("§7Próximo cambio: §cNinguno (El modo es Manual, cambia con comando)");
        }

        return sb.toString();
    }
}