package me.gemini.gravespro.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeathSnapshot {
    private UUID id;
    private UUID playerId;
    private String playerName;
    private Location location;
    private long timestamp;
    private List<ItemStack> inventory;
    private List<ItemStack> armor;
    private ItemStack offHand;
    private int experience;
    private String deathCause;
}
