package me.gemini.gravespro.manager;

import com.zaxxer.hikari.HikariConfig;
import me.gemini.gravespro.GravesPro;
import org.bukkit.configuration.file.FileConfiguration;

public class MySQLProvider extends AbstractSQLProvider {

    public MySQLProvider(GravesPro plugin) {
        super(plugin);
    }

    @Override
    protected HikariConfig createConfig() {
        FileConfiguration settings = plugin.getConfigurationManager().getConfig("settings.yml");
        HikariConfig config = new HikariConfig();
        
        String host = settings.getString("storage.mysql.host");
        int port = settings.getInt("storage.mysql.port");
        String database = settings.getString("storage.mysql.database");
        String username = settings.getString("storage.mysql.username");
        String password = settings.getString("storage.mysql.password");
        boolean useSSL = settings.getBoolean("storage.mysql.use-ssl");

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("GravesPro-MySQL");
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useSSL", String.valueOf(useSSL));
        
        return config;
    }
}
