# **MotorPH Basic Payroll (Group 1)**



CP1 (Milestone 2)



This program reads employee details and attendance from CSV files, enforces MotorPH time rules to compute hours per cutoff, then calculates gross, government deductions (computed monthly from the combined cutoffs), and net pay - with no rounding. It covers June to December 2024. It also includes a login that branches into employee and payroll\_staff menus.



 	CSV structure reference:

 		• MotorPH - Employee Details.csv includes: Employee #, Last Name, First Name, Birthday, …, Gross Semi-monthly Rate, Hourly Rate (hourly rate is the last column)

 		• MotorPH - Attendance Record.csv includes: Employee #, Last Name, First Name, Date, Log In, Log Out with Date = MM/DD/YYYY, Time = H:mm (24‑hour), spanning June–December 2024.



### **What this program does**

1. Login:
   • Valid usernames: employee, payroll\_staff
   • Password: 12345
   • Wrong credentials → prints Incorrect username and/or password. and terminates.
2. If username = employee
   • Menu:

   1. Enter your employee number → shows Employee Number, Employee Name, Birthday (no hourly rate).
   2. Exit the program → terminate.

3. If username = payroll\_staff

   • Menu:

 	1. Process Payroll

 	2. Exit the program

   • Under Process Payroll

 	1. One employee → asks employee # (or prints Employee number does not exist.)

 	2. All employees → iterates through every employee in Employee Details.csv

 	3. Exit the program

   • For each employee (and for each month, June–December 2024):

 	• Reads attendance from CSV (matching Employee #, month, year).

 	• Computes hours per day with MotorPH rules (see Hours Logic below).

 	• Sums Cutoff 1 (1–15) and Cutoff 2 (16–end).

 	• Computes gross per cutoff and monthly deductions from (gross1 + gross2).

 	• Applies all deductions only to the 2nd cutoff (as required).

 	• Prints both cutoffs, with a full deduction breakdown on the second cutoff.



### **Why this design fits a “basic payroll system” for MotorPH**

* CSV‑driven (no hardcoded employees or attendance). The employee CSV contains hourly rate as the last column and biographical fields; attendance has date/time per day.
* Company time rules respected: 8:00–17:00 window; grace up to 8:05; lunch deduction only when overlapping 12:00–13:00.
* No rounding anywhere—outputs preserve exact computed doubles.
* Cutoff handling: First compute monthly gross by adding both cutoffs, then compute SSS/PhilHealth/Pag‑IBIG/Tax on that monthly total; apply all deductions on the second cutoff only (per instructions).
* One Java file, static methods, simple loops, String.split(",") parsing; no OOP required.



### **How the program works (step-by-step)**

1. Imports

import java.io.BufferedReader;

import java.io.FileReader;

import java.time.Duration;

import java.time.LocalTime;

import java.time.YearMonth;

import java.time.format.DateTimeFormatter;

import java.util.Scanner;

 	• BufferedReader/FileReader – read CSV files line by line.

 	• LocalTime/Duration – robust time math for clamping and lunch overlap.

 	• YearMonth – get the correct number of days in a month.

 	• DateTimeFormatter – parse H:mm times (e.g., 8:05, 17:00).

 	• Scanner – read console input for login/menu choices and Employee #.

 

 	CSV formats used:

 		• Employees: includes Hourly Rate as final column (.csv file of Employee Details).

 		• Attendance: Date uses MM/DD/YYYY; Log In/Log Out use H:mm.



2\. File paths and login
String empFile  = "resources/MotorPH - Employee Details.csv";

String attFile  = "resources/MotorPH - Attendance Record.csv";



Scanner sc = new Scanner(System.in);

System.out.print("Enter username: ");

String username = sc.nextLine().trim();

System.out.print("Enter password: ");

String password = sc.nextLine().trim();



if (!((username.equals("employee") || username.equals("payroll\_staff")) \&\& password.equals("12345"))) {

    System.out.println("Incorrect username and/or password.");

    return;

}

• Method style: Code in main using simple conditional checks (no OOP).

• Why essential: Enforces role‑based flow before reading/printing sensitive payroll details.



3\. Employee menu (non‑payroll view)

* If the user is employee, the program:
* Prints the menu.
* On Enter employee number, it scans the Employee CSV (skips header), matches the Employee #, and prints Employee Number, Employee Name, Birthday.
* If not found → prints Employee number does not exist.
* No hourly rate displayed in this view (per instruction).



• Method style: CSV read loop with BufferedReader, split(",") in main.

• Why essential: Satisfies the employee role requirements without exposing payroll figures.



Employee CSV includes these fields, with Hourly Rate as the last column used later by payroll staff.



4\. Payroll staff menus (payroll view)

• If the user is payroll\_staff, the program shows:

&nbsp;	• Main: 1. Process Payroll or 2. Exit

&nbsp;	• Sub: 1. One employee, 2. All employees, 3. Exit

• One employee:

&nbsp;	• Prompts for Employee #, loads Last Name, First Name, Birthday, Hourly Rate (last column), then processes June–December.

• All employees:

&nbsp;	• Iterates through every row of Employee Details.csv and processes each employee for June–December.



• Method style: Nested loops in main, basic conditionals, repeated CSV scans per month.

• Why essential: Implements the exact role/option structure required by your professor.



5\. Time parsing \& month loop

DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("H:mm");



for (int month = 6; month <= 12; month++) {

&nbsp;   int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

&nbsp;   double cut1 = 0.0; // hours for days 1–15

&nbsp;   double cut2 = 0.0; // hours for days 16–end

&nbsp;   ...

}



• Method style: Loop variables and arithmetic in main.

• Why essential: Ensures June–December are always displayed and computed.



6\. Reading attendance \& totaling hours per cutoff

• For each month, the program re‑opens the attendance CSV, and for each row:

&nbsp;	• Checks the Employee # and Date (MM/DD/YYYY) to match the current employee and month.

&nbsp;	• Parses Log In and Log Out using timeFmt.

&nbsp;	• Computes hours with computeHours(in, out).

&nbsp;	• Adds to cut1 if day ≤ 15, otherwise to cut2.



• Method style: Simple CSV scanning loop + basic if filters.

• Why essential: Creates accurate per‑cutoff totals used in salary math.



**Hours logic (No rounding)
static double computeHours(LocalTime in, LocalTime out)**


// Count only within 08:00–17:00, apply 5-min grace, subtract lunch overlap.

static double computeHours(LocalTime in, LocalTime out) {

&nbsp;   if (in == null || out == null) return 0.0;



&nbsp;   LocalTime dayStart = LocalTime.of(8, 0);

&nbsp;   LocalTime dayEnd   = LocalTime.of(17, 0);



&nbsp;   if (out.isAfter(dayEnd)) out = dayEnd;



&nbsp;   LocalTime effectiveIn = in.isBefore(dayStart) ? dayStart : in;



&nbsp;   // 5-minute grace: 08:00–08:05 treated as 08:00 (not late)

&nbsp;   LocalTime grace = LocalTime.of(8, 5);

&nbsp;   if (!in.isAfter(grace)) effectiveIn = dayStart;



&nbsp;   if (!out.isAfter(effectiveIn)) return 0.0;



&nbsp;   double totalMinutes = Duration.between(effectiveIn, out).toMinutes();



&nbsp;   // Deduct only actual overlap with 12:00–13:00

&nbsp;   double lunchOverlap = overlapMinutes(effectiveIn, out, LocalTime.of(12,0), LocalTime.of(13,0));



&nbsp;   double workedMinutes = totalMinutes - lunchOverlap;

&nbsp;   if (workedMinutes < 0) workedMinutes = 0.0;



&nbsp;   return workedMinutes / 60.0; // exact decimal hours, no rounding

}



• Method kind: static utility method.

• Why essential: Encapsulates all the timing rules in one place so the rest of the code can stay beginner‑simple:

&nbsp;	• 8:00–17:00 window only (no overtime counted).

&nbsp;	• ≤ 8:05 treated as 8:00 (grace period).

&nbsp;	• Lunch deduction is precise—subtract only the minute overlap with 12:00–13:00 (e.g., 11:40–12:20 deducts 20 minutes).

&nbsp;	• Returns minute‑accurate decimal hours (satisfies “no rounding” policy).



====



**static double overlapMinutes(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2)**

static double overlapMinutes(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2) {

&nbsp;   LocalTime s = a1.isAfter(b1) ? a1 : b1; // later start

&nbsp;   LocalTime e = a2.isBefore(b2) ? a2 : b2; // earlier end

&nbsp;   if (e.isAfter(s)) return Duration.between(s, e).toMinutes();

&nbsp;   return 0.0;

}



• Method kind: static helper method.

• Why essential: Precisely computes minutes of overlap between two time windows. computeHours uses it to remove only the actual lunch overlap.



### **Payroll computation (monthly deductions from combined cutoffs)**

For each month:
	a. Gross per cutoff

 	double gross1 = cut1 \* hourlyRate;

 	double gross2 = cut2 \* hourlyRate;

 

 	b. Monthly gross (sum both cutoffs first)

 	double monthlyGross = gross1 + gross2;



 	c. Monthly deductions (computed from monthlyGross)

 	• SSS (bracket-based function)

 	• PhilHealth (3% premium, clamped 300–1800; employee share is 50%)

 	• Pag-IBIG (1% for 1,000–1,500; 2% if >1,500; cap 100/month)

 	• Withholding Tax (monthly tax table based on taxable income)

 	double sssMonthly  = computeSSS(monthlyGross);

&nbsp;	double philMonthly = computePhilHealthEmployeeShare(monthlyGross);

&nbsp;	double pagibigMon  = computePagIbig(monthlyGross);



&nbsp;	double taxableMonthly = monthlyGross - (sssMonthly + philMonthly + pagibigMon);

&nbsp;	if (taxableMonthly < 0) taxableMonthly = 0.0;



&nbsp;	double wtaxMonthly = computeWithholdingTax(taxableMonthly);

&nbsp;	double totalDeductions = sssMonthly + philMonthly + pagibigMon + wtaxMonthly;

 	

&nbsp;	d. Net per cutoff (apply deductions ONLY on 2nd cutoff)

 	double net1 = gross1;                     // First payout: no deductions

&nbsp;	double net2 = gross2 - totalDeductions;   // Second payout: all deductions



• Method style: Arithmetic in main using static helper methods for deductions.

• Why essential: Matches the instruction: add 1st + 2nd cutoff amounts first, compute monthly deductions, and apply on second payout only.



### **Deductions methods**

The program uses basic formulas to keep the code single‑file and beginner‑level. If your professor later provides official tables, these can be adapted to CSV without changing the overall structure.



**static double computeSSS(double monthlySalary)**

Bracket math with a cap; returns the employee share per month.



**static double computePhilHealthEmployeeShare(double monthlySalary)**

* 3% monthly premium
* Clamp 300–1800
* Employee share is 50% of premium



**static double computePagIbig(double monthlySalary)**

* 1% for 1,000–1,500
* 2% for > 1,500
* Cap at 100 per month



**static double computeWithholdingTax(double taxableMonthly)**

Simple monthly tax table applied to taxableMonthly = monthlyGross − (SSS + PH + Pag‑IBIG).



• Method kind: static helpers

• Why essential: Keeps the main flow readable while fulfilling the requirement to break down deductions (SSS, PhilHealth, Pag‑IBIG, Tax) on the second cutoff.



### **Output**

For each month (June–December):

* Cutoff 1 (1–15):

&nbsp;	• Total Hours Worked

&nbsp;	• Gross Salary

&nbsp;	• Net Salary (no deductions applied here)



* Cutoff 2 (16–end):

&nbsp;	• Total Hours Worked

&nbsp;	• Gross Salary

&nbsp;	• Each Deduction: SSS, PhilHealth, Pag‑IBIG, Tax

&nbsp;	• Total Deductions

&nbsp;	• Net Salary (gross2 − total deductions)



Why essential: It displays and follows rules, and clearly shows that all deductions hit the second payout only.



### **Notes**:

* No rounding applied anywhere (hours, gross, deductions, net).
* Time window strictly 08:00–17:00, 5‑minute grace for logins up to 08:05, lunch overlap (12:00–13:00) is deducted only when it actually overlaps.
* June–December 2024 are always processed and displayed.
* CSV parsing uses String.split(",") (sufficient for the course dataset).
* If a time field is invalid in the dataset, Java will throw a parsing error—your provided CSVs follow MM/DD/YYYY and H:mm formats.



### **Why each part was coded this way**

* Single file / No OOP.
* java.time API: Safe, modern time math; makes grace periods, clamping, and lunch overlap exact.
* Minute‑accurate → decimal hours: Works cleanly with the no rounding rule.
* Monthly deductions from combined gross (payout applied on cutoff 2): Mirrors the instruction precisely while still showing both cutoff results clearly.
* CSV‑driven: Uses the real dataset you supplied—no hardcoded employees or times.



### **How to run**

1. Run the Java file.
2. Log in as either:

&nbsp;	• employee / 12345

&nbsp;	• payroll\_staff / 12345

3\. Follow the on‑screen menu.

