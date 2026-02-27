package TitleListener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPAnimation {
    private final JavaPlugin plugin;
    private boolean pvpEnabled;

    public PVPAnimation(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pvpEnabled = plugin.getConfig().getBoolean("pvp-enabled", false);

        // Aplicar el estado guardado como gamerules
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.PVP, pvpEnabled);
        }
    }

    public void togglePVP(boolean enable) {
        this.pvpEnabled = enable;
        plugin.getConfig().set("pvp-enabled", enable);
        plugin.saveConfig();

        // Aplicar gamerule PvP en todos los mundos
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.PVP, enable);
        }

        String jsonTitle;
        Sound[] sounds;

        if (enable) {
            jsonTitle = "[\"\",{\"text\":\"\u2620\",\"color\":\"#83786F\"},{\"text\":\" PvP\",\"bold\":true,\"color\":\"#B73F40\"},{\"text\":\" Activado\",\"bold\":true,\"color\":\"#F65F70\"},{\"text\":\" \u2620\",\"color\":\"#83786F\"}]";
            sounds = new Sound[]{Sound.ENTITY_ALLAY_DEATH, Sound.ENTITY_BLAZE_DEATH};
        } else {
            jsonTitle = "[\"\",{\"text\":\"\u2600\",\"color\":\"#83786F\"},{\"text\":\" PvP\",\"bold\":true,\"color\":\"#B73F40\"},{\"text\":\" Desactivado\",\"bold\":true,\"color\":\"#87F071\"},{\"text\":\" \u2600\",\"color\":\"#83786F\"}]";
            sounds = new Sound[]{Sound.BLOCK_NOTE_BLOCK_CHIME, Sound.ENTITY_EXPERIENCE_ORB_PICKUP};
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " title " + jsonTitle);
            for (Sound sound : sounds) {
                player.playSound(player.getLocation(), sound, 10.0f, 0.7f);
            }
        }
    }

    public boolean isPvPEnabled() {
        return pvpEnabled;
    }
}
