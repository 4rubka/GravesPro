package me.gemini.gravespro.api;

import me.gemini.gravespro.model.DeathSnapshot;
import me.gemini.gravespro.model.Grave;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {
    
    CompletableFuture<Void> init();
    
    CompletableFuture<Void> saveGrave(Grave grave);
    CompletableFuture<Void> deleteGrave(UUID id);
    CompletableFuture<List<Grave>> loadAllGraves();
    
    CompletableFuture<Void> saveSnapshot(DeathSnapshot snapshot);
    CompletableFuture<List<DeathSnapshot>> loadSnapshots(UUID playerId);
    
    CompletableFuture<Void> close();
}
