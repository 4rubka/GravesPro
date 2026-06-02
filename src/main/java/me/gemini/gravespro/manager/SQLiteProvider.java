package me.gemini.gravespro.manager;

import com.zaxxer.hikari.HikariConfig;
import me.gemini.gravespro.GravesPro;

import java.io.File;

public class SQLiteProvider extends AbstractSQLProvider {

    public SQLiteProvider(GravesPro plugin) {
        super(plugin);
    }

    @Override
    protected HikariConfig createConfig() {
        HikariConfig config = new HikariConfig();
        File dbFile = new File(plugin.getDataFolder(), "graves.db");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("GravesPro-SQLite");
        config.setMaximumPoolSize(1); // SQLite only supports one connection for writing
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }
}
