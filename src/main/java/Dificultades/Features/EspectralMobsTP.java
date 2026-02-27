package Dificultades.Features;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class EspectralMobsTP {
    protected final JavaPlugin plugin;
    protected final NamespacedKey mobKey;
    private boolean eventsRegistered = false;
    private BukkitRunnable autoTeleportTask; // Tarea para el TP automático

    public EspectralMobsTP(JavaPlugin plugin, String keyName) {
        this.plugin = plugin;
        this.mobKey = new NamespacedKey(plugin, keyName);
    }

    public void apply() {
        if (!eventsRegistered) {
            // 1. Registrar evento de daño (Tu lógica original)
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onMobHurt(EntityDamageEvent event) {
                    if (isCustomMob(event.getEntity())) {
                        handleTeleportOnDamage((LivingEntity) event.getEntity());
                    }
                }
            }, plugin);

            // 2. Iniciar el loop de teletransporte aleatorio (NUEVO)
            startAutoTeleportTask();

            eventsRegistered = true;
        }
    }

    // Nuevo método para detener todo (útil para reload o revert)
    public void revert() {
        if (autoTeleportTask != null && !autoTeleportTask.isCancelled()) {
            autoTeleportTask.cancel();
            autoTeleportTask = null;
        }
        // Nota: Los listeners no se pueden des-registrar individualmente fácil sin HandlerList,
        // pero el task sí es importante pararlo.
        eventsRegistered = false;
    }

    private void startAutoTeleportTask() {
        // Ejecutar cada 100 ticks (5 segundos)
        autoTeleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    // Optimización: Solo iterar LivingEntities, no todas las entidades
                    for (LivingEntity entity : world.getLivingEntities()) {

                        // Si es nuestro mob custom
                        if (isCustomMob(entity)) {

                            // LÓGICA DE TIEMPO (1 a 3 minutos)
                            // Promedio 120 segs. Ejecutamos cada 5 segs.
                            // Chance = 5 / 120 = 0.0416 (aprox 4.2%)
                            // Esto asegura que cada mob tenga su propio "reloj" interno aleatorio
                            if (Math.random() < 0.042) {
                                teleportRandomly(entity, 30);
                            }
                        }
                    }
                }
            }
        };
        // Inicia ya, repite cada 5 segundos (100 ticks)
        autoTeleportTask.runTaskTimer(plugin, 100L, 100L);
    }

    protected void handleTeleportOnDamage(LivingEntity mob) {
        // 50% de chance al recibir daño (Tu lógica original)
        if (Math.random() < 0.5) {
            teleportRandomly(mob, 30);
        }
    }

    protected void teleportRandomly(LivingEntity mob, int radius) {
        World world = mob.getWorld();
        Location originalLoc = mob.getLocation();

        // Cálculo seguro de ubicación
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * radius;
        double x = originalLoc.getX() + distance * Math.cos(angle);
        double z = originalLoc.getZ() + distance * Math.sin(angle);

        // Buscar la altura más alta en ese punto para no aparecer bajo tierra
        int highestY = world.getHighestBlockYAt((int)x, (int)z);

        // Un poco de seguridad: Si el techo está muy bajo (void) o muy alto, ajustar
        if (highestY < -60) highestY = originalLoc.getBlockY();

        Location newLoc = new Location(world, x, highestY + 1, z);

        // Efectos en salida
        world.playSound(originalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.0f);
        world.spawnParticle(Particle.PORTAL, originalLoc, 50, 0.5, 0.5, 0.5, 0.5);

        // Teletransporte
        mob.teleport(newLoc);

        // Efectos en llegada
        world.playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.0f);
        world.spawnParticle(Particle.PORTAL, newLoc, 50, 0.5, 0.5, 0.5, 0.5);
    }

    public abstract boolean isCustomMob(Entity entity);
}