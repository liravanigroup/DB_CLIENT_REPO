package pl.com.bottega.photostock.sales.infrastructure.repositories;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.com.bottega.photostock.sales.model.deal.Money;
import pl.com.bottega.photostock.sales.model.users.Client;
import pl.com.bottega.photostock.sales.model.users.ClientFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static pl.com.bottega.photostock.sales.model.users.ClientStatus.STANDARD;


public class JdbcClientRepositoryTest {
    private Repository repo;
    private final static Client INIT_CLIENT = ClientFactory.getClientInstance("1", "Agnieszka", "London", new Money(50), STANDARD, true);

    @Before
    public void setUp() throws SQLException {
        // given
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:stockdb", "SA", "");
        createClientsTable(connection);
        insertTestClient(connection);
        connection.close();
        // when
        repo = new JdbcClientRepository("jdbc:hsqldb:mem:stockdb", "SA", "");
    }

    @Test
    public void shouldLoadClientByName() throws SQLException {
        // when
       Client loadedClient = (Client) repo.load("Agnieszka");

        // then
        assertEquals(INIT_CLIENT.getName(), loadedClient.getName());
        assertEquals(INIT_CLIENT.getCurrency(), loadedClient.getCurrency());
        assertEquals(INIT_CLIENT.getPaymentStrategy().getClass(), loadedClient.getPaymentStrategy().getClass());
        assertEquals(INIT_CLIENT.getSaldo(), loadedClient.getSaldo());
        assertEquals(Client.class, loadedClient.getClass());
    }

    @Test
    public void shouldReturnNullWhenClientDoesNotExists() throws SQLException {
        // when
        Client client = (Client) repo.load("Mario");

        //then
        Assert.assertNull(client);
    }

    private void insertTestClient(Connection connection) throws SQLException {
        connection.createStatement().executeUpdate("INSERT INTO Clients (name, address, amount_cents, amount_currency, status, active) VALUES ('Agnieszka','London', 50, 'PLN', 0, true);");
    }

    private void createClientsTable(Connection connection) throws SQLException {
        connection.createStatement().executeUpdate("DROP TABLE clients IF EXISTS ");
        connection.createStatement().executeUpdate("CREATE TABLE clients (\n" +
                "  id INTEGER IDENTITY PRIMARY KEY,\n" +
                "  name VARCHAR(255) NOT NULL,\n" +
                "  address VARCHAR(255) NOT NULL,\n" +
                "  amount_cents INTEGER DEFAULT 0 NOT NULL, \n" +
                "  amount_currency CHAR(3) DEFAULT 'PLN' NOT NULL, \n" +
                "  status INTEGER DEFAULT 0 NOT NULL, \n" +
                "  active BOOLEAN DEFAULT true NOT NULL,  \n" +
                ");");
    }

    @Test
    public void shouldSaveClient(){
        // given
        Client savedClient = new Client("Jan Kowalski", "", "London", Money.FIVE_PL);
        repo.save(savedClient);

        //When
        Client loadedClient = (Client) repo.load(savedClient.getName());

        //then
        Assert.assertEquals(savedClient.getName(),loadedClient.getName());
    }

    @Test
    public void shouldUpdateClient(){
        // given
        Client savedClient = new Client("Jan Kowalski", "", "London", new Money(100));
        Client updatedClient = new Client("Jan Kowalski", "", "Boston", new Money(50));

        repo.save(savedClient);
        repo.save(updatedClient);

        //When
        Client loadedClientAfterUpdate = (Client) repo.load("Jan Kowalski");

        //then
        Assert.assertEquals(updatedClient.getName(),loadedClientAfterUpdate.getName());
        Assert.assertEquals(updatedClient.getAddress(),loadedClientAfterUpdate.getAddress());
        Assert.assertEquals(updatedClient.getSaldo(),loadedClientAfterUpdate.getSaldo());
    }
}