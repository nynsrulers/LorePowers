package com.nynsrulers.lorepowers;

public enum Power {
    VOID_TOTEMS("Void Totems"),
    BEE_FLIGHT("Bee Flight"),
    SPEED_MINE("Speed Mine"),
    PERMANENT_ELYTRA("Permanent Elytra"),
    GLITCHED_PRESENCE("Glitched Presence"),
    DRAGON_FORM("Dragon Form"),
    //DRAGON_FORM_CARNAGE("Dragon Form Carnage"),
    //SPECTER_VANISH("Specter Vanish"),
    MAP_WARP("Map Warp"),
    PIGLIN_AVIAN_TRAITS("Piglin-Avian Traits"),
    HEAT_RESISTANCE("Heat Resistance"),
    PIGLIN_AID("Piglin Aid"),
    ANKLE_BITER("Ankle Biter"),
    FIRE_BREATH("Fire Breath"),
    PICK_UP("Pick-Me-Up"),
    //DRAGON_FORM_WEAPON_BLOCK("Dragon Form Weapon Block"),
    //DRAGON_FORM_FIRE_BUFF("Dragon Form Fire Buff"),
    //DRAGON_FORM_SPIKES("Dragon Form Spikes"),
    VILLAGERS_RESPECT("Villager's Respect"),
    FOX_MAGIC("Fox's Magic"),
    POTATO_RULER("Potato Ruler"),
    BESTSPARKS_IDEA("BestSpark's Idea"),
    PEARL_LINK("Pearl Link"),
    LIGHT_WEIGHT("Light Weight");

    private final String name;

    Power(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
