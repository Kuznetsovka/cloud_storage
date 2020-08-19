package com.geekbrains.cloud_storage.server;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlClient {
    enum Type {
        MySQL, SQLite
    }
    private static Connection connection;
    private static Statement statement;

    synchronized static String getNickname(String login, String password) {
        String query = String.format("select nickname from clients where login='%s' and password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next())
                return set.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    synchronized static void connect(Type type) {
        try {
            if (type == Type.SQLite) {
                Class.forName ("org.sqlite.JDBC");
                connection = DriverManager.getConnection ("jdbc:sqlite:Cloudy.db");
            }
            if (type == Type.MySQL) {
                Class.forName ("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection ("jdbc:mysql://localhost:3306/cloud_storage?serverTimezone=Europe/Moscow", "root", "Nbirf");
            }
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static int getIdUser(String login, String password) {
        String query = String.format("select id_user from users where fld_name='%s' and fld_password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next())
                return set.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}