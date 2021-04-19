package me.gabrideiros.bot.database.types;

import me.gabrideiros.bot.database.Storage;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

public class SQLite implements Storage {

	private Connection connection;
	
	private final Plugin plugin;
	private final File file;
	private final String url;
	
	public SQLite(Plugin plugin, String file) {
		this.plugin = plugin;
		this.file = new File(plugin.getDataFolder(), file);
		this.url = "jdbc:sqlite:" + file;
	}
	
	@Override
	public void openConnection() {
		try {
			if(!file.getParentFile().exists()) file.getParentFile().mkdir();
			if(!file.exists()) file.createNewFile();
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection(getUrl());
			System.out.println("Conexao com o SQLite aberta com sucesso!");
		} catch (Exception exception) {
			System.out.println("Ocorreu um erro ao tentar abrir a conexao com o SQLite:");
			exception.printStackTrace();
		}
    }

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void close() {
		try {
			connection.close();
			getPlugin().getLogger().info("Conexao com o SQLite fechada com sucesso!");
		} catch (Exception exception) {
			System.out.println("Ocorreu um erro ao tentar fechar a conexao com o SQLite:");
			exception.printStackTrace();
		}
	}
	
	@Override
	public void sendCommand(String... commands) {
		try {
			for(String command : commands) {
				PreparedStatement pStmt = connection.prepareStatement(command);
				pStmt.executeUpdate();
			}
		} catch (Exception exception) {
			System.out.println("Ocorreu um erro ao tentar enviar um comando ao SQLite:");
			exception.printStackTrace();
		}
	}

	@Override
	public ResultSet sendQuery(String coPrismMobsand) {
		try {
			Statement stmt = connection.createStatement();
			return stmt.executeQuery(coPrismMobsand);
		} catch (Exception exception) {
			System.out.println("Ocorreu um erro ao tentar enviar um comando ao SQLite:");
			exception.printStackTrace();
		}
		return null;
	}

	@Override
	public String getHost() {
		return null;
	}

	@Override
	public String getDatabase() {
		return null;
	}

	@Override
	public String getUser() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}

}
