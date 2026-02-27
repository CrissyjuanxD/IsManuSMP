package Dificultades;

import Bosses.QueenBeeHandler;
import Dificultades.CustomMobs.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import Handlers.DayHandler;
import Dificultades.Features.AltarActivateEvent;

import java.util.*;

public class DayFourChanges implements Listener {
    private final JavaPlugin plugin;
    private boolean isApplied = false;
    /*private final MobCapManager mobCapManager;*/
    private final Map<Location, Long> altarCooldowns = new HashMap<>();
    private final Random random = new Random();
    private final GuardianBlaze blazespawmer;
    private final GuardianCorruptedSkeleton guardianCorruptedSkeleton;
    private final CorruptedInfernalSpider corruptedInfernalSpider;
    private final CorruptedZombies corruptedZombies;
    private final CorruptedSpider corruptedSpider;
    private final CorruptedBee corruptedBee;
    private final Bombita bombita;
    private final Iceologer iceologer;
    /*private final QueenBeeHandler queenBeeHandler;*/
    private final DayHandler dayHandler;

    private final NamespacedKey uuidKey;
    private final NamespacedKey upgradeKey;

    public DayFourChanges(JavaPlugin plugin, DayHandler handler) {
        this.plugin = plugin;
        this.dayHandler = handler;
        this.blazespawmer = new GuardianBlaze(plugin);
        this.guardianCorruptedSkeleton = new GuardianCorruptedSkeleton(plugin);
        this.corruptedInfernalSpider = new CorruptedInfernalSpider(plugin);
        /*this.queenBeeHandler = new QueenBeeHandler(plugin);*/
        this.corruptedZombies = new CorruptedZombies(plugin);
        this.corruptedSpider = new CorruptedSpider(plugin,handler);
        this.corruptedBee = new CorruptedBee(plugin);
        this.bombita = new Bombita(plugin);
        this.iceologer= new Iceologer(plugin);
        this.uuidKey = new NamespacedKey(plugin, "creator_uuid");
        this.upgradeKey = new NamespacedKey(plugin, "is_upgrade");

    }

    public void apply() {
        if (!isApplied) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            isApplied = true;
            blazespawmer.apply();
            guardianCorruptedSkeleton.apply();
            corruptedInfernalSpider.apply();
            corruptedBee.apply();
        }
    }

    public void revert() {
        if (isApplied) {
            isApplied = false;
            blazespawmer.revert();
            guardianCorruptedSkeleton.revert();
            corruptedInfernalSpider.revert();
            corruptedBee.revert();

            Bukkit.removeRecipe(new NamespacedKey(plugin, "fragment_upgrade"));
            Bukkit.removeRecipe(new NamespacedKey(plugin, "duplicador"));
            Bukkit.removeRecipe(new NamespacedKey(plugin, "netherite_upgrade"));
            Bukkit.removeRecipe(new NamespacedKey(plugin, "netherite_duplicado"));

            Bukkit.removeRecipe(new NamespacedKey(plugin, "nether_emblem"));
            Bukkit.removeRecipe(new NamespacedKey(plugin, "overworld_emblem"));
            Bukkit.removeRecipe(new NamespacedKey(plugin, "fragmento_infernal"));

            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onAltarActivate(AltarActivateEvent event) {
        if (!isApplied) return;

        if (event.getAltarType().equals("queen_bee")) {
            Player player = event.getPlayer();
            Location loc = event.getLocation();

            // Lógica específica del boss (Bad Omen, etc.)
            if (player.getPotionEffect(PotionEffectType.BAD_OMEN) != null) {

                // Verificar si ya hay un boss vivo cerca (opcional, tu lógica antigua lo tenía)
                if (isQueenBeeSpawned(loc)) {
                    player.sendMessage(ChatColor.RED + "۞ Ya hay una Reina viva cerca.");
                    // No ponemos cooldown porque falló la invocación
                    return;
                }

                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(loc) <= 40 * 40) {
                        p.playSound(loc, Sound.MUSIC_DISC_TEARS, SoundCategory.RECORDS, 10.0f, 0.8f);
                    }
                }

                // INVOCACIÓN
                spawnQueenBee(loc);

                // Consumir el Bad Omen
                player.removePotionEffect(PotionEffectType.BAD_OMEN);

                // Por ejemplo: 20 minutos = 1200 segundos
                event.setCooldownSeconds(1500);

            } else {
                player.sendMessage(ChatColor.RED + "۞ Necesitas Bad Omen para activar este altar.");
                // No seteamos cooldown, el evento termina sin activar el timer
            }
        }
    }


    private boolean isQueenBeeSpawned(Location altarLocation) {
        for (Entity entity : Objects.requireNonNull(altarLocation.getWorld()).getNearbyEntities(altarLocation, 50, 50, 50)) {
            if (entity instanceof Bee bee && "Abeja Reina".equals(bee.getCustomName())) {
                return true;
            }
        }
        return false;
    }

    private void spawnQueenBee(Location altarLocation) {
        World world = altarLocation.getWorld();
        if (world == null) return;

        Location center = altarLocation.clone().add(0.5, 0.5, 0.5);
        int totalSteps = 100;
        int finalY = 4;

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step <= totalSteps) {
                    double progress = (double) step / totalSteps;
                    double yOffset = progress * finalY;

                    Location particleLoc = center.clone().add(0, yOffset, 0);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 5, 0.1, 0, 0.1, 0.01);

                    if (step % 20 == 0) {
                        world.playSound(center, Sound.BLOCK_HONEY_BLOCK_SLIDE, 3.0f, 0.5f + (float)progress);
                    }

                    step++;
                } else if (step <= totalSteps + 40) {
                    double angle = (step - totalSteps) * (Math.PI / 20);
                    for (int i = 0; i < 360; i += 10) {
                        double radians = Math.toRadians(i);
                        double radius = 1.5 + Math.sin(angle) * 1.5;
                        double x = Math.cos(radians) * radius;
                        double z = Math.sin(radians) * radius;
                        Location loc = center.clone().add(0, finalY, 0).add(x, 0, z);
                        world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                    }

                    if ((step - totalSteps) % 10 == 0) {
                        world.playSound(center.clone().add(0, finalY, 0), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 3.0f, 1.5f);
                    }

                    step++;
                } else {
                    Location spawnLocation = center.clone().add(0, finalY, 0);
                    world.spawnParticle(Particle.EXPLOSION, spawnLocation, 1);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, spawnLocation, 100, 0.5, 1, 0.5, 0.1);
                    world.playSound(spawnLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5.0f, 1.0f);

                    QueenBeeHandler.spawn(plugin, spawnLocation);

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    //MOBS
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isApplied) return;

        // Primero manejar la lógica de Wither Skeletons (existente)
        if (shouldConvertWitherSpawn(event)) {
            WitherSkeleton skeleton = (WitherSkeleton) event.getEntity();
            convertToCorruptedSkeleton(skeleton);
            return;
        }

        handlePiglinToSpiderConversion(event);
        handleCorruptedZombieConversion(event);
        handleCorruptedSpiderConversion(event);
    }

    private void handleCorruptedZombieConversion(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.ZOMBIE) return;

        if (event.getEntity().getPersistentDataContainer().has(corruptedZombies.getCorruptedKey(), PersistentDataType.BYTE)) {
            return;
        }

        if (random.nextInt(12) != 0) return;

        Zombie zombie = (Zombie) event.getEntity();
        corruptedZombies.transformToCorruptedZombie(zombie);
    }

    private void handleCorruptedSpiderConversion(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.SPIDER) return;

        if (event.getLocation().getWorld().getEnvironment() != World.Environment.NORMAL) return;

        if (event.getEntity().getPersistentDataContainer().has(corruptedSpider.getCorruptedSpiderKey(), PersistentDataType.BYTE)) {
            return;
        }

        if (random.nextInt(12) != 0) return;

        Spider spider = (Spider) event.getEntity();
        corruptedSpider.transformspawnCorruptedSpider(spider);
    }

    private void handlePiglinToSpiderConversion(CreatureSpawnEvent event) {
        if (event.getLocation().getWorld().getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (event.getEntityType() != EntityType.ZOMBIFIED_PIGLIN) return;

        if (event.getEntity().getPersistentDataContainer()
                .has(corruptedInfernalSpider.getCorruptedInfernalKey(), PersistentDataType.BYTE)) {
            return;
        }

        if (random.nextInt(10) != 0) return;

        PigZombie pigZombie = (PigZombie) event.getEntity();
        Location loc = pigZombie.getLocation();

        corruptedInfernalSpider.spawnCorruptedInfernalSpider(loc);
        pigZombie.remove();
    }

    private boolean shouldConvertWitherSpawn(CreatureSpawnEvent event) {
        return isApplied &&
                event.getEntityType() == EntityType.WITHER_SKELETON &&
                !event.getEntity().getPersistentDataContainer()
                        .has(guardianCorruptedSkeleton.getGCSkeletonKey(), PersistentDataType.BYTE);
    }

    private void convertToCorruptedSkeleton(WitherSkeleton skeleton) {
        Location loc = skeleton.getLocation();
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 0.5, 0.5, 0.5);

        guardianCorruptedSkeleton.transformToCorruptedSkeleton(skeleton);
    }

    @EventHandler
    public void onWitherSkeletonDeath(EntityDeathEvent event) {
        if (!isApplied) return;

        if (event.getEntityType() == EntityType.WITHER_SKELETON) {
            WitherSkeleton skeleton = (WitherSkeleton) event.getEntity();
            PersistentDataContainer data = skeleton.getPersistentDataContainer();

            if (!data.has(guardianCorruptedSkeleton.getGCSkeletonKey(), PersistentDataType.BYTE)) {
                event.getDrops().removeIf(item -> item.getType() == Material.WITHER_SKELETON_SKULL);
            }
        }
    }

    //Permitir que los creepers puedan spawnear de dia

}