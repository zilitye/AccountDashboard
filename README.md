## Account Dashboard
The application must be construct using database, design pattern, component (i.e. 
ExpensesCompute bean) and program library (i.e. Chart library):  
- ExpensesCompute bean is a pre-fabricated bean to calculate relevant spending 
information (Appendix 1) 
- Database to store a dataset of year, month, expenses category and amount spend as 
shown in Table 1. 
- Use Singleton [\[1\]](https://sourcemaking.com/design_patterns/singleton) design pattern for the SQLConnection class to ensure only one 
instance of connection is established to the database. Refers Appendix 2.  
- Use program library (e.g. JFreeChart [2], XChart [3] etc.) to generate graph to represents 
the dataset in Lab 1 by making query from Lab 1 repository. The graph must include chart 
title, label axis x and y.

## Demo
![demo](demo.png)

## Objectives
1. Develop a non-visual JavaBean following standard JavaBean conventions 
2. Implement persistence using Serializable interface 
3. Manage data using arrays or ArrayList 
4. Generate documentation using Javadoc and package the component 

## Instructions
develop a JavaBean named: com.ex.calculate.ExpensesCompute

The bean must: 
- Have a no-argument constructor 
- Implement the Serializable interface 
- Import necessary packages 
- Act as a non-visual component

## Required Methods
1. Yearly Total Expenses  → Sum of all expenses for the year 
Parameter: array of integers 
Return: integer 
Example: [100, 200, 300] → total = 600 
2. Monthly Total Expenses → Total expenses for January, February, etc. 
Parameter: array of integers 
Return: integer 
Example: [100, 200, 300] → total = 600 
3. Yearly Total by Category → Example: Total Food & Beverages in 2026 
Parameter: array of integers 
Return: integer 
Example: [100, 200, 300] → total = 600 
4. Average Monthly Expenses → Total yearly / number of months 
Parameter: integer, integer 
Return: double 
Example: [1000, 4] → average monthly = 250 
5. Month-to-Month Comparison 
→ Difference between January and February 
→ (Increase / Decrease) 
6. Percentage Change Between Months 
→ ((Feb – Jan) / Jan) × 100 

## Submission
Submission Material in ONE zipped folder, which label with your MATRIX No.: 
1. Sequence diagram 
2. Program codes & execution files (*.jar) – zipped project folder 
3. Export MySQL Database Schema (structure & data) 
4. Screenshot of graphs 

## References
1. Singleton Design Pattern, Retrieved from 
https://sourcemaking.com/design_patterns/singleton, March 2015 
2. JFreeChart1.5.6, Retrieved from http://www.jfree.org/jfreechart/ 
3. XChart, Retrieved from https://knowm.org/open-source/xchart/

