package Dificultades.CustomMobs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Pose; // Import necesario para forzar que no se levante

public class Fox_Statue {

    public static final String STATUE_NAME = ChatColor.of("#FFB347") + "" + ChatColor.BOLD + "Estatua de Recompensas";

    public static void spawn(Location loc) {
        Fox fox = (Fox) loc.getWorld().spawnEntity(loc, EntityType.FOX);

        // 1. Forzar estado visual de sueño (Evita que se levante)
        fox.setSleeping(true);
        fox.setPose(Pose.SLEEPING); // IMPORTANTE: Esto fuerza la animación visual

        // 2. Congelar IA y Físicas
        fox.setAI(false);
        fox.setGravity(false);
        fox.setCollidable(false); // Recomendado para estatuas para evitar empujones fantasma

        // 3. Protección
        fox.setInvulnerable(true);
        fox.setSilent(true);

        // 4. Bloquear edad (Adulto permanente)
        fox.setAgeLock(true);
        fox.setAdult();

        // --- NOMBRE ---
        fox.setCustomName(STATUE_NAME);
        fox.setCustomNameVisible(true);
        fox.setRemoveWhenFarAway(false);

        // --- ESCALADO ---
        // Se ha aumentado de 1.7 a 2.5 para que sea más imponente
        if (fox.getAttribute(Attribute.SCALE) != null) {
            fox.getAttribute(Attribute.SCALE).setBaseValue(2.1);
        }

        // Tag para identificarlo
        fox.addScoreboardTag("reward_statue");
    }
}