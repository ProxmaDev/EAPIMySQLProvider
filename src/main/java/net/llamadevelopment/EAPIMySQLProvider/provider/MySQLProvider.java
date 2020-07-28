package net.llamadevelopment.EAPIMySQLProvider.provider;

import cn.nukkit.utils.Config;
import me.onebone.economyapi.provider.Provider;
import net.llamadevelopment.EAPIMySQLProvider.EAPIMySQLProvider;

import java.io.File;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MySQLProvider implements Provider {

    private Connection connection;
    private LinkedHashMap<String, Double> data = new LinkedHashMap<String, Double>();
    private String database = "economyapi";

    @Override
    public void init(File file) { }

    @Override
    public void open() {
        EAPIMySQLProvider plugin = EAPIMySQLProvider.getInstance();
        Config c = plugin.getConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            database = c.getString("mysql.database");
            String connectionUri = "jdbc:mysql://" + c.getString("mysql.ip") + ":" + c.getString("mysql.port") + "/" + c.getString("mysql.database") + "?autoReconnect=true&useGmtMillisForDatetimes=true&serverTimezone=GMT";
            connection = DriverManager.getConnection(connectionUri, c.getString("mysql.username"), c.getString("mysql.password"));
            connection.setAutoCommit(true);

            DatabaseMetaData dbm = null;
            dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "money", null);
            if (!tables.next()) {
                String tableCreate = "CREATE TABLE money (id VARCHAR(64), money double null, constraint money_pk primary key(id))";
                Statement createTable = connection.createStatement();
                createTable.executeUpdate(tableCreate);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("It was not possible to establish a connection with the database.");
        } catch (ClassNotFoundException ex) {
            System.out.println("MySQL Driver is missing... Are you using the right .jar file?");
        }
    }

    @Override
    public void save() { }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean accountExists(String id) {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + database + ".money WHERE id='" + id + "'");
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeAccount(String id) {
        if (accountExists(id)) {
            try {
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + database + ".money WHERE id=?");
                deleteStatement.setString(1, id);
                deleteStatement.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean createAccount(String id, double defaultMoney) {
        if (!this.accountExists(id)) {
            try {
                PreparedStatement newUserStatement = connection.prepareStatement("INSERT INTO " + database + ".money (id, money) VALUES (?,?)");
                newUserStatement.setString(1, id);
                newUserStatement.setDouble(2, defaultMoney);
                newUserStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean setMoney(String id, double amount) {
        try {
            connection.createStatement().executeUpdate("UPDATE " + database + ".money SET money = " + amount +" WHERE id='" + id + "'");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean addMoney(String id, double amount) {
        try {
            connection.createStatement().executeUpdate("UPDATE " + database + ".money SET money = money +" + amount +" WHERE id='" + id + "'");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean reduceMoney(String id, double amount) {
        try {
            connection.createStatement().executeUpdate("UPDATE " + database + ".money SET money = money -" + amount +" WHERE id='" + id + "'");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public double getMoney(String id) {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + database + ".money WHERE id='" + id + "'");
            if (resultSet.next()) {
                return resultSet.getDouble("money");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @Override
    public LinkedHashMap<String, Double> getAll() {
        LinkedHashMap<String, Double> all = new LinkedHashMap<String, Double>();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + database + ".money");
            while (resultSet.next()) {
                all.put(resultSet.getString("id"), resultSet.getDouble("money"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return all;
    }

    @Override
    public String getName() {
        return "MySQL";
    }

}