package com.tupack.palletsortingapi.user.domain;

import com.tupack.palletsortingapi.order.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

    // Nombres como: ROLE_USER, ROLE_ADMIN
    @Column(nullable = false, length = 50)
    private String name;
}
