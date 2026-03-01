package com.tupack.palletsortingapi.user.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.tupack.palletsortingapi.fixtures.ClientTestFixtures;
import com.tupack.palletsortingapi.user.application.dto.ClientDto;
import com.tupack.palletsortingapi.user.domain.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * CRITICAL TEST: Verifies the fix for firstName/lastName mapping bug.
 *
 * This test ensures that the ClientMapper correctly maps firstName and lastName
 * from User entity to ClientDto without swapping them.
 *
 * Bug History: Previously, firstName was mapped to lastName and vice versa,
 * causing data corruption in the database.
 */
@DisplayName("ClientMapper - Critical firstName/lastName mapping test")
class ClientMapperTest {

    private final ClientMapper mapper = Mappers.getMapper(ClientMapper.class);

    @Test
    @DisplayName("Should correctly map firstName from User to ClientDto")
    void shouldMapFirstNameCorrectly() {
        // Given: A client with specific firstName
        String expectedFirstName = "Juan";
        Client client = ClientTestFixtures.createClientWithNames(expectedFirstName, "Pérez");

        // When: Mapping to DTO
        ClientDto dto = mapper.toDto(client);

        // Then: firstName should match exactly (not swapped)
        assertThat(dto.getFirstName())
            .as("firstName should be mapped correctly, not swapped with lastName")
            .isEqualTo(expectedFirstName);
    }

    @Test
    @DisplayName("Should correctly map lastName from User to ClientDto")
    void shouldMapLastNameCorrectly() {
        // Given: A client with specific lastName
        String expectedLastName = "García";
        Client client = ClientTestFixtures.createClientWithNames("María", expectedLastName);

        // When: Mapping to DTO
        ClientDto dto = mapper.toDto(client);

        // Then: lastName should match exactly (not swapped)
        assertThat(dto.getLastName())
            .as("lastName should be mapped correctly, not swapped with firstName")
            .isEqualTo(expectedLastName);
    }

    @Test
    @DisplayName("Should map both names correctly without swapping")
    void shouldMapBothNamesCorrectlyWithoutSwapping() {
        // Given: A client with specific names
        String expectedFirstName = "Carlos";
        String expectedLastName = "Rodríguez";
        Client client = ClientTestFixtures.createClientWithNames(expectedFirstName, expectedLastName);

        // When: Mapping to DTO
        ClientDto dto = mapper.toDto(client);

        // Then: Both names should be mapped in correct order
        assertThat(dto.getFirstName())
            .as("firstName should not be swapped with lastName")
            .isEqualTo(expectedFirstName)
            .isNotEqualTo(expectedLastName);

        assertThat(dto.getLastName())
            .as("lastName should not be swapped with firstName")
            .isEqualTo(expectedLastName)
            .isNotEqualTo(expectedFirstName);
    }

    @Test
    @DisplayName("Should map all Client fields correctly")
    void shouldMapAllClientFieldsCorrectly() {
        // Given: A complete client entity
        Client client = ClientTestFixtures.createClient();

        // When: Mapping to DTO
        ClientDto dto = mapper.toDto(client);

        // Then: All fields should be mapped correctly
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(client.getId().toString());
        assertThat(dto.getBusinessName()).isEqualTo(client.getBusinessName());
        assertThat(dto.getRuc()).isEqualTo(client.getRuc());
        assertThat(dto.getPhone()).isEqualTo(client.getPhone());
        assertThat(dto.getAddress()).isEqualTo(client.getAddress());
        assertThat(dto.getEmail()).isEqualTo(client.getUser().getEmail());
        assertThat(dto.isTrust()).isEqualTo(client.isTrust());
    }

    @Test
    @DisplayName("Should handle names with special characters")
    void shouldHandleNamesWithSpecialCharacters() {
        // Given: Names with accents and special characters
        String firstName = "José María";
        String lastName = "Pérez-García";
        Client client = ClientTestFixtures.createClientWithNames(firstName, lastName);

        // When: Mapping to DTO
        ClientDto dto = mapper.toDto(client);

        // Then: Special characters should be preserved
        assertThat(dto.getFirstName()).isEqualTo(firstName);
        assertThat(dto.getLastName()).isEqualTo(lastName);
    }
}
