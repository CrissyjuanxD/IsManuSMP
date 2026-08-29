package Managers;

import Dificultades.DayOneChanges;
import Habilidades.HabilidadesBook;
import imp.crissyjuanxd.IsManuSMP;
import items.*;
import items.IceBow.IceBowItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    private final IsManuSMP plugin;

    private final DoubleLifeTotem doubleLifeTotem;
    private final EconomyIceTotem economyIceTotem;
    private final EconomyFlyTotem economyFlyTotem;
    private final excavatorItem ExcavatorItem;
    private final AmuletBloodM amuletBloodM;
    private final AmuletInmortal amuletInmortal;
    private final LifeCampfire lifeCampfire;
    private final IceBowItem iceBowItem;
    private final HappyGhastEnchant happyGhastEnchant;
    private final ItemsEventos itemsEventos;

    private final List<String> registeredItems;

    public ItemManager(IsManuSMP plugin) {
        this.plugin = plugin;
        this.doubleLifeTotem = new DoubleLifeTotem(plugin);
        this.economyIceTotem = new EconomyIceTotem(plugin);
        this.economyFlyTotem = new EconomyFlyTotem(plugin);
        this.ExcavatorItem = new excavatorItem(plugin);
        this.amuletBloodM = new AmuletBloodM(plugin);
        this.amuletInmortal = new AmuletInmortal(plugin);
        this.lifeCampfire = new LifeCampfire(plugin);
        this.iceBowItem = new IceBowItem(plugin);
        this.happyGhastEnchant = new HappyGhastEnchant(plugin);
        this.itemsEventos = new ItemsEventos(plugin);

        this.registeredItems = new ArrayList<>();
        cargarNombresDeItems();
    }

    private void cargarNombresDeItems() {
        String[] items = {
                "doubletotem", "corrupted_steak", "corrupted_golden_apple", "libro_habilidades",
                "dinocoins", "dinofichas", "blood_fragment", "mochila_nivel_1", "mochila_nivel_2",
                "mochila_nivel_3", "mochila_nivel_4", "mochila_nivel_5", "enderbag", "gancho",
                "panic_apple", "artefacto_nivel_1", "artefacto_nivel_2", "misiones", "icetotem",
                "flytotem", "excavator_pickaxe", "potion_resistance_2", "splash_resistance_3",
                "potion_slow_falling", "splash_regeneration_3", "potion_haste_3", "potion_haste_2",
                "splash_absorption_10", "frasco_de_velocidad", "amulet_bloodmoon", "amuleto_inmortalidad",
                "life_campfire", "fuel_campfire", "special_totem", "cristal_hielo", "arco_hielo",
                "happy_ghast_enchant", "tarta_calabaza_mejorada", "bar_tequila", "bar_margarita",
                "bar_mezcal", "bar_pulque", "bar_cerveza", "bar_ron", "bar_vodka", "bar_whisky",
                "bar_sake", "bar_ginebra", "bar_azulito", "bar_michelada", "manzana_vida", "pluma_levitacion",
        };
        for (String item : items) {
            registeredItems.add(item);
        }
    }

    public ItemStack getItem(String itemName, int cantidad, Player target) {
        return getItem(itemName, cantidad, target, -1);
    }

    public ItemStack getItem(String itemName, int cantidad, Player target, int usosEspeciales) {
        ItemStack item = null;

        switch (itemName.toLowerCase()) {
            case "doubletotem": item = doubleLifeTotem.createDoubleLifeTotem(); break;
            case "corrupted_steak": item = DayOneChanges.corruptedSteak(); break;
            case "corrupted_golden_apple": item = CorruptedGoldenApple.createCorruptedGoldenApple(); break;
            case "libro_habilidades": item = HabilidadesBook.createHabilidadesBook(); break;
            case "dinocoins": item = EconomyItems.createVithiumCoin(); break;
            case "dinofichas": item = EconomyItems.createVithiumToken(); break;
            case "blood_fragment": item = EconomyItems.createBloodFragment(); break;
            case "mochila_nivel_1": item = EconomyItems.createNormalMochila(); break;
            case "mochila_nivel_2": item = EconomyItems.createGreenMochila(); break;
            case "mochila_nivel_3": item = EconomyItems.createRedMochila(); break;
            case "mochila_nivel_4": item = EconomyItems.createBlueMochila(); break;
            case "mochila_nivel_5": item = EconomyItems.createPurpleMochila(); break;
            case "enderbag": item = EconomyItems.createEnderBag(); break;
            case "gancho": item = EconomyItems.createGancho(); break;
            case "panic_apple": item = EconomyItems.createManzanaPanico(); break;
            case "artefacto_nivel_1": item = EconomyItems.createYunqueReparadorNivel1(); break;
            case "artefacto_nivel_2": item = EconomyItems.createYunqueReparadorNivel2(); break;
            case "misiones": item = Misionesitem.createMisiones(); break;
            case "icetotem": item = economyIceTotem.createIceTotem(); break;
            case "flytotem": item = economyFlyTotem.createFlyTotem(); break;
            case "excavator_pickaxe": item = ExcavatorItem.createExcavator(); break;
            case "potion_resistance_2": item = CustomPotions.getResistanceIIPotion(); break;
            case "splash_resistance_3": item = CustomPotions.getSplashResistanceIIIPotion(); break;
            case "potion_slow_falling": item = CustomPotions.getSlowFallingPotion(); break;
            case "splash_regeneration_3": item = CustomPotions.getSplashRegenerationIIIPotion(); break;
            case "potion_haste_3": item = CustomPotions.getHasteIIIPotion(); break;
            case "potion_haste_2": item = CustomPotions.getHasteIIPotion(); break;
            case "splash_absorption_10": item = CustomPotions.getSplashAbsorptionXPotion(); break;
            case "frasco_de_velocidad": item = CustomPotions.getSpeedHoneyBottle(); break;
            case "amulet_bloodmoon": item = amuletBloodM.createAmulet(); break;
            case "amuleto_inmortalidad": item = amuletInmortal.createAmulet(); break;
            case "life_campfire": item = lifeCampfire.createCampfire(); break;
            case "fuel_campfire": item = lifeCampfire.createFuel(); break;
            case "special_totem": item = ItemsTotems.createSpecialTotem(); break;
            case "cristal_hielo": item = ItemsTotems.createIceCrystal(); break;
            case "arco_hielo": item = iceBowItem.createIceBow(); break;
            case "happy_ghast_enchant": item = happyGhastEnchant.createFastFlightBook(1); break;
            case "tarta_calabaza_mejorada": item = DayOneChanges.improvedPumpkinPie(); break;
            case "bar_tequila": item = CustomPotions.getTequila(); break;
            case "bar_margarita": item = CustomPotions.getMargarita(); break;
            case "bar_mezcal": item = CustomPotions.getMezcal(); break;
            case "bar_pulque": item = CustomPotions.getPulque(); break;
            case "bar_cerveza": item = CustomPotions.getBeer(); break;
            case "bar_ron": item = CustomPotions.getRum(); break;
            case "bar_vodka": item = CustomPotions.getVodka(); break;
            case "bar_whisky": item = CustomPotions.getWhisky(); break;
            case "bar_sake": item = CustomPotions.getSake(); break;
            case "bar_ginebra": item = CustomPotions.getGin(); break;
            case "bar_azulito": item = CustomPotions.getAzulito(); break;
            case "bar_michelada": item = CustomPotions.getMichelada(); break;
            case "manzana_vida": item = itemsEventos.createManzanaVida(); break;
            case "pluma_levitacion": item = itemsEventos.createPlumaLevitacion(); break;
            default: return null;
        }

        if (item != null) {
            item.setAmount(cantidad);
        }
        return item;
    }

    public List<String> getRegisteredItems() {
        return registeredItems;
    }
}