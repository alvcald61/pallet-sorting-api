package com.tupack.palletsortingapi.fixtures;

import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.domain.User;
import java.util.Set;

/**
 * Test fixtures for Client entities.
 * Provides factory methods for creating test data.
 */
public class ClientTestFixtures {

    public static Client createClient() {
        Client client = new Client();
        client.setId(1L);
        client.setRuc("20123456789");
        client.setBusinessName("Test Business S.A.C.");
        client.setPhone("987654321");
        client.setAddress("Av. Test 123, Lima");
        client.setTrust(true);
        client.setEnabled(true);

        User user = createUser();
        client.setUser(user);

        return client;
    }

    public static Client createClientWithNames(String firstName, String lastName) {
        Client client = createClient();
        User user = client.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return client;
    }

    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Juan");
        user.setLastName("Pérez");
        user.setEmail("juan.perez@test.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        Role clientRole = new Role();
        clientRole.setId(1L);
        clientRole.setName("CLIENT");
        clientRole.setEnabled(true);

        user.setRoles(Set.of(clientRole));

        return user;
    }

    public static Client createDisabledClient() {
        Client client = createClient();
        client.setEnabled(false);
        return client;
    }

    public static Client createClientWithEmail(String email) {
        Client client = createClient();
        client.getUser().setEmail(email);
        return client;
    }
}
