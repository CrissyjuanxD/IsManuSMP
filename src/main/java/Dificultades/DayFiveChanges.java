package Dificultades;

import Dificultades.CustomMobs.*;
import Handlers.DayHandler;
import items.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DayFiveChanges implements Listener {
    private final JavaPlugin plugin;
    private final DayHandler dayHandler;
    private boolean isApplied = false;
    private final Random random = new Random();

    // Mobs Custom
    private final EspectralGhast espectralGhast;
    private final EspectralCreeper espectralCreeper;
    private final Bombita bombita;
    private final CorruptedZombies corruptedZombies;

    // Keys y Mapas
    private final Map<UUID, Long> cooldownPlayers = new HashMap<>();
    private final NamespacedKey explosiveShulkerKey;
    private final NamespacedKey shulkerTNTKey;
    private final NamespacedKey protectedShellKey;

    public DayFiveChanges(JavaPlugin plugin, DayHandler handler) {
        this.plugin = plugin;
        this.dayHandler = handler;
        this.espectralGhast = new EspectralGhast(plugin);
        this.espectralCreeper = new EspectralCreeper(plugin);
        this.bombita = new Bombita(plugin);
        this.corruptedZombies = new CorruptedZombies(plugin);

        this.explosiveShulkerKey = new NamespacedKey(plugin, "explosive_shulker_bullet");
        this.shulkerTNTKey = new NamespacedKey(plugin, "shulker_tnt");
        this.protectedShellKey = new NamespacedKey(plugin, "protected_shulker_shell");
    }

    public void apply() {
        if (!isApplied) {
            isApplied = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
            espectralGhast.apply();
            espectralCreeper.apply();
            setEndGameRule(GameRule.KEEP_INVENTORY, true);
        }
    }

    public void revert() {
        if (isApplied) {
            isApplied = false;
            espectralGhast.revert();
            espectralCreeper.revert();
            setEndGameRule(GameRule.KEEP_INVENTORY, false);

            removeRecipe("guardian_powder");
            removeRecipe("apilate_gold_block");
            removeRecipe("ender_emblem");
            removeRecipe("corrupted_golden_apple");

            HandlerList.unregisterAll(this);
        }
    }

    private void removeRecipe(String keyName) {
        Bukkit.removeRecipe(new NamespacedKey(plugin, keyName));
    }

    private void setEndGameRule(GameRule<Boolean> rule, boolean value) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.THE_END) {
                world.setGameRule(rule, value);
            }
        }
    }

    // --- SPAWNS MODIFICADOS ---

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isApplied) return;
        if (event.isCancelled()) return;
        handleRandomMobConversion(event);
    }

    private void handleRandomMobConversion(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.ENDERMAN) return;
        if (event.getLocation().getWorld().getEnvironment() != World.Environment.THE_END) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        double chance = random.nextDouble();

        // CAMBIO: Bajado de 0.15 (15%) a 0.08 (8%)
        if (chance > 0.08) {
            return;
        }

        event.setCancelled(true);
        Location loc = event.getLocation();

        double typeChance = random.nextDouble();

        // CAMBIO: Redistribución para eliminar al Zombie Corrupto
        if (typeChance < 0.40) {
            // 40% Ender Ghast
            espectralGhast.spawnEnderGhast(loc);
        } else if (typeChance < 0.80) {
            // 40% Ender Creeper
            espectralCreeper.spawnEnderCreeper(loc);
        } else {
            // 20% Bombita (Restante)
            bombita.spawnBombita(loc);
        }
        // Eliminado el bloque del CorruptedZombie
    }

    // --- CONSUMIBLES ---

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!isApplied) return;
        if (event.getItem().isSimilar(CorruptedGoldenApple.createCorruptedGoldenApple())) {
            CorruptedGoldenApple.applyEffects(event.getPlayer());
        }
    }

    // --- SHULKER Y PROYECTILES ---

    @EventHandler
    public void onShulkerShoot(ProjectileLaunchEvent event) {
        if (!isApplied) return;
        if (event.getEntity() instanceof ShulkerBullet bullet) {
            if (bullet.getShooter() instanceof Shulker) {
                bullet.getPersistentDataContainer().set(explosiveShulkerKey, PersistentDataType.BYTE, (byte) 1);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isApplied) return;
        if (!(event.getEntity() instanceof ShulkerBullet bullet)) return;

        if (bullet.getPersistentDataContainer().has(explosiveShulkerKey, PersistentDataType.BYTE)) {
            // 1. Crear explosión
            bullet.getWorld().createExplosion(bullet.getLocation(), 2.0f, false, true);

            if (event.getHitEntity() instanceof LivingEntity target) {
                // Levitación I (amplifier 0) por 10 segundos (200 ticks), igual que vanilla
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, 0));
            }
        }
    }

    // --- PROTECCIÓN DE DROPS Y DAÑO ---

    @EventHandler
    public void onExplosionDamageItems(EntityDamageByEntityEvent event) {
        if (!isApplied) return;
        if (event.getEntity() instanceof Item item) {
            if (item.getItemStack().getType() == Material.SHULKER_SHELL) {
                EntityDamageEvent.DamageCause cause = event.getCause();
                if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                        cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                        isFireCause(cause)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isFireCause(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.LAVA;
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!isApplied) return;
        if (event.getEntity().getItemStack().getType() == Material.SHULKER_SHELL) {
            event.getEntity().getPersistentDataContainer().set(protectedShellKey, PersistentDataType.BYTE, (byte) 1);
            event.getEntity().setInvulnerable(true);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (!isApplied) return;
        if (event.getEntity() instanceof Item item) {
            if (item.getItemStack().getType() == Material.SHULKER_SHELL ||
                    item.getPersistentDataContainer().has(protectedShellKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onShulkerDeath(EntityDeathEvent event) {
        if (!isApplied) return;
        if (event.getEntityType() != EntityType.SHULKER) return;

        LivingEntity shulker = event.getEntity();
        // CAMBIO: Eliminada la lógica de spawnear TNT aquí

        event.getDrops().removeIf(item -> item.getType() == Material.SHULKER_SHELL);

        if (random.nextDouble() < 0.35) {
            int amount = 1;
            Player killer = shulker.getKiller();
            if (killer != null) {
                ItemStack weapon = killer.getInventory().getItemInMainHand();
                if (weapon.containsEnchantment(Enchantment.LOOTING)) {
                    int lootingLevel = weapon.getEnchantmentLevel(Enchantment.LOOTING);
                    if (lootingLevel >= 3) {
                        if (random.nextDouble() < 0.30) {
                            amount = 2;
                        }
                    }
                }
            }
            event.getDrops().add(new ItemStack(Material.SHULKER_SHELL, amount));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isApplied) return;
        if (event.getEntityType() == EntityType.SHULKER) {
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setDamage(event.getDamage() * 0.2);
            }
        }
    }

    // --- BUFFS A MOBS ---

    @EventHandler
    public void onCreaturesEnderSpawn(CreatureSpawnEvent event) {
        if (!isApplied) return;
        if (event.getEntityType() == EntityType.ENDERMITE) {
            event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1));
        }
    }

    @EventHandler
    public void onPotionEffectApply(EntityPotionEffectEvent event) {
        if (!isApplied) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getEntity().getWorld().getEnvironment() == World.Environment.THE_END &&
                event.getNewEffect() != null &&
                event.getNewEffect().getType() == PotionEffectType.INVISIBILITY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerVoidDamage(EntityDamageEvent event) {
        if (!isApplied) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (player.getWorld().getEnvironment() != World.Environment.THE_END) return;

        long currentTime = System.currentTimeMillis();
        if (cooldownPlayers.containsKey(player.getUniqueId())) {
            long lastDamageTime = cooldownPlayers.get(player.getUniqueId());
            if (currentTime - lastDamageTime < 5000) {
                event.setCancelled(true);
                return;
            }
        }

        boolean tieneTotem = player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING ||
                player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;

        if (tieneTotem) {
            cooldownPlayers.put(player.getUniqueId(), currentTime);
            event.setCancelled(true);
            player.setNoDamageTicks(0);

            // Este daño manual es el que confunde a la Misión 20
            player.damage(player.getHealth() + 500.0);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isValid() && !player.isDead()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 3 * 20, 100, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 50 * 20, 0, false, false));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 2.0f);
                }
            }, 5L);
        }
    }
}