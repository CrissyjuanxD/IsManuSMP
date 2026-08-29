package list;

import Handlers.Teams.TeamType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class VHList extends BukkitRunnable {

    private final JavaPlugin plugin;
    private boolean showCreator = true;

    // Colores de vida actualizados
    private static final String COL_HP_HIGH = ChatColor.of("#4ade80").toString(); // Verde   > 12
    private static final String COL_HP_MID  = ChatColor.of("#fb923c").toString(); // Naranja 6-12
    private static final String COL_HP_LOW  = ChatColor.of("#ef4444").toString(); // Rojo    <= 6

    public VHList(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0L, 20L);

        // Alternador de créditos del footer
        new BukkitRunnable() {
            @Override
            public void run() {
                showCreator = !showCreator;
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeOldScoreboards(player); // Limpiamos rastro del ping/vida antiguos en scoreboard
            updateTablistForPlayer(player);
        }
    }

    public void updateTablistForPlayer(Player player) {
        int online = Bukkit.getOnlinePlayers().size();
        int ping = player.getPing();

        // ─── Header & Footer de Ismanugames ─────────────────────────────────────────
        String sepColor1 = ChatColor.of("#fdfd96").toString();
        String sepColor2 = ChatColor.of("#ffffff").toString();

        String separator = ChatColor.DARK_GRAY + "●" + sepColor1 + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "                 " +
                sepColor2 + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "                 " +
                sepColor1 + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "                 " + ChatColor.DARK_GRAY + "●\n";

        String header = separator +
                ChatColor.GRAY + " \n" +
                ChatColor.of("#fbbf24") + ChatColor.BOLD + "      \uD83E\uDD8A ISMANUGAMES.HOLY.GG \uD83E\uDD8A     \n" +
                ChatColor.GRAY + " \n" +
                ChatColor.of("#facc15") + "📊 ONLINE: " + ChatColor.WHITE + online + ChatColor.DARK_GRAY + "  |  " +
                ChatColor.of("#4ade80") + "📶 PING: " + ChatColor.WHITE + ping + " ms\n" +
                ChatColor.GRAY + " \n";

        String alternatingText;
        if (showCreator) {
            alternatingText = ChatColor.WHITE + "" + ChatColor.BOLD + "Programado por: " + ChatColor.of("#22d3ee") + "CrissyjuanxD";
        } else {
            alternatingText = ChatColor.WHITE + "" + ChatColor.BOLD + "Organizado por: " + ChatColor.of("#facc15") + "IsManuPlay";
        }

        String footer = " \n" + alternatingText + " \n" +
                ChatColor.GRAY + " \n" +
                ChatColor.GRAY + "Hosteado por @HolyHosting\n" +
                ChatColor.GRAY + " \n" +
                separator.replace("\n", "");

        player.setPlayerListHeaderFooter(header, footer);

        // ─── Sistema de Prefijos, Nombre y Corazones ────────────────
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = mainScoreboard.getEntryTeam(player.getName());

        String tabPrefix = "";
        String colorHex = ChatColor.GRAY.toString();
        String suffix = "";

        if (team != null) {
            suffix = team.getSuffix() != null ? team.getSuffix() : "";
            TeamType type = TeamType.getById(team.getName());

            if (type != null) {
                tabPrefix = type.getTabPrefix();
                colorHex = type.getBungeeColor().toString();
            } else {
                tabPrefix = team.getPrefix() != null ? team.getPrefix() : "";
            }
        }

        // Cálculos de vida
        int hp = (int) Math.ceil(player.getHealth());
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        int totalHp = hp + absorption;

        // El símbolo ahora es siempre el mismo (texto plano), solo cambia el color
        String heartSymbol = "❤";
        String heartColor;

        if (hp > 12) {
            heartColor = COL_HP_HIGH; // Verde
        } else if (hp > 6) {
            heartColor = COL_HP_MID;  // Naranja
        } else {
            heartColor = COL_HP_LOW;  // Rojo
        }

        // Concatenación final del nombre en Tablist (Con 2 espacios exactos y el corazón pegado al número)
        String coloredName = ChatColor.WHITE + tabPrefix + colorHex + player.getName() + suffix + "  " + heartColor + heartSymbol + totalHp;

        if (!coloredName.equals(player.getPlayerListName())) {
            player.setPlayerListName(coloredName);
        }
    }

    // Limpia los objetivos de Scoreboard para que no interfieran ni dupliquen información
    public void removeOldScoreboards(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            Objective healthObjectiveVct = scoreboard.getObjective("Healthvct");
            if (healthObjectiveVct != null) healthObjectiveVct.unregister();

            Objective healthObjective = scoreboard.getObjective("tabHealth");
            if (healthObjective != null) healthObjective.unregister();

            Objective pingObjective = scoreboard.getObjective("tabPing");
            if (pingObjective != null) pingObjective.unregister();
        }
    }
}