### Purpose
1. To practice building a reusable, modular JavaBean that can handle financial records (income and expenses).
2. To apply CRUD operations (Create, Read, Update, Delete) with data persistence using serialization.

### Background
1. In Component-Based Software Engineering (CBSE), components must be reusable, stateful, and modular.
2. Your bean will manage financial records and computations while storing/retrieving data persistently.

### Objectives
By the end, you should be able to:
1. Develop a non-visual JavaBean following JavaBean conventions.
2. Implement CRUD operations for financial data.
3. Compute totals for income and expenses.
4. Implement persistence with the Serializable interface.
5. Manage data using arrays or ArrayList.
6. Generate documentation with Javadoc and package the component.

### Instructions
You must create:
1. Class name: com.ex.calculate.MoneyCalculatorBean

### Requirements:
1. No-argument constructor
2. Implements Serializable
3. Imports necessary packages
4. Acts as a non-visual component

### Required Methods
- computeTotalIncome(int[] income)
- Input: array of integers
- Output: integer (sum of all income values)
- Example: [100, 200, 300] → 600
- computeTotalExpenses(int[] expenses)
- Input: array of integers
- Output: integer (sum of all expense values)
- Example: [50, 30, 20] → 100
- computeAverageExpenses(int[] expenses, int newExpense)
- Input: existing array + new value
- Output: updated array with new expense added
- Example: [50, 30] + 20 → [50, 30, 20]