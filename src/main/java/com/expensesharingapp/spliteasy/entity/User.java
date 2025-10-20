package com.expensesharingapp.spliteasy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Google OAuth subject ID (sub claim), unique for each user
    @Column(nullable = false, unique = true)
    private String oauthId;

    // Role: USER / ADMIN
    @Column(nullable = false)
    private String role;
}
