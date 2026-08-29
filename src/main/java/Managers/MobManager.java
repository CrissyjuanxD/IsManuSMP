package Managers;

import Bosses.QueenBeeHandler;
import Dificultades.CustomMobs.*;
import Handlers.DayHandler;
import imp.crissyjuanxd.IsManuSMP;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MobManager {

    private final IsManuSMP plugin;
    private final DayHandler dayHandler;

    // Instancias de Mobs
    private final Bombita bombitaSpawner;
    private final Iceologer iceologerSpawner;
    private final CorruptedZombies corruptedZombieSpawner;
    private final CorruptedSpider corruptedSpider;
    private final GuardianBlaze guardianBlaze;
    private final GuardianCorruptedSkeleton guardianCorruptedSkeleton;
    private final CorruptedInfernalSpider corruptedInfernalSpider;
    private final CustomBoat customBoat;
    private final InfestedBeeHandler infestedBeeHandler;
    private final CorruptedBee corruptedBee;

    private final List<String> registeredMobs;

    @FunctionalInterface
    public interface SpawnCallback {
        void onSpawned(Entity entity);
    }

    private volatile SpawnCallback pendingCallback = null;
    private volatile Location expectedSpawnLocation = null;

    public void notifyEntitySpawned(Entity entity) {
        if (pendingCallback == null || expectedSpawnLocation == null) return;
        Location eLoc = entity.getLocation();
        Location expLoc = expectedSpawnLocation;
        if (eLoc.getWorld() == null || !eLoc.getWorld().equals(expLoc.getWorld())) return;
        if (eLoc.distanceSquared(expLoc) > 36) return;

        SpawnCallback cb = pendingCallback;
        pendingCallback = null;
        expectedSpawnLocation = null;
        cb.onSpawned(entity);
    }

    public MobManager(IsManuSMP plugin, DayHandler dayHandler) {
        this.plugin = plugin;
        this.dayHandler = dayHandler;

        this.bombitaSpawner = new Bombita(plugin);
        this.iceologerSpawner = new Iceologer(plugin);
        this.corruptedZombieSpawner = new CorruptedZombies(plugin);
        this.corruptedSpider = new CorruptedSpider(plugin, dayHandler);
        this.guardianBlaze = new GuardianBlaze(plugin);
        this.guardianCorruptedSkeleton = new GuardianCorruptedSkeleton(plugin);
        this.corruptedInfernalSpider = new CorruptedInfernalSpider(plugin);
        this.customBoat = new CustomBoat(plugin);
        this.infestedBeeHandler = new InfestedBeeHandler(plugin);
        this.corruptedBee = new CorruptedBee(plugin);

        this.registeredMobs = new ArrayList<>();
        cargarNombresDeMobs();
    }

    private void cargarNombresDeMobs() {
        String[] mobs = {
                "bombita", "iceologer", "corruptedzombie", "corruptedspider", "queenbee",
                "guardianblaze", "guardiancorruptedskeleton", "corruptedinfernalspider",
                "infestedbee", "estatuarecompensa", "corruptedbee"
        };
        for (String mob : mobs) {
            registeredMobs.add(mob);
        }
    }

    public boolean spawnMob(String mobType, Location location, Player targetPlayer, String variantArgs) {
        switch (mobType.toLowerCase()) {
            case "bombita": bombitaSpawner.spawnBombita(location); return true;
            case "iceologer": iceologerSpawner.spawnIceologer(location); return true;
            case "corruptedzombie": corruptedZombieSpawner.spawnCorruptedZombie(location); return true;
            case "corruptedspider": corruptedSpider.spawnCorruptedSpider(location); return true;
            case "queenbee": QueenBeeHandler.spawn(plugin, location); return true;
            case "guardianblaze": guardianBlaze.spawnGuardianBlaze(location); return true;
            case "guardiancorruptedskeleton": guardianCorruptedSkeleton.spawnGuardianCorruptedSkeleton(location); return true;
            case "corruptedinfernalspider": corruptedInfernalSpider.spawnCorruptedInfernalSpider(location); return true;
            case "infestedbee": infestedBeeHandler.spawnInfestedBee(location); return true;
            case "estatuarecompensa": Estatua_Reward.spawn(location); return true;
            case "corruptedbee": corruptedBee.spawnCorruptedBee(location); return true;
            default: return false;
        }
    }

    public Entity spawnMobAndReturn(String mobType, Location location, Player targetPlayer, String variantArgs) {
        final Entity[] captured = {null};

        pendingCallback = entity -> captured[0] = entity;
        expectedSpawnLocation = location.clone();

        spawnMob(mobType, location, targetPlayer, variantArgs);

        pendingCallback = null;
        expectedSpawnLocation = null;

        return captured[0];
    }

    public List<String> getRegisteredMobs() {
        return registeredMobs;
    }
}