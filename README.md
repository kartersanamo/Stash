# STASH

**STASH** is a Bukkit/Spigot plugin roadmap for a player storage and vault system with a strong GUI-first experience.

Right now, the repository starts as a minimal plugin skeleton. This README describes the **planned** direction for the project: a polished personal vault system with admin controls, scalable persistence, and a feature set that can grow into a full server storage platform.

## Vision

STASH is intended to be more than a simple `/pv` plugin.
It should become a **complete storage ecosystem** for Minecraft servers:

- Personal vaults for every player
- Configurable vault tiers by permission
- Inventory GUI navigation with clean UX
- Persistent per-player storage
- Admin recovery, audit, and moderation tools
- Optional shared vaults, team storage, and network-grade infrastructure

The goal is to keep the plugin easy to use for players while giving administrators an absurd amount of control under the hood.

## Design Goals

- **Fast for players**: open, move, store, and retrieve items with minimal friction
- **Safe for servers**: no item loss, versioned persistence, backup-friendly storage
- **Expandable**: support small survival servers and large multi-group networks
- **Readable**: clean commands, clean menus, clean storage rules
- **Powerful**: rich admin tooling, analytics, recovery, and inspection features

## Planned Core Features

### 1) Personal Vaults

- `/pv [num]` opens a player’s vault GUI
- Vault pages are unlocked by permission tier
- Vault size expands based on rank, permissions, or config rules
- Item layouts are saved per player, per vault page
- Optional lock states and access restrictions

### 2) GUI-Driven Storage Management

- Inventory-based menus for vault browsing
- Page navigation with next / previous controls
- Search, filter, and sort views for large vault sets
- Confirm dialogs for destructive actions
- Sound effects and visual feedback for actions
- Custom menu themes and item icons for different vault states

### 3) Persistence Layer

- Per-player storage saved to YAML at first, with room for database support later
- Safe serialization for item stacks, metadata, enchantments, lore, and NBT-like data
- Versioned storage format with migration support
- Autosave, flush-on-disable, and crash recovery planning
- Optional backup snapshots for rollback and support cases

### 4) Admin Toolkit

- Inspect any player’s vaults
- Restore deleted or corrupted inventory pages
- Audit item movement history
- Track who opened, modified, withdrew, or deposited items
- Force-lock or force-unlock vaults
- Export and import vault data for support or migration

## “Insane” Expansion Ideas

These are intentionally bigger-than-basic ideas for making STASH feel like a premium storage platform:

### Smart Storage Intelligence

- Automatic stacking and compaction rules
- Duplicate item detection
- Item analytics: most stored materials, value estimates, rarity summaries
- Smart suggestions for where items should be deposited
- Quick-transfer of matching item groups across pages

### Searchable Item Index

- Search vaults by material
- Search by item name, lore, enchantments, or tags
- Advanced filter combinations
- Fuzzy lookup for players with huge vault histories
- Index-based searches so large storage sets stay fast

### Shared and Social Vaults

- Guild/team/clan vaults
- Party storage
- Temporary raid vaults
- Event reward vaults
- Shared permission rules and role-based access

### Cross-Server / Network Vision

- Storage sync across multiple servers
- Central data backend for network-wide vaults
- Proxy-aware design for future velocity/bungee-style support
- Server-aware logs and per-server item origin tracking

### Safety and Recovery Features

- Full audit trail for every vault action
- Snapshot-based rollback
- Anti-dupe forensic mode for suspicious item chains
- Immutable log records for moderation support
- Corruption detection and repair tooling

### Cosmetic / Quality-of-Life Features

- Vault themes
- Animated GUI transitions
- Custom open/close sounds
- Player-specific menu preferences
- Hotbar shortcuts for frequently used actions
- Pinning favorite vault pages

## Planned Commands

> These commands are roadmap targets and may evolve as the plugin grows.

| Command                      | Description                      |
|------------------------------|----------------------------------|
| `/pv [num]`                  | Open a personal vault page       |
| `/pv list`                   | List available vaults            |
| `/pv search <query>`         | Search stored items              |
| `/pv rename <name>`          | Rename a vault                   |
| `/pv clear <name>`           | Clears a vault of items          |
| `/pv admin inspect <player>` | Inspect another player’s storage |

## Planned Permission Structure

A flexible permission model is planned so servers can scale vault access cleanly.

### Base Access

- `stash.use` — open and use personal vaults
- `stash.command` — use main STASH commands
- `stash.page.<n>` — unlock specific page counts

### Premium / Tiered Access

- `stash.pages.<n>` — grant a certain number of vault pages
- `stash.size.<n>` — expand inventory capacity
- `stash.shared` — access shared vault systems
- `stash.theme.*` — allow custom GUI themes

### Administrative Access

- `stash.admin` — access admin tools
- `stash.admin.inspect` — inspect other players
- `stash.admin.restore` — restore snapshots
- `stash.admin.audit` — view storage history
- `stash.admin.bypass` — bypass restrictions when required

## Planned Storage Model

The data model is expected to revolve around a few simple, versioned concepts:

- **Player Profile** — UUID, name, tier, settings, access flags
- **Vault** — owner, type, size, lock state, permissions
- **Vault Page** — page index, serialized contents, metadata
- **Item Snapshot** — item stack, source, timestamp, action type
- **Audit Event** — who changed what, when, and why
- **Schema Version** — migration and compatibility tracking

This should allow the plugin to evolve without breaking old player data.

## Planned Architecture

STASH should follow a clean plugin layout similar to a standard Bukkit project:

- `main` plugin class for lifecycle control
- command handlers for user/admin actions
- GUI manager for menus, button actions, and navigation
- storage service for serialization and persistence
- config layer for vault tiers, sounds, messages, and limits
- audit layer for logging and recovery

The implementation should favor small, focused classes and clear service boundaries.

## Planned Configuration

The config should be server-friendly and easy to tune.

Possible settings:

- vault page counts by permission group
- default inventory size
- menu titles and sounds
- lock rules and access control
- autosave intervals
- logging verbosity
- backup frequency
- shared vault limits
- migration toggles

## Reliability Targets

STASH should be built with data safety as a first-class goal.

- No inventory loss on reload or shutdown
- Safe async persistence boundaries
- Graceful fallback if storage fails
- Backups before destructive migration steps
- Versioned serialization for future compatibility
- Strong validation of item data before save/load

## Testing Plan

Before the plugin is considered production-ready, the roadmap should include:

- serialization/deserialization tests
- permission boundary tests
- GUI navigation tests
- page expansion and overflow tests
- migration tests for old vault data
- recovery tests for corrupted records
- stress testing with many players and large vaults

## Roadmap

### Phase 1 — Foundation

- Minimal plugin bootstrap
- `/pv` command
- Basic GUI vault pages
- Per-player persistence
- Tier-based vault sizing

### Phase 2 — Quality of Life

- Search, sort, and pagination
- Sounds and polish
- Vault locking
- Better config system
- Admin inspection tools

### Phase 3 — Advanced Storage

- Shared vaults
- Audit logs
- Restore snapshots
- Advanced filters
- Backup and migration support

### Phase 4 — Network-Grade STASH

- Cross-server synchronization
- Central storage backend
- Smart analytics
- Forensic auditing
- Full recovery toolkit

## Status

This project currently begins as a lightweight Bukkit plugin skeleton.
The README above describes the intended roadmap for turning it into a fully featured vault and storage platform.

## License

This project is licensed under the MIT License. See LICENSE.
