# Changelog 2.25.0-260212

## 🚀 Vault Economy Migration
- **Native Vault Integration**: Replaced the entire command-based economy system with direct Vault API calls.
- **Soft Dependency**: Vault is now a soft dependency. If missing, economy features are gracefully disabled.
- **Performance**: Removed the overhead of parsing console commands for every balance check.

## 🛡️ Enhanced Trade Handling
- **Post-Trade Balance Check**: Implemented logic to re-evaluate affordability immediately after a shift-click trade.
- **Slot Invalidation**: If a player (or shop owner) runs out of funds during a bulk trade, the trade slot is automatically cleared or not replenished to prevent invalid transactions.

## 🧹 Configuration Cleanup
- **Simplified Config**: Removed `economy.commands`, `economy.balance-placeholder`, `buy-settings`, and `sell-settings`.
- **Enforced Defaults**: Balance checks and costs/payouts are now always enabled by default in the code.
- **Config Version 1.3**: Bumped `config-version` to 1.3 to ensure proper migration.

# Changelog 2.25.0-260211

## 🚀 Universal Economy Integration
- **Command-Based Economy**: Implemented a universal economy system that works with *any* economy plugin (Essentials, CMI, Vault, etc.) by executing console commands and parsing output.
- **Zero Dependencies**: Removed hard dependency on Vault API.
- **Console Capture**: Created a custom `ConsoleCapture` system to intercept command output for balance checking.
- **Configuration**: Added comprehensive `economy` section to `config.yml` for defining commands, regex patterns, and transaction settings.

## 🛡️ Strict Balance Checks
- **Trade Selection**: Validates player balance (buying) and shop owner balance (selling) immediately when a trade is selected.
- **Pre-Trade Verification**: Re-checks balance right before transaction execution to prevent race conditions.
- **Post-Trade UI Update**: Automatically refreshes the trade UI to remove items if the player/owner can no longer afford the trade.
- **Owner Insolvency Protection**: Prevents selling to player shops if the owner lacks funds (unless configured otherwise).

## 🐛 Critical Bug Fixes
- **Recursion Guard**: Fixed `Recursive call to appender` errors in `ConsoleCapture` by implementing a `ThreadLocal` guard.
- **ANSI Parsing Fix**: Fixed incorrect balance parsing (e.g., `336.0`) by stripping ANSI color codes from console output before parsing.
- **Heuristic Parser**: Improved auto-detection logic to scan log lines in reverse, prioritizing the most recent output.

## 📦 Maintenance
- **API Update**: Updated `Shopkeepers` API dependency to `v2.25.0`.
- **Version Bump**: Project version updated to `2.25.0.1`.
