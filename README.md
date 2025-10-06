

# ShopkeepersAddon (Fork) – Improved Price Input Handling

**✅ This fork fixes a critical issue with price editing in the original ShopkeepersAddon.**  
In the official version, entering a price in chat often results in the message “Invalid Price!” because many servers use chat plugins (e.g. ChatColor2, TAB, LPC, FreedomChat) that inject formatting or color codes into messages.  

This fork updates the price parsing logic to:  
- Remove color codes and formatting from chat inputs  
- Accept both `10`, `10.00`, and `10,00` formats  
- Support US and EU number styles  
- Prevent `NumberFormatException` crashes  
- Properly unregister the listener on the main thread  

**Only the `SetPriceTask` class was modified! The rest of the plugin remains unchanged.**  

![banner](https://cdn.modrinth.com/data/cached_images/790af409b42cc8c9cd15b8d933a1dc2addf9ffd8.png)
## 🚀 **Transform Your Server's Economy!**

> Credits:
> Major Credits go to blablubbabc for making the amazing Shopkeepers plugin.

> Disclaimer:
> This plugin is NOT a full shopkeepers plugin, this ONLY adds additional features to the parent plugin.
> If you are using Shopkeepers then this plugin shall add sorting and easier navigation to the shopkeepers.

ShopkeepersAddon supercharges the popular **Shopkeepers** plugin with advanced economy features, beautiful GUI navigation, and powerful player shop management!

---

## ✨ **Features Overview**

| Feature | Description | Status |
|---------|-------------|--------|
| 💰 **Advanced Economy** | Vault integration with custom price formatting | ✅ |
| 🎯 **Smart GUI** | Intuitive menus for shop browsing | ✅ |
| 👥 **Player Shops** | Complete player shop management system | ✅ |
| ⚡ **Bulk Trading** | Optimized performance for mass trades | ✅ |
| 🌍 **Multi-Currency** | Custom symbols and international formats | ✅ |

---

## 📚 **Documentation & Guides**

### 🔧 **Setup & Configuration**
- **[📖 Full Wiki](https://github.com/the-w41k3r/ShopkeepersAddon/wiki)** - Complete documentation
- **[⚡ Quick Start](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Installation)** - Get running in 5 minutes
- **[⚙️ Configuration](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Configuration)** - Detailed settings guide

### 🎮 **User Guides**
- **[⌨️ Commands & Permissions](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Commands-&-Permissions)** - Complete command reference
- **[💰 Economy Features](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Economy-Features)** - Money system guide
- **[🖥️ GUI Navigation](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/GUI-Navigation)** - Interface tutorial

### 👥 **Shop Management**
- **[👤 Player Shops](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Player-Shops)** - Player shop setup
- **[⚡ Admin Shops](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Admin-Shops)** - Admin tools guide
- **[🔧 Troubleshooting](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Troubleshooting)** - Fix common issues

---

## 🎯 **Quick Links**

### 📥 **Download**
- **[✨ SpigotMC](https://www.spigotmc.org/resources/your-resource-id)** - Primary download
- **[🌿 Modrinth](https://modrinth.com/plugin/shopkeepersaddon)** - Alternative source
- **[🐙 GitHub Releases](https://github.com/the-w41k3r/ShopkeepersAddon/releases)** - Latest versions

### 💬 **Community & Support**
- **[💬 Discord Support](https://discord.gg/bnfhjAtGYU)** - Live help & community
- **[🐛 Bug Reports](https://github.com/the-w41k3r/ShopkeepersAddon/discussions)** - Report issues
- **[💡 Feature Requests](https://github.com/the-w41k3r/ShopkeepersAddon/issues)** - Suggest ideas

### 🔧 **Developer Resources**
- **[🔄 Update Log](https://github.com/the-w41k3r/ShopkeepersAddon/releases)** - Version history
- **[❤️ Donate](https://paypal.me/thew41k3r)** - Support development

---

## 🚀 **Get Started Now!**

### **Prerequisites**
- **[Shopkeepers](https://www.spigotmc.org/resources/shopkeepers.80756/)** (v2.23.9+)
- **[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)** (v5.1.0+)

### **Installation**
1. **Download** the latest JAR from [Releases](https://github.com/the-w41k3r/ShopkeepersAddon/releases)
2. **Place** in your `plugins` folder
3. **Restart** your server
4. **Configure** via [`config.yml`](https://github.com/the-w41k3r/ShopkeepersAddon/wiki/Configuration)
5. **Use** `/shops` to open the menu!

---

## 🏆 **Why Choose ShopkeepersAddon?**

| Aspect | Benefit |
|--------|---------|
| **Performance** | Async processing & bulk trade optimization |
| **Customization** | Complete control over economy formatting |
| **User Experience** | Beautiful, intuitive GUI interfaces |
| **Reliability** | Regular updates & active support |
| **Compatibility** | Works with all Vault economy plugins |

---

## 📊 **Plugin Statistics**

- **✅ 100% Shopkeepers Compatible**
- **⚡ Handles 1000+ Shops Efficiently**
- **🌍 Supports International Number Formats**
- **🔧 Highly Configurable & Modular**

---

## 🤝 **Support the Project**

Love ShopkeepersAddon? Here's how you can help:

- ⭐ **Star** the [GitHub Repository](https://github.com/the-w41k3r/ShopkeepersAddon)
- 📝 **Leave a Review** on SpigotMC/Modrinth
- 🔄 **Share** with other server owners
- 💖 **Donate** to support development

---

## 📞 **Contact & Links**

- **Developer**: _w41k3r
- **Source Code**: [GitHub Repository](https://github.com/the-w41k3r/ShopkeepersAddon)
- **Issue Tracker**: [GitHub Issues](https://github.com/the-w41k3r/ShopkeepersAddon/issues)
- **License**: MIT - Free for commercial use

---

