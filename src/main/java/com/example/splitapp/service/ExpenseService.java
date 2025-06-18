package com.example.splitapp.service;

import com.example.splitapp.dto.ExpenseDto;
import com.example.splitapp.model.Expense;
import com.example.splitapp.model.ExpenseShareType;
import com.example.splitapp.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
    
    public Optional<Expense> getExpenseById(String id) {
        return expenseRepository.findById(id);
    }
    
    public Expense createExpense(ExpenseDto expenseDto) {
        validateExpenseDto(expenseDto);
        
        Expense expense = new Expense();
        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setPaidBy(expenseDto.getPaidBy());
        expense.setShareType(expenseDto.getShareType());
        
        // If no involved people specified, assume equal split between payer and others
        List<String> involvedPeople = expenseDto.getInvolvedPeople();
        if (involvedPeople == null || involvedPeople.isEmpty()) {
            involvedPeople = Arrays.asList(expenseDto.getPaidBy());
        }
        
        // Ensure paidBy is in involved people
        if (!involvedPeople.contains(expenseDto.getPaidBy())) {
            involvedPeople = new ArrayList<>(involvedPeople);
            involvedPeople.add(expenseDto.getPaidBy());
        }
        
        expense.setInvolvedPeople(involvedPeople);
        
        // Calculate shares based on share type
        Map<String, BigDecimal> shares = calculateShares(expenseDto, involvedPeople);
        expense.setShares(shares);
        
        // Set timestamps
        expense.onCreate();
        
        return expenseRepository.save(expense);
    }
    
    public Expense updateExpense(String id, ExpenseDto expenseDto) {
        validateExpenseDto(expenseDto);
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        
        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setPaidBy(expenseDto.getPaidBy());
        expense.setShareType(expenseDto.getShareType());
        
        List<String> involvedPeople = expenseDto.getInvolvedPeople();
        if (involvedPeople == null || involvedPeople.isEmpty()) {
            involvedPeople = Arrays.asList(expenseDto.getPaidBy());
        }
        
        if (!involvedPeople.contains(expenseDto.getPaidBy())) {
            involvedPeople = new ArrayList<>(involvedPeople);
            involvedPeople.add(expenseDto.getPaidBy());
        }
        
        expense.setInvolvedPeople(involvedPeople);
        
        Map<String, BigDecimal> shares = calculateShares(expenseDto, involvedPeople);
        expense.setShares(shares);
        
        // Set update timestamp
        expense.onUpdate();
        
        return expenseRepository.save(expense);
    }
    
    public void deleteExpense(String id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }
    
    public List<String> getAllPeople() {
        List<Expense> expenses = expenseRepository.findAllExpensesWithPeople();
        Set<String> allPeople = new HashSet<>();
        
        for (Expense expense : expenses) {
            allPeople.add(expense.getPaidBy());
            if (expense.getInvolvedPeople() != null) {
                allPeople.addAll(expense.getInvolvedPeople());
            }
        }
        
        return new ArrayList<>(allPeople);
    }
    
    private void validateExpenseDto(ExpenseDto expenseDto) {
        if (expenseDto.getAmount() == null || expenseDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        
        if (expenseDto.getDescription() == null || expenseDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        
        if (expenseDto.getPaidBy() == null || expenseDto.getPaidBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Paid by is required");
        }
        
        // Validate shares if provided
        if (expenseDto.getShareType() == ExpenseShareType.PERCENTAGE && expenseDto.getShares() != null) {
            BigDecimal totalPercentage = expenseDto.getShares().values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
                throw new IllegalArgumentException("Percentage shares must add up to 100%");
            }
        }
        
        if (expenseDto.getShareType() == ExpenseShareType.EXACT_AMOUNT && expenseDto.getShares() != null) {
            BigDecimal totalShares = expenseDto.getShares().values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalShares.compareTo(expenseDto.getAmount()) != 0) {
                throw new IllegalArgumentException("Exact amount shares must add up to total expense amount");
            }
        }
    }
    
    private Map<String, BigDecimal> calculateShares(ExpenseDto expenseDto, List<String> involvedPeople) {
        Map<String, BigDecimal> shares = new HashMap<>();
        
        switch (expenseDto.getShareType()) {
            case EQUAL:
                BigDecimal equalShare = expenseDto.getAmount()
                        .divide(new BigDecimal(involvedPeople.size()), 2, RoundingMode.HALF_UP);
                for (String person : involvedPeople) {
                    shares.put(person, equalShare);
                }
                break;
                
            case PERCENTAGE:
                if (expenseDto.getShares() != null) {
                    for (String person : involvedPeople) {
                        BigDecimal percentage = expenseDto.getShares().getOrDefault(person, BigDecimal.ZERO);
                        BigDecimal share = expenseDto.getAmount()
                                .multiply(percentage)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        shares.put(person, share);
                    }
                } else {
                    // Default to equal if no percentages provided
                    return calculateShares(new ExpenseDto(expenseDto.getAmount(), expenseDto.getDescription(), 
                            expenseDto.getPaidBy(), involvedPeople, ExpenseShareType.EQUAL, null), involvedPeople);
                }
                break;
                
            case EXACT_AMOUNT:
                if (expenseDto.getShares() != null) {
                    for (String person : involvedPeople) {
                        BigDecimal share = expenseDto.getShares().getOrDefault(person, BigDecimal.ZERO);
                        shares.put(person, share);
                    }
                } else {
                    // Default to equal if no exact amounts provided
                    return calculateShares(new ExpenseDto(expenseDto.getAmount(), expenseDto.getDescription(), 
                            expenseDto.getPaidBy(), involvedPeople, ExpenseShareType.EQUAL, null), involvedPeople);
                }
                break;
        }
        
        return shares;
    }
}