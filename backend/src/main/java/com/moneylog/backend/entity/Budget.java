package com.moneylog.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "budget")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth;

    @Column(nullable = false)
    private Long amount;

    public void update(Long amount) {
        this.amount = amount;
    }

}
