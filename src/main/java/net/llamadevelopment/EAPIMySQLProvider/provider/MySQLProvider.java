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
    public void init(File file) {

    }

    @Override
    public void open() {
        EAPIMySQLProvider plugin = EAPIMySQLProvider.getInstance();
        Config c = plugin.getConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            database = c.getString("mysql.database");
            String connectionUri = "jdbc:mysql://" + c.getString("mysql.ip") + ":" + c.getString("mysql.port") + "/" + c.getString("mysql.database");
            connection = DriverManager.getConnection(connectionUri, c.getString("mysql.username"), c.getString("mysql.password"));
            connection.setAutoCommit(true);

            DatabaseMetaData dbm = null;
            dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "money", null);
            if (!tables.next()) {
                String tableCreate = "CREATE TABLE money (id VARCHAR(255) null, money double null, constraint money_pk primary key(id))";
                Statement createTable = connection.createStatement();
                createTable.executeUpdate(tableCreate);
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + database + ".money");

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                Double money = resultSet.getDouble("money");
                data.put(id, money);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("It was not possible to establish a connection with the database.");
            return;
        } catch (ClassNotFoundException ex) {
            System.out.println("MySQL Driver is missing... Are you using the right .jar file?");
        }
    }

    @Override
    public void save() {
        try {
            for (Map.Entry<String, Double> map : data.entrySet()) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + database + ".money WHERE id='" + map.getKey() + "'");
                if (resultSet.next()) {
                    Double currMoney = resultSet.getDouble("money");
                    if (!currMoney.equals(map.getValue())) {
                        Statement updateStatement = connection.createStatement();
                        updateStatement.executeUpdate("UPDATE " + database + ".money SET money=" + map.getValue() + " WHERE id='" + map.getKey() + "'");
                    }
                } else {
                    PreparedStatement newUserStatement = connection.prepareStatement("INSERT INTO " + database + ".money (id, money) VALUES (?,?)");
                    newUserStatement.setString(1, map.getKey());
                    newUserStatement.setDouble(2, map.getValue());
                    newUserStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        this.save();

        data.clear();
    }

    @Override
    public boolean accountExists(String id) {
        return data.containsKey(id);
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
            data.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean createAccount(String id, double defaultMoney) {
        if (!this.accountExists(id)) {
            data.put(id, defaultMoney);
        }
        return false;
    }

    @Override
    public boolean setMoney(String id, double amount) {
        if (data.containsKey(id)) {
            data.put(id, amount);
            return true;
        }
        return false;
    }


    @Override
    public boolean addMoney(String id, double amount) {
        if (data.containsKey(id)) {
            data.put(id, data.get(id) + amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean reduceMoney(String id, double amount) {
        if (data.containsKey(id)) {
            data.put(id, data.get(id) - amount);
            return true;
        }
        return false;
    }

    @Override
    public double getMoney(String id) {
        if (data.containsKey(id)) {
            return data.get(id);
        }
        return -1;
    }

    @Override
    public LinkedHashMap<String, Double> getAll() {
        return data;
    }

    @Override
    public String getName() {
        return "MySQL";
    }
}
