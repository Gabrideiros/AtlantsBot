package me.gabrideiros.bot.database.types;

import me.gabrideiros.bot.database.Storage;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class MySQL implements Storage {

	private Connection connection;
	private final String host;
	private final int port;
	private final String database;
	private final String user;
	private final String password;
	private final String url;
	private final Plugin plugin;
	
	public MySQL(Plugin plugin, String host, int port, String database, String user, String password) {
		this.plugin = plugin;
		this.port = port;
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
		this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
	}
	
	@Override
	public void openConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(getUrl(), getUser(), getPassword());
			getPlugin().getLogger().info("Conexao com o MySQL aberta com sucesso!");
		} catch (Exception exception) {
			getPlugin().getLogger().severe("Ocorreu um erro ao tentar abrir a conexao com o MySQL:");
			exception.printStackTrace();
		}
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void close() throws IOException {
		try {
			connection.close();
			getPlugin().getLogger().info("Conexao com o MySQL fechada com sucesso!");
		} catch (Exception exception) {
			getPlugin().getLogger().severe("Ocorreu um erro ao tentar fechar a conexao com o MySQL:");
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
			getPlugin().getLogger().severe("Ocorreu um erro ao tentar enviar um comando ao MySQL:");
			exception.printStackTrace();
		}
	}

	@Override
	public ResultSet sendQuery(String command) {
		try {
			Statement stmt = connection.createStatement();
			return stmt.executeQuery(command);
		} catch (Exception exception) {
			getPlugin().getLogger().severe("Ocorreu um erro ao tentar enviar um comando ao MySQL:");
			exception.printStackTrace();
		}
		return null;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public String getDatabase() {
		return database;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public File getFile() {
		return null;
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
