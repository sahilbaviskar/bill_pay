package com.example.splitapp.repository;

import com.example.splitapp.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String> {
    
    @Query(value = "{}", fields = "{ 'involvedPeople' : 1 }")
    List<Expense> findAllExpensesWithPeople();
    
    @Query("{ $or: [ { 'paidBy': ?0 }, { 'involvedPeople': ?0 } ] }")
    List<Expense> findExpensesByPerson(String person);
}