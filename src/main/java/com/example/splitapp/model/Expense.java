package com.example.splitapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    
    @Id
    private String id;
    
    private BigDecimal amount;
    
    private String description;
    
    private String paidBy;
    
    private List<String> involvedPeople = new ArrayList<>();
    
    private ExpenseShareType shareType = ExpenseShareType.EQUAL;
    
    private Map<String, BigDecimal> shares = new HashMap<>();
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}