package dev.MrFlyn.shopkeeperNavAddon.GlobalShopGui;

public enum MenuType {
    MAIN_MENU("Main Menu"),
    PLAYER_SHOP("Player Shop"),
    ADMIN_SHOP("Admin Shop"),
    ITEM_SHOP("Item Shop"),
    SHOPKEEPERS("Shopkeepers");
    private final String text;

    MenuType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
