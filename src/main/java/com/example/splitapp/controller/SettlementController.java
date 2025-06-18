package com.example.splitapp.controller;

import com.example.splitapp.dto.ApiResponse;
import com.example.splitapp.dto.BalanceDto;
import com.example.splitapp.dto.SettlementDto;
import com.example.splitapp.service.ExpenseService;
import com.example.splitapp.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettlementController {
    
    private final SettlementService settlementService;
    private final ExpenseService expenseService;
    
    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<BalanceDto>>> getBalances() {
        try {
            List<BalanceDto> balances = settlementService.calculateBalances();
            return ResponseEntity.ok(ApiResponse.success(balances, "Balances calculated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to calculate balances: " + e.getMessage()));
        }
    }
    
    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<List<SettlementDto>>> getSettlements() {
        try {
            List<SettlementDto> settlements = settlementService.calculateSettlements();
            return ResponseEntity.ok(ApiResponse.success(settlements, "Settlements calculated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to calculate settlements: " + e.getMessage()));
        }
    }
    
    @GetMapping("/people")
    public ResponseEntity<ApiResponse<List<String>>> getAllPeople() {
        try {
            List<String> people = expenseService.getAllPeople();
            return ResponseEntity.ok(ApiResponse.success(people, "People retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve people: " + e.getMessage()));
        }
    }
}