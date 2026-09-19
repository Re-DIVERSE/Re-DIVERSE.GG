package com.github.re_diverse.rediv_gg;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class ReDiv_GG extends JavaPlugin {
	public static ReDiv_GG instance;

	public static HikariDataSource dataSource;

	@Override
	public void onEnable() {
		instance = this;
		connect();
		StatisticsDataManager.createTable();
	}

	@Override
	public void onDisable() {
		instance = null;
		Bukkit.getScheduler().cancelTasks(this);
		dataSource.close();
	}

	public static void connect() {
		instance.saveDefaultConfig();
		instance.reloadConfig();
		String address = instance.getConfig().getString("server.address");
		int port = instance.getConfig().getInt("server.port");
		String user = instance.getConfig().getString("server.user");
		String pass = instance.getConfig().getString("server.password");
		String schema = instance.getConfig().getString("server.schema");
		String driver = instance.getConfig().getString("database.class_name");
		int lifespan = instance.getConfig().getInt("database.lifespan");
		int maxPoolSize = instance.getConfig().getInt("database.max_pool_size", 1);
		if (!Utilities.strNullCheck(address, user, pass, schema, driver))
			throw new IllegalStateException("いずれかの設定が正しくありません。");
		if (!Utilities.portCheck(port))
			throw new IllegalStateException("ポート番号の設定が正しくありません。");
		if (lifespan == 0)
			throw new IllegalStateException("接続維持時間の設定が正しくありません。");
		dataSource = new HikariDataSource();
		dataSource.setDriverClassName(driver);
		String url = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&useSSL=false", address, port, schema, user, pass);
		dataSource.setJdbcUrl(url);
		if (lifespan > 0)
			dataSource.setMaxLifetime(TimeUnit.MINUTES.toMillis(lifespan));
		if (maxPoolSize > 0) {
			dataSource.setMaximumPoolSize(maxPoolSize);
			dataSource.setMinimumIdle(maxPoolSize);
		}
	}
}
