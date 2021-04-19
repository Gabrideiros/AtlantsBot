package me.gabrideiros.bot.database;

import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

public interface Storage extends Closeable {

	void openConnection();

	Connection getConnection();

	void close() throws IOException;

	void sendCommand(String... commands);

	ResultSet sendQuery(String command);

	String getHost();

	String getDatabase();

	String getUser();

	String getPassword();

	int getPort();

	File getFile();

	String getUrl();

	Plugin getPlugin();
	
}
