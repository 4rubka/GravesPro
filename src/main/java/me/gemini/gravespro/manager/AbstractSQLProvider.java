package me.gemini.gravespro.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.gemini.gravespro.GravesPro;
import me.gemini.gravespro.api.StorageProvider;
import me.gemini.gravespro.model.DeathSnapshot;
import me.gemini.gravespro.model.Grave;
import me.gemini.gravespro.model.GraveState;
import me.gemini.gravespro.util.ItemSerialization;
import me.gemini.gravespro.util.LocationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public abstract class AbstractSQLProvider implements StorageProvider {

    protected final GravesPro plugin;
    protected HikariDataSource dataSource;

    public AbstractSQLProvider(GravesPro plugin) {
        this.plugin = plugin;
    }

    protected abstract HikariConfig createConfig();

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            this.dataSource = new HikariDataSource(createConfig());
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Graves table
                stmt.execute("CREATE TABLE IF NOT EXISTS gp_graves (" +
                        "id VARCHAR(36) PRIMARY KEY," +
                        "owner_id VARCHAR(36)," +
                        "owner_name VARCHAR(16)," +
                        "location TEXT," +
                        "creation_time BIGINT," +
                        "expiration_time BIGINT," +
                        "state VARCHAR(20)," +
                        "items TEXT," +
                        "experience INT," +
                        "death_cause TEXT" +
                        ")");

                // Snapshots table
                stmt.execute("CREATE TABLE IF NOT EXISTS gp_snapshots (" +
                        "id VARCHAR(36) PRIMARY KEY," +
                        "player_id VARCHAR(36)," +
                        "player_name VARCHAR(16)," +
                        "location TEXT," +
                        "timestamp BIGINT," +
                        "inventory TEXT," +
                        "armor TEXT," +
                        "offhand TEXT," +
                        "experience INT," +
                        "death_cause TEXT" +
                        ")");
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize database tables!", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveGrave(Grave grave) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO gp_graves (id, owner_id, owner_name, location, creation_time, expiration_time, state, items, experience, death_cause) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, grave.getId().toString());
                ps.setString(2, grave.getOwnerId().toString());
                ps.setString(3, grave.getOwnerName());
                ps.setString(4, LocationUtil.serializeLocation(grave.getLocation()));
                ps.setLong(5, grave.getCreationTime());
                ps.setLong(6, grave.getExpirationTime());
                ps.setString(7, grave.getState().name());
                String serializedItems = ItemSerialization.serializeItemMap(grave.getItems());
                ps.setString(8, serializedItems);
                ps.setInt(9, grave.getExperience());
                ps.setString(10, grave.getDeathCause());
                ps.executeUpdate();
                plugin.getLogger().info("Successfully saved grave " + grave.getId() + " for " + grave.getOwnerName() + " (" + grave.getItems().size() + " slots)");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "CRITICAL: Failed to save grave " + grave.getId() + " to database!", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteGrave(UUID id) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM gp_graves WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete grave " + id, e);
            }
        });
    }

    @Override
    public CompletableFuture<List<Grave>> loadAllGraves() {
        return CompletableFuture.supplyAsync(() -> {
            List<Grave> graves = new ArrayList<>();
            String sql = "SELECT * FROM gp_graves";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    graves.add(Grave.builder()
                            .id(UUID.fromString(rs.getString("id")))
                            .ownerId(UUID.fromString(rs.getString("owner_id")))
                            .ownerName(rs.getString("owner_name"))
                            .location(LocationUtil.deserializeLocation(rs.getString("location")))
                            .creationTime(rs.getLong("creation_time"))
                            .expirationTime(rs.getLong("expiration_time"))
                            .state(GraveState.valueOf(rs.getString("state")))
                            .items(ItemSerialization.deserializeItemMap(rs.getString("items")))
                            .experience(rs.getInt("experience"))
                            .deathCause(rs.getString("death_cause"))
                            .build());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load all graves!", e);
            }
            return graves;
        });
    }

    @Override
    public CompletableFuture<Void> saveSnapshot(DeathSnapshot snapshot) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO gp_snapshots (id, player_id, player_name, location, timestamp, inventory, armor, offhand, experience, death_cause) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, snapshot.getId().toString());
                ps.setString(2, snapshot.getPlayerId().toString());
                ps.setString(3, snapshot.getPlayerName());
                ps.setString(4, LocationUtil.serializeLocation(snapshot.getLocation()));
                ps.setLong(5, snapshot.getTimestamp());
                ps.setString(6, ItemSerialization.serializeItems(snapshot.getInventory()));
                ps.setString(7, ItemSerialization.serializeItems(snapshot.getArmor()));
                ps.setString(8, ItemSerialization.serializeItem(snapshot.getOffHand()));
                ps.setInt(9, snapshot.getExperience());
                ps.setString(10, snapshot.getDeathCause());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save snapshot " + snapshot.getId(), e);
            }
        });
    }

    @Override
    public CompletableFuture<List<DeathSnapshot>> loadSnapshots(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            List<DeathSnapshot> snapshots = new ArrayList<>();
            String sql = "SELECT * FROM gp_snapshots WHERE player_id = ? ORDER BY timestamp DESC";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        snapshots.add(DeathSnapshot.builder()
                                .id(UUID.fromString(rs.getString("id")))
                                .playerId(UUID.fromString(rs.getString("player_id")))
                                .playerName(rs.getString("player_name"))
                                .location(LocationUtil.deserializeLocation(rs.getString("location")))
                                .timestamp(rs.getLong("timestamp"))
                                .inventory(ItemSerialization.deserializeItems(rs.getString("inventory")))
                                .armor(ItemSerialization.deserializeItems(rs.getString("armor")))
                                .offHand(ItemSerialization.deserializeItem(rs.getString("offhand")))
                                .experience(rs.getInt("experience"))
                                .deathCause(rs.getString("death_cause"))
                                .build());
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load snapshots for " + playerId, e);
            }
            return snapshots;
        });
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        });
    }
}
