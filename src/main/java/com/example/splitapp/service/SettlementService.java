package com.example.splitapp.service;

import com.example.splitapp.dto.BalanceDto;
import com.example.splitapp.dto.SettlementDto;
import com.example.splitapp.model.Expense;
import com.example.splitapp.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {
    
    private final ExpenseRepository expenseRepository;
    
    public List<BalanceDto> calculateBalances() {
        List<Expense> expenses = expenseRepository.findAll();
        Map<String, BalanceDto> balanceMap = new HashMap<>();
        
        // Initialize balances for all people
        Set<String> allPeople = new HashSet<>();
        for (Expense expense : expenses) {
            allPeople.add(expense.getPaidBy());
            allPeople.addAll(expense.getInvolvedPeople());
        }
        
        for (String person : allPeople) {
            balanceMap.put(person, new BalanceDto(person));
        }
        
        // Calculate balances
        for (Expense expense : expenses) {
            String payer = expense.getPaidBy();
            BigDecimal totalAmount = expense.getAmount();
            
            // Add to payer's total paid
            BalanceDto payerBalance = balanceMap.get(payer);
            payerBalance.setTotalPaid(payerBalance.getTotalPaid().add(totalAmount));
            
            // Distribute shares among involved people
            for (Map.Entry<String, BigDecimal> shareEntry : expense.getShares().entrySet()) {
                String person = shareEntry.getKey();
                BigDecimal share = shareEntry.getValue();
                
                BalanceDto personBalance = balanceMap.get(person);
                if (personBalance != null) {
                    personBalance.setTotalOwed(personBalance.getTotalOwed().add(share));
                }
            }
        }
        
        // Calculate final balance (totalPaid - totalOwed)
        for (BalanceDto balance : balanceMap.values()) {
            BigDecimal finalBalance = balance.getTotalPaid().subtract(balance.getTotalOwed());
            balance.setBalance(finalBalance);
        }
        
        return new ArrayList<>(balanceMap.values());
    }
    
    public List<SettlementDto> calculateSettlements() {
        List<BalanceDto> balances = calculateBalances();
        List<SettlementDto> settlements = new ArrayList<>();
        
        // Separate creditors (positive balance) and debtors (negative balance)
        List<BalanceDto> creditors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getBalance().compareTo(a.getBalance())) // Sort descending
                .collect(Collectors.toList());
        
        List<BalanceDto> debtors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .sorted((a, b) -> a.getBalance().compareTo(b.getBalance())) // Sort ascending (most negative first)
                .collect(Collectors.toList());
        
        // Create a working copy to avoid modifying original balances
        Map<String, BigDecimal> creditorBalances = creditors.stream()
                .collect(Collectors.toMap(BalanceDto::getPerson, BalanceDto::getBalance));
        
        Map<String, BigDecimal> debtorBalances = debtors.stream()
                .collect(Collectors.toMap(BalanceDto::getPerson, b -> b.getBalance().negate()));
        
        // Minimize transactions using greedy algorithm
        for (String debtor : debtorBalances.keySet()) {
            BigDecimal debtAmount = debtorBalances.get(debtor);
            
            for (String creditor : creditorBalances.keySet()) {
                BigDecimal creditAmount = creditorBalances.get(creditor);
                
                if (debtAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                BigDecimal settlementAmount = debtAmount.min(creditAmount);
                
                if (settlementAmount.compareTo(BigDecimal.ZERO) > 0) {
                    settlements.add(new SettlementDto(debtor, creditor, settlementAmount));
                    
                    debtAmount = debtAmount.subtract(settlementAmount);
                    creditAmount = creditAmount.subtract(settlementAmount);
                    
                    debtorBalances.put(debtor, debtAmount);
                    creditorBalances.put(creditor, creditAmount);
                }
            }
        }
        
        return settlements;
    }
}