
Credits:
Major Credits go to blablubbabc for making the amazing Shopkeepers plugin.

Disclaimer:
This plugin is NOT a full shopkeepers plugin, this ONLY adds additional features to the parent plugin.
If you are using Shopkeepers then this plugin shall add sorting and easier navigation to the shopkeepers.

What does this plugin do?

Navigation Addon:
It will sort them in 3 type:
1. Player shops - will display all individual player heads and shops they own
2. Item shop - will display all available trades along with villagers that are selling
3. Admin shop - all available admin shops
   On clicking on the desired trade, it will teleport you to the shopkeeper/plot(if using plotsquared)

Economy Addon:
It will allow you to do trades with virtual currencies(vault api).
You will need a Economy plugin for this feature e.g. CMI, EssentialsX. As well as Vault and ProtocolLib.
Setting up currency based trades are as simple as setting up normal shopkeeper trades.
In the editor menu on adding a result item a paper will automatically appear on the cost slot. On clicking the paper you will be prompted to enter the price in the chat. Enter the price and you first economy based shopkeeper trade is done.

Requirements:
[QUOTE]
1. Shopkeepers
2. ProtocolLib
3. Vault
4. Any Economy Plugin
3. PlotSquared latest version. (optional)[/QUOTE][QUOTE]
   [/QUOTE]

How to Install:

1. Click download button, which shall download a jar file. Now put the file inside the plugins folder on your server along with Shopkeepers plugin and PlotSquared(optional).

2. Restart the server.

3. To access the shop gui use the command /shops and to allow other players to use the command give them the permission 'SNA.command.shops'.

NOTE: This plugin integrates with plotsquared if its installed in your server to directly teleport the player to the shop's plot instead of teleporting the player to the villager.
[SPOILER='config.yml']
[code=YAML]
#Set this to false if you don't want players to get teleported on clicking the shopkeeper in the gui.
AllowTeleportToShopkeepers: true
EconomyHook:
#Setting this to true allows shopkeepers to trade using virtual-currency(Vault Api)
#This requires protocolLib and Vault to be installed
#Requires server restart
Enabled: true
#Enabling this will prevent players from setting up item to item based trades.
BlockItemBasedTrades: false
#Set the price limit for trades.
SellingPriceLimit: 100000000000000.00
#Enable this if bedrock players play in you server(Geyser plugin)
#Note: you need to have a prefix before the bedrock player's name for this to work(you need to have FloodGate Installed to change bedrock prefix).
Geyser-Compat:
Enabled: false
Name-Prefix: 'B_'
[/code]
[/SPOILER]

[SPOILER="messages.yml"][code=YAML]
Supply-Chest-Is-Full: '§cNo space left in the supplies.'
Owner-Add-Money: '§a+ [amount]'
Owner-Subtract-Money: '§a- [amount]'
Shop-Out-Of-Money: '§cThis shop is out of money.'
Player-Out-Of-Money: '§cYou dont have enough money to buy the item.'
Shop-Out-Of-Stock: '§cOut of Stock.'
Invalid-Price-Input: '§cYour trade setup was cancelled due to invalid price input.'
Price-Limit-Reached: '§cYour trade setup was cancelled due to invalid price input. Highest price limit is [maxPrice]'
Price-Set-Successfully: '§aItem price successfully set.'
Price-Input-Request: '§aEnter the price of the item. Type cancel to cancel the setup.'
Trade-Setup-Cancel: '§cYour trade setup was cancelled.'
Player-Inventory-Full: '§cYour inventory is full please clear it before purchasing.'
Currency-Item:
Name-Format: 'Price [amount]'
Material: 'PAPER'
CustomModelData: 0
Lore:
- '§bClick to edit'
[/code]
[/SPOILER]

[SPOILER='permissions']
[code]permissions:
SNA.command.shops:
description: allows /shops or /sna shops command
default: op
SNA.command.shop:
description: allows /shop or /sna shop command
default: op
SNA.command.playershops:
description: allows /playershops or /sna playershops command
default: op
SNA.teleport:
description: teleport on click
default: op
SNA.command.help:
description: show /sna help message.
default: op
SNA.admin:
description: allows /sna reload command.
default: op
[/code]
[/SPOILER]

[SPOILER='commands']
[code]commands:
shops:
description: shop
usage: '/<command>'
playershops:
description: shop gui withoud admin shop icon
usage: '/<command>'
shop:
description: opens admin shops gui with remote access
usage: '/<command>'
sna:
description: parent command
usage: '/<command> [help, reload, playershops, shop, shops]'
[/code]
[/SPOILER]

[SPOILER='Navigation Screenshots']










[/SPOILER]

[SPOILER='Economy Screenshots']







[/SPOILER]

Get support:


Recommended Hosting Provider:
