package com.example.splitapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDto {
    private String person;
    private BigDecimal totalPaid;
    private BigDecimal totalOwed;
    private BigDecimal balance; // positive means they are owed money, negative means they owe money
    
    public BalanceDto(String person) {
        this.person = person;
        this.totalPaid = BigDecimal.ZERO;
        this.totalOwed = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
    }
}