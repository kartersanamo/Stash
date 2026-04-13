package com.kartersanamo.stash.api.gui;

import java.util.UUID;

public record OpenVaultContext(UUID owner, int page, int rows, String title) {
}
