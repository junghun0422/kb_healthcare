package com.kb.healthcare.common.business.user.domain.entity;

import com.kb.healthcare.common.business.base.domain.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Entity
public class User extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<UserIdentifier> userIdentifiers;

    @ElementCollection(fetch =  FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();


    @Builder
    public User(String name, String nickName, String email, String password, Set<String> roles) {
        this.name = name;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>();

        // 기본 역할 설정 (roles가 비어있으면)
        if (this.roles.isEmpty()) {
            this.roles.add("USER");
        }
    }
}
