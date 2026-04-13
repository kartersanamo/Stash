package com.kartersanamo.stash.api.gui;

import java.util.UUID;

public record OpenVaultContext(UUID owner, String ownerName, int page, int maxPages, int rows, String title,
                               boolean readOnly) {
}
