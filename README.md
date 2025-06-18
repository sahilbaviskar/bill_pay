# Split App Backend

A backend system that helps groups of people split expenses fairly and calculate who owes money to whom. Built with Java Spring Boot.

## Features

### Core Features (MUST HAVE)
- ✅ **Expense Tracking**: Add, view, edit, and delete expenses
- ✅ **Automatic Person Management**: People are automatically added when mentioned in expenses
- ✅ **Multiple Split Types**: Equal split, percentage-based, and exact amount splits
- ✅ **Settlement Calculations**: Calculate balances and optimized settlements
- ✅ **Data Validation**: Comprehensive input validation and error handling
- ✅ **RESTful API**: Clean API design with proper HTTP status codes

### Optional Features (NICE TO HAVE)
- 🔄 **Recurring Transactions**: (Not implemented yet)
- 📊 **Expense Categories**: (Not implemented yet)
- 📈 **Enhanced Analytics**: (Not implemented yet)
- 🌐 **Simple Web Interface**: (Not implemented yet)

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: H2 (development), PostgreSQL (production)
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven
- **Validation**: Bean Validation (JSR-303)

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Git

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd split-app-backend
   ```

2. **Run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Access the application**
   - API Base URL: `http://localhost:8080`
   - H2 Console: `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:splitapp`
     - Username: `sa`
     - Password: `password`

### Production Deployment

For production deployment (Railway, Render, etc.):

1. **Update application.properties for PostgreSQL**
   ```properties
   spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
   spring.datasource.username=<username>
   spring.datasource.password=<password>
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

2. **Set environment variables**
   ```
   DATABASE_URL=postgresql://<username>:<password>@<host>:<port>/<database>
   ```

## API Documentation

### Base URL
- Local: `http://localhost:8080`
- Production: `<your-deployed-url>`

### Endpoints

#### Expense Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/expenses` | List all expenses |
| POST | `/expenses` | Add new expense |
| GET | `/expenses/{id}` | Get expense by ID |
| PUT | `/expenses/{id}` | Update expense |
| DELETE | `/expenses/{id}` | Delete expense |

#### Settlement & People

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/balances` | Get each person's balance |
| GET | `/settlements` | Get optimized settlement transactions |
| GET | `/people` | List all people (derived from expenses) |

### Request/Response Examples

#### Add Expense
```http
POST /expenses
Content-Type: application/json

{
  "amount": 600.00,
  "description": "Dinner at restaurant",
  "paidBy": "Shantanu",
  "involvedPeople": ["Shantanu", "Sanket", "Om"],
  "shareType": "EQUAL"
}
```

#### Response Format
```json
{
  "success": true,
  "data": {
    "id": 1,
    "amount": 600.00,
    "description": "Dinner at restaurant",
    "paidBy": "Shantanu",
    "involvedPeople": ["Shantanu", "Sanket", "Om"],
    "shareType": "EQUAL",
    "shares": {
      "Shantanu": 200.00,
      "Sanket": 200.00,
      "Om": 200.00
    },
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "message": "Expense added successfully"
}
```

#### Get Balances
```http
GET /balances
```

```json
{
  "success": true,
  "data": [
    {
      "person": "Shantanu",
      "totalPaid": 1100.00,
      "totalOwed": 710.00,
      "balance": 390.00
    },
    {
      "person": "Sanket",
      "totalPaid": 730.00,
      "totalOwed": 710.00,
      "balance": 20.00
    },
    {
      "person": "Om",
      "totalPaid": 300.00,
      "totalOwed": 710.00,
      "balance": -410.00
    }
  ],
  "message": "Balances calculated successfully"
}
```

#### Get Settlements
```http
GET /settlements
```

```json
{
  "success": true,
  "data": [
    {
      "from": "Om",
      "to": "Shantanu",
      "amount": 390.00
    },
    {
      "from": "Om",
      "to": "Sanket",
      "amount": 20.00
    }
  ],
  "message": "Settlements calculated successfully"
}
```

## Settlement Calculation Logic

The settlement algorithm works as follows:

1. **Calculate Individual Balances**: For each person, calculate `totalPaid - totalOwed`
2. **Identify Creditors and Debtors**: 
   - Positive balance = creditor (should receive money)
   - Negative balance = debtor (should pay money)
3. **Minimize Transactions**: Use a greedy algorithm to minimize the number of settlement transactions
4. **Generate Optimal Settlements**: Create the minimum number of transactions needed to settle all debts

### Example Calculation

Given expenses:
- Shantanu paid ₹1100, owes ₹710 → Balance: +₹390 (creditor)
- Sanket paid ₹730, owes ₹710 → Balance: +₹20 (creditor)  
- Om paid ₹300, owes ₹710 → Balance: -₹410 (debtor)

Optimal settlement:
- Om pays ₹390 to Shantanu
- Om pays ₹20 to Sanket
- Total: 2 transactions instead of potentially 6

## Data Validation

### Input Validation Rules
- **Amount**: Must be positive, max 2 decimal places
- **Description**: Required, max 255 characters
- **Paid By**: Required, only letters and spaces
- **Share Type**: EQUAL, PERCENTAGE, or EXACT_AMOUNT
- **Percentage Shares**: Must sum to 100%
- **Exact Amount Shares**: Must sum to total expense amount

### Error Handling
- Proper HTTP status codes (400, 404, 500)
- Descriptive error messages
- Validation error details
- Graceful handling of edge cases

## Sample Data

The application comes pre-populated with sample data:

**People**: Shantanu, Sanket, Om

**Sample Expenses**:
1. Dinner at restaurant - ₹600 (paid by Shantanu)
2. Groceries for the week - ₹450 (paid by Sanket)  
3. Petrol for road trip - ₹300 (paid by Om)
4. Movie tickets - ₹500 (paid by Shantanu)
5. Pizza delivery - ₹280 (paid by Sanket)

## Testing

### Using Postman
1. Import the provided Postman collection
2. Set the base URL to your deployed API or `http://localhost:8080`
3. Run the pre-configured requests to test all endpoints

### Manual Testing
```bash
# Get all expenses
curl -X GET http://localhost:8080/expenses

# Add new expense
curl -X POST http://localhost:8080/expenses \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00,"description":"Test expense","paidBy":"John","involvedPeople":["John","Jane"]}'

# Get balances
curl -X GET http://localhost:8080/balances

# Get settlements
curl -X GET http://localhost:8080/settlements
```

## Known Limitations

1. **Currency**: Currently handles only one currency (assumes INR)
2. **Precision**: Uses BigDecimal with 2 decimal places for money calculations
3. **Concurrency**: No optimistic locking for concurrent updates
4. **Authentication**: No user authentication/authorization implemented
5. **Audit Trail**: No tracking of who made changes to expenses

## Future Enhancements

- [ ] Multi-currency support
- [ ] User authentication and authorization
- [ ] Expense categories and tags
- [ ] Recurring expenses
- [ ] File attachments (receipts)
- [ ] Email notifications
- [ ] Mobile app integration
- [ ] Advanced analytics and reporting

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please create an issue in the GitHub repository.

---

**Happy Splitting! 💰**