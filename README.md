# TrackIt AI Finance Manager

An AI-powered personal finance management application built with Java, Maven, MySQL, JDBC, and Swing. The application helps users track expenses, manage budgets, monitor savings goals, and receive intelligent recommendations to improve financial habits.

## Features

- Add Expenses
- View Expenses
- Update Expenses
- Delete Expenses
- Monthly Budget Management
- Savings Goal Tracking
- Monthly Financial Summary
- Expense Categorization
- AI-Powered Financial Insights
- Spending Analysis and Recommendations

## AI Features

The application analyzes user spending patterns and provides intelligent suggestions to reduce unnecessary expenses and improve savings habits.

Examples include:

- Identifying categories with excessive spending
- Highlighting recurring unnecessary expenses
- Suggesting budget adjustments
- Providing personalized savings recommendations

## Tech Stack

- Java
- Maven
- Swing
- JDBC
- MySQL
- Object-Oriented Programming (OOP)

## Screenshots

### Home Page
![Home Page](screenshots/home-page.png)

### Dashboard
![Dashboard](screenshots/dashboard.png)

### Analytics
![Analytics](screenshots/analytics.png)

### Budget Management
![Budget Management](screenshots/budget.png)

## Database Configuration

Database credentials are stored externally using a configuration file for improved security.

### Setup

1. Copy `config.properties.example` to `config.properties`
2. Update the values with your MySQL credentials

```properties
database.url=jdbc:mysql://localhost:3306/finance_manager?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
database.username=your-db-username
database.password=your-db-password
```

3. Ensure `config.properties` is not committed to GitHub.

## Running the Application

### Clone Repository

```bash
git clone <repository-url>
cd TrackIt-AI-Finance-Manager
```

### Build Project

```bash
mvn clean package
```

### Run Application

```bash
mvn exec:java
```

## Security

- Database credentials are stored outside source code.
- `config.properties` is excluded using `.gitignore`.
- `config.properties.example` is provided as a template.
- Sensitive information is not stored in the repository.

## Author

Rohit Kumar S