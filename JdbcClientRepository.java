package pl.com.bottega.photostock.sales.infrastructure.repositories;

import pl.com.bottega.photostock.sales.model.deal.Money;
import pl.com.bottega.photostock.sales.model.exceptions.DataAccessException;
import pl.com.bottega.photostock.sales.model.users.Client;
import pl.com.bottega.photostock.sales.model.users.ClientFactory;
import pl.com.bottega.photostock.sales.model.users.ClientStatus;
import pl.com.bottega.photostock.sales.model.usertool.Reservation;

import java.sql.*;
import java.util.List;

/**
 * lublin-3-16-photostock
 * Sergii
 * 2016-06-01.
 */

public class JdbcClientRepository implements Repository<Client> {


    private final String url;
    private final String login;
    private final String password;


    public JdbcClientRepository(String url, String login, String password) throws SQLException {
        super();
        this.url = url;
        this.login = login;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, login, password);
    }

    @Override
    public Client load(String name) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, address, amount_cents, amount_currency, active, status FROM clients WHERE name = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return ClientFactory.getClientInstance(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        new Money(rs.getInt("amount_cents"), rs.getString("amount_currency")),
                        ClientStatus.values()[rs.getInt("status")],
                        rs.getBoolean("active")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e, e.getMessage());
        }
    }

    @Override
    public void save(Client client) {
        try (Connection connection = getConnection()) {
            String sql = load(client.getName()) == null ?
                    "INSERT INTO clients (name, address, amount_cents, amount_currency, active, status) VALUES(?,?,?,?,?,?)" :
                    "UPDATE clients SET name = ?, address = ?, amount_cents = ?, amount_currency = ?, active = ?, status = ? WHERE name = '" +  client.getName() + "'";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, client.getName());
            statement.setString(2, client.getAddress());
            statement.setInt(3, client.getAmountCents());
            statement.setString(4, client.getCurrency());
            statement.setBoolean(5, client.isActive());
            statement.setInt(6, client.getStatusIndex());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e, e.getMessage());
        }
    }

}
