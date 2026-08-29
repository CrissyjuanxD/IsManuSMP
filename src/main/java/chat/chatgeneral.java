package chat;

import Handlers.Teams.TeamType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class chatgeneral implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getEntryTeam(player.getName());

        String finalPrefix = "";
        String suffix = "";
        ChatColor nameColor = ChatColor.WHITE;

        if (team != null) {
            suffix = team.getSuffix();
            TeamType type = TeamType.getById(team.getName());

            if (type != null) {
                // Toma toda la info directamente del Enum optimizado
                finalPrefix = type.getChatPrefix();
                nameColor = type.getBungeeColor();
            } else {
                finalPrefix = team.getPrefix();
            }
        } else {
            finalPrefix = ChatColor.GRAY + "";
        }

        // Formateo del mensaje usando las variables de la enumeración
        event.setFormat(finalPrefix + nameColor + "%1$s" + ChatColor.RESET + suffix + ChatColor.WHITE + ": %2$s");
    }

    // Mensajes de Moderacion
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && block.getType() == Material.CHEST && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM, HH:mm:ss");
            Date date = new Date();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + (player.getName() + " HA ABIERTO UN COFRE, COORDENADAS " + block.getLocation() + ", FECHA: ").toUpperCase() + ChatColor.GOLD + formatter.format(date).toUpperCase());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM, HH:mm:ss");
            Date date = new Date();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + (player.getName() + " HA ROTO UN COFRE, COORDENADAS " + block.getLocation() + ", FECHA: ").toUpperCase() + ChatColor.GOLD + formatter.format(date).toUpperCase());
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Team team = scoreboard.getEntryTeam(player.getName());
        ItemStack item = event.getCurrentItem();
        if ((player.isOp() || (team != null && team.getName().equals("mod"))) && item != null && item.getType() != Material.AIR && event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM, HH:mm:ss");
            Date date = new Date();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + (player.getName() + " HA SACADO " + item.getAmount() + " " + item.getType() + " DEL MODO CREATIVO, FECHA: ").toUpperCase() + ChatColor.YELLOW + formatter.format(date).toUpperCase());
        }
    }
}