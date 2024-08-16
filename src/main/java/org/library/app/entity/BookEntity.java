package org.library.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "public", name = "t_book")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_title", nullable = false)
    private String title;

    @Column(name = "c_author", nullable = false)
    private String author;

    @Column(name = "c_amount", nullable = false)
    private Integer amount;

    @ManyToMany(mappedBy = "borrowedBooks", fetch = FetchType.EAGER)
    private Set<MemberEntity> members = new HashSet<>();
}