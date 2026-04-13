# STASH

STASH is a Spigot plugin that provides players with personal, persistent vault storage and gives admins full GUI-based management for vault data, audits, and backups.

## Status

**v1.0.0 is production-ready** with YAML persistence, player vault management, and a complete admin control center.

## Core Features

- **Personal Vault Selector GUI**
  - `/pv` opens a vault browser GUI
  - Left-click opens a vault page
  - Right-click opens manage view for vault metadata
- **Vault Manage GUI**
  - Rename vault display name
  - Change vault icon material
  - Change vault description
  - Word-wrapped descriptions in lore (30-char soft wrap, no word splitting)
- **Persistent Storage**
  - Per-player, per-page storage in `vaults.yml`
  - Slot-indexed persistence preserves exact item positions
  - Save on close and plugin disable
- **Vault Controls**
  - Lock/unlock vault pages
  - Clear page with confirmation flow
  - Permission-aware access checks
- **GUI Navigation + UX**
  - In-vault control row with navigation and controls
  - Non-interactive placeholder panes in control row edge slots
- **Admin Control Center**
  - `/stash` opens admin GUI
  - Player browser, profile controls, read-only inspect
  - Backup explorer, audit explorer, system actions
  - GUI confirmation dialog for destructive/admin actions
- **Audit + Recovery**
  - Audit event logging in `audits.yml`
  - Paged and filtered audit viewing
  - CSV export support
  - Backup creation, preview, confirm restore, rollback latest/history

## Commands

### Player

- `/pv` - open personal vault selector GUI
- `/pv <page>` - open a specific vault page directly
- `/pv list` - open vault selector GUI
- `/pv search <query>` - search vault pages by item/material
- `/pv lock <page>` / `/pv unlock <page>` - toggle page lock
- `/pv clear <page>` + `/pv confirmclear` - clear page with confirmation

### Admin

- `/stash` - open admin control center GUI
- `/stash reload`
- `/stash backup`
- `/stash backups`
- `/stash previewrestore <backup-file>`
- `/stash confirmrestore`
- `/stash restore <backup-file>`
- `/stash rollback <history|latest>`
- `/stash audit [page] [limit] [actor|*] [action|*]`
- `/stash audit export [actor|*] [action|*]`

## Permissions

- `stash.use`
- `stash.command`
- `stash.reload`
- `stash.admin`
- `stash.admin.inspect`
- `stash.admin.restore`
- `stash.admin.audit`
- `stash.admin.bypass`
- `stash.pages.*`
- `stash.size.*`

## Configuration

`config.yml` includes:

- `vault.default-pages`
- `vault.max-pages` (default `54`)
- `vault.default-rows` (default `5`, max `5`)
- open-sound settings
- audit retention settings

All user-facing output is centralized in `messages.yml`.

## Build

```bash
mvn clean package
```

Output jar:

- `target/Stash-1.0.0.jar`

## License

MIT License (see `LICENSE`).
