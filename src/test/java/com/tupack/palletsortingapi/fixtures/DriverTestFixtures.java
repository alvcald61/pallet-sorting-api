package com.tupack.palletsortingapi.fixtures;

import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.domain.User;
import java.util.Set;

/**
 * Test fixtures for Driver entities.
 * Provides factory methods for creating test data.
 */
public class DriverTestFixtures {

    public static Driver createDriver() {
        Driver driver = new Driver();
        driver.setDriverId(1L);
        driver.setDni("12345678");
        driver.setPhone("987654321");
        driver.setEnabled(true);

        User user = createDriverUser();
        driver.setUser(user);

        return driver;
    }

    public static User createDriverUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Carlos");
        user.setLastName("Rodríguez");
        user.setEmail("carlos.rodriguez@test.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        Role driverRole = new Role();
        driverRole.setId(2L);
        driverRole.setName("DRIVER");
        driverRole.setEnabled(true);

        user.setRoles(Set.of(driverRole));

        return user;
    }

    public static Driver createDriverWithDni(String dni) {
        Driver driver = createDriver();
        driver.setDni(dni);
        return driver;
    }

    public static Driver createDisabledDriver() {
        Driver driver = createDriver();
        driver.setEnabled(false);
        return driver;
    }
}
