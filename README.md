## Account Dashboard
A Java-based financial dashboard application that uses JavaBeans, Singleton design pattern, MySQL database, and chart libraries (JFreeChart/XChart) to compute and visualize expense data.

## Features
- ExpensesCompute Bean
    - Non-visual JavaBean following standard conventions
    - Implements Serializable for persistence
    - Provides methods for yearly totals, monthly totals, category totals, averages, comparisons, and percentage changes

- Database Integration
    - MySQL schema with fields: year, month, category, amount
    - Singleton SQLConnection ensures only one connection instance

- Charts & Visualization
    - Line, bar, and pie charts generated using JFreeChart/XChart
    - Includes chart titles, X/Y axis labels, and legends

- Design Patterns
    - Singleton for database connection
    - JavaBean for computation logic

## Project Structure
![demo3](demo3.png)

## Sequence Diagram
![demo2](demo2.png)

## Required Methods
1. Yearly Total Expenses

Input: int[]

Output: int

2. Monthly Total Expenses

Input: int[]

Output: int

3. Yearly Total by Category

Input: int[]

Output: int

4. Average Monthly Expenses

Input: (int totalYearly, int months)

Output: double

5. Month-to-Month Comparison

Input: (int jan, int feb)

Output: int (difference)

6. Percentage Change Between Months

Formula: ((Feb - Jan) / Jan) × 100

## Demo
![demo](demo.png)

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

