package me._w41k3r.shopkeepersAddon.GlobalShopGui;

public enum MenuType {
    MAIN_MENU("Main Menu"),
    PLAYER_SHOP("Player Shop"),
    ADMIN_SHOP("Admin Shop"),
    REMOTE_ADMIN_SHOP("Remote Admin Shop"),
    ITEM_SHOP("Item Shop"),
    SHOPKEEPERS("Shopkeepers"),
    PLAYER_SHOPS("Player Shops");
    private final String text;

    MenuType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
