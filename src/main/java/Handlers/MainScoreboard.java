package Handlers;

import Handlers.Teams.TeamType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class MainScoreboard extends BukkitRunnable {

    private final JavaPlugin plugin;

    public MainScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard == mainBoard || scoreboard.getObjective("ManuBoard") == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        for (Team mainTeam : mainBoard.getTeams()) {
            Team pTeam = scoreboard.getTeam(mainTeam.getName());
            if (pTeam == null) {
                pTeam = scoreboard.registerNewTeam(mainTeam.getName());
                pTeam.setPrefix(mainTeam.getPrefix());
                pTeam.setSuffix(mainTeam.getSuffix());
                pTeam.setColor(mainTeam.getColor());
                pTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, mainTeam.getOption(Team.Option.NAME_TAG_VISIBILITY));
                pTeam.setOption(Team.Option.COLLISION_RULE, mainTeam.getOption(Team.Option.COLLISION_RULE));
            }
            for (String entry : mainTeam.getEntries()) {
                if (!pTeam.hasEntry(entry)) {
                    pTeam.addEntry(entry);
                }
            }
        }

        Objective objective = scoreboard.getObjective("ManuBoard");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("ManuBoard", "dummy",
                    ChatColor.of("#fbbf24") + "" + ChatColor.BOLD + " MANUGAMES ");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Extraer rango
        String rankName = "Ninguno";
        String rankColor = ChatColor.GRAY.toString();
        Team playerTeam = mainBoard.getEntryTeam(player.getName());

        if (playerTeam != null) {
            TeamType type = TeamType.getById(playerTeam.getName());
            if (type != null) {
                rankName = type.getDisplayName();
                rankColor = type.getBungeeColor().toString();
            }
        }

        // Calcular horas jugadas
        int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        double hoursPlayed = ticksPlayed / (20.0 * 60.0 * 60.0);
        String formattedHours = String.format("%.1f", hoursPlayed).replace(",", ".") + "h";

        // Obtener estado del PvP directamente del mundo del jugador
        boolean pvpEnabled = player.getWorld().getPVP();
        String pvpStatus = ChatColor.of("#c26a7a") + (pvpEnabled ? "Activado" : "Desactivado"); // Rojo vino pastel

        // Setear las líneas del scoreboard (de mayor a menor)
        setLine(scoreboard, objective, 11, ChatColor.GRAY + " ");
        setLine(scoreboard, objective, 10, ChatColor.WHITE + "Usuario: " + ChatColor.of("#189adf") + player.getName());
        setLine(scoreboard, objective, 9, ChatColor.GRAY + "  ");
        setLine(scoreboard, objective, 8, ChatColor.WHITE + "Rango: " + rankColor + rankName);
        setLine(scoreboard, objective, 7, ChatColor.GRAY + "   ");
        setLine(scoreboard, objective, 6, ChatColor.WHITE + "Modo: " + ChatColor.of("#fdfd96") + "IsManuSMP 4"); // Amarillo pastel
        setLine(scoreboard, objective, 5, ChatColor.GRAY + "    ");
        setLine(scoreboard, objective, 4, ChatColor.WHITE + "Horas Jugadas: " + ChatColor.of("#4ade80") + formattedHours);
        setLine(scoreboard, objective, 3, ChatColor.GRAY + "     ");
        setLine(scoreboard, objective, 2, ChatColor.WHITE + "PVP: " + pvpStatus);
        setLine(scoreboard, objective, 1, ChatColor.GRAY + "      ");
        setLine(scoreboard, objective, 0, ChatColor.of("#fdfd96") + "   ismanugames.holy.gg");
    }

    private void setLine(Scoreboard board, Objective objective, int lineNumber, String text) {
        String entry = ChatColor.values()[lineNumber].toString() + ChatColor.RESET;

        Team team = board.getTeam("sb_line_" + lineNumber);
        if (team == null) {
            team = board.registerNewTeam("sb_line_" + lineNumber);
            team.addEntry(entry);
        }

        team.setPrefix(text);
        objective.getScore(entry).setScore(lineNumber);
    }
}