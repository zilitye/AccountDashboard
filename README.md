# MoneyCalculatorBean
Purpose
To practice building a reusable, modular JavaBean that can handle financial records (income and expenses).
To apply CRUD operations (Create, Read, Update, Delete) with data persistence using serialization.

Background
In Component-Based Software Engineering (CBSE), components must be reusable, stateful, and modular.
Your bean will manage financial records and computations while storing/retrieving data persistently.

Objectives
By the end, you should be able to:
Develop a non-visual JavaBean following JavaBean conventions.
Implement CRUD operations for financial data.
Compute totals for income and expenses.
Implement persistence with the Serializable interface.
Manage data using arrays or ArrayList.
Generate documentation with Javadoc and package the component.

Instructions
You must create:
Class name: com.ex.calculate.MoneyCalculatorBean
Requirements:
No-argument constructor
Implements Serializable
Imports necessary packages
Acts as a non-visual component

Required Methods
computeTotalIncome(int[] income)
Input: array of integers
Output: integer (sum of all income values)
Example: [100, 200, 300] → 600
computeTotalExpenses(int[] expenses)
Input: array of integers
Output: integer (sum of all expense values)
Example: [50, 30, 20] → 100
computeAverageExpenses(int[] expenses, int newExpense)
Input: existing array + new value
Output: updated array with new expense added
Example: [50, 30] + 20 → [50, 30, 20]