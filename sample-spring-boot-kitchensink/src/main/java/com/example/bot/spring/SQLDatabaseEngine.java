package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {
	@Override
	String search(String text) throws Exception {
			PreparedStatement statement = getConnection().prepareStatement(
					"SELECT * FROM messages WHERE ? LIKE concat('%',keyword,'%')");
			statement.setString(1, text);
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				String response = rs.getString("response");
				try {
					int hit = rs.getInt("hit");
					statement = getConnection().prepareStatement("UPDATE messages SET hit = ? WHERE response = ?");
					statement.setInt(1,++hit);
					statement.setString(2,response);
					statement.executeUpdate();
					return response;
				}
				catch (Exception e){
					System.out.println(e);
					statement = getConnection().prepareStatement("ALTER TABLE messages ADD hit int DEFAULT 0");
					statement.executeUpdate();
					statement = getConnection().prepareStatement("UPDATE messages SET hit = ? WHERE response = ?");
					int hit = 1;
					statement.setInt(1, hit);
					statement.setString(2, response);
					statement.executeUpdate();
					return response + " " + String.valueOf(hit);
				}
		}
		throw new Exception("NOT FOUND");
		//return null;
	}
	
	private Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);
		
		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}

}
