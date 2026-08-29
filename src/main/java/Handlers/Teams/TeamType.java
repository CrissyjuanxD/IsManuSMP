package Handlers.Teams;

import net.md_5.bungee.api.ChatColor;

public enum TeamType {
    // Orden: ID_INTERNO, HEX, PREFIJO_CHAT/CABEZA, PREFIJO_TAB, COLOR_BUKKIT, PRIORIDAD_TAB, NOMBRE_AMIGABLE (Para la Scoreboard)

    ADMIN("Admin", "#ff935f",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.GOLD + ChatColor.BOLD + "HOKAGE" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.of("#ff935f") + ChatColor.BOLD + "HOK" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.GOLD, "01_Admin", "Hokage"),

    MOD("Mod", "#00BFFF",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.AQUA + ChatColor.BOLD + "ANBU" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.of("#00BFFF") + ChatColor.BOLD + "ANB" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.AQUA, "02_Mod", "Anbu"),

    T_HELPER("Helper", "#67E590",
            "\uEB89 ",
            "\uEB92 ",
            org.bukkit.ChatColor.GREEN, "03_Helper", "Helper"),

    T_SURVIVOR("TSurvivor", "#9455ED",
            "\uEB8A ",
            "\uEB8F ",
            org.bukkit.ChatColor.LIGHT_PURPLE, "04_TSurvivor", "Survivor"),

    // Nuevo Aldeano+ usando los colores de DinoNugget+
    Y_MIEMBRO("YMiembro", "#F7A1F0",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ALDEANO" + ChatColor.GOLD + ChatColor.BOLD + "+" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ALD" + ChatColor.GOLD + ChatColor.BOLD + "+" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.LIGHT_PURPLE, "98_Miembro", "Aldeano+"),

    Z_MIEMBRO("ZMiembro", "#ffa39d",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.of("#ffa39d") + ChatColor.BOLD + "ALDEANO" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.of("#ffa39d") + ChatColor.BOLD + "ALD" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.RED, "99_Miembro", "Aldeano"),

    LAVACLASH("LavaClash", "#FFD294",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.GOLD + ChatColor.BOLD + "Lava" + ChatColor.YELLOW + ChatColor.BOLD + "Clash" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.GOLD + ChatColor.BOLD + "Lv" + ChatColor.YELLOW + ChatColor.BOLD + "C" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.DARK_AQUA, "05_LavaClash", "LavaClash"),

    ITEMPARTY("Itemparty", "#C056E6",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_AQUA + ChatColor.BOLD + "ITEM" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "PARTY" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_AQUA + ChatColor.BOLD + "I" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "PT" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.DARK_PURPLE, "06_Itemparty", "ItemParty"),

    HOTPOTATO("HotPotato", "#FCA37D",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_RED + ChatColor.BOLD + "HOT" + ChatColor.RED + ChatColor.BOLD + "POTATO" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_RED + ChatColor.BOLD + "H" + ChatColor.RED + ChatColor.BOLD + "PO" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.GOLD, "07_HotPotato", "HotPotato"),

    BUILDBATTLE("buildbattle", "#C056E6",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "BUILD" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "BATTLE" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            ChatColor.GRAY + "" + ChatColor.BOLD + "[" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "B" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "BT" + ChatColor.GRAY + ChatColor.BOLD + "] ",
            org.bukkit.ChatColor.DARK_PURPLE, "08_buildbattle", "BuildBattle"),

    Z_FANTASMA("ZFantasma", "#555555",
            "\uEB8C ",
            "\uEB91 ",
            org.bukkit.ChatColor.DARK_GRAY, "99_Fantasma", "Fantasma");

    private final String id;
    private final String hexColor;
    private final String chatPrefix;
    private final String tabPrefix;
    private final org.bukkit.ChatColor bukkitColor;
    private final String priority;
    private final String displayName; // Nuevo campo

    TeamType(String id, String hexColor, String chatPrefix, String tabPrefix, org.bukkit.ChatColor bukkitColor, String priority, String displayName) {
        this.id = id;
        this.hexColor = hexColor;
        this.chatPrefix = chatPrefix;
        this.tabPrefix = tabPrefix;
        this.bukkitColor = bukkitColor;
        this.priority = priority;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public ChatColor getBungeeColor() { return ChatColor.of(hexColor); }
    public String getChatPrefix() { return chatPrefix; }
    public String getTabPrefix() { return tabPrefix; }
    public org.bukkit.ChatColor getBukkitColor() { return bukkitColor; }
    public String getPriority() { return priority; }
    public String getDisplayName() { return displayName; }

    public static TeamType getById(String id) {
        for (TeamType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) return type;
        }
        return null;
    }
}