## Account Dashboard
Account Dashboard is a Java application that combines JavaBeans, Swing, and MySQL to manage and visualize expense data. At its core is a non‑visual JavaBean (ExpensesCompute) that handles financial computations and persistence. The system uses a Singleton SQLConnection for database access and external chart libraries (JFreeChart/XChart) to generate clear, labeled graphs of spending trends.

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

## Demo
[![Download](https://img.shields.io/badge/Download-v1.2.2-blue?style=flat-square)](https://github.com/zilitye/AccountDashboard/releases/download/v1.2.2/AccountDashboard.jar)

Run demo with `java -jar AccountDashboard.jar` (or double‑click it). Requires Java 8 or newer.

![demo](.\media\graph2.png)

## Sequence Diagram
![sequenceDiagram](.\media\sequenceDiagram.png)

## JavaDoc
```
& "C:\Program Files\Java\jdk-24\bin\javadoc.exe" `
-d docs `
-sourcepath src `
-subpackages com:chart `
-classpath "lib/*;."
```
## References
1. Singleton Design Pattern, Retrieved from 
https://sourcemaking.com/design_patterns/singleton
2. JFreeChart, Retrieved from http://www.jfree.org/jfreechart/
3. XChart, Retrieved from https://knowm.org/open-source/xchart/
4. Flatlaf, Retrieved from https://github.com/JFormDesigner/FlatLaf

