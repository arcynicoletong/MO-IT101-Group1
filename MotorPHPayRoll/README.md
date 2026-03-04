# MOTORPH PAYROLL SYSTEM (GROUP 1)

### Computer Programming 1 (Milestone 2)

### H1101 Group 1
- Anna Veronica Cortes  
- Arcy Nicole Tong  
- Gwyneth Quisora  
- John Kenneth Marta  
- Niña Fatima Bartolome  

---

This program handles profile viewing for MotorPH employees and payroll processing for MotorPH payroll staff. It reads employee details and attendance from two (2) CSV files, enforces MotorPH time rules to compute total hours worked, then calculates gross pay per cut-off, monthly government deductions, and the net salary per cut-off—all without rounding any values. It covers the MotorPH payroll from June to December 2024.

---

## What This Program Does

The program is designed to receive user input, read data from the source CSV files, and display information based on the user’s request/input. Once the program is run, it will show a simple header displaying the company’s name:

```
===================================
      MOTORPH PAYROLL SYSTEM       
===================================
```

From here, the user will be asked to log in using their credentials and choose their next desired action.

---

## User Workflow

Here's the step-by-step description of how the program works from the outsise (through the console).

### 1. User Log-in

**Valid Usernames:** employee | payroll_staff  
**Valid Password:** 12345  

Invalid credentials print:
```
Incorrect username and/or password.
```
and terminate the program.

### 2. Employee Session

Menu:
```
[1] View your employee information
[2] Exit the program
```

- Viewing employee information requires a valid employee number.
- Displays employee number, name, and birthday.
- If invalid, prints:
```
Employee number does not exist.
```

### 3. Payroll Staff Session

Menu:
```
[1] Process Payroll
[2] Exit the program
```

Payroll options:
```
[1] One employee
[2] All employees
[3] Exit the program
```

Payroll output includes:
- Employee information
- Monthly cutoff salary breakdown
- Deductions
- Net salary

## Notes

- Cutoff 1: 1–15
- Cutoff 2: 16–end of month
- Monthly deductions are applied only on the 2nd cutoff.
- Payroll runs from June–December 2024 only.

---

## How the Program Works

The team coded the program in a highly organized manner. It follows the common project standard where all the definitive and declarative methods come before the main method. The program is divided and sequenced into the following methods:

1. Display Employee Information  
2. Data Parsing  
3. Worked Hours Computation  
4. Unpaid Lunch Overlap Calculation  
5. SSS Calculation  
6. PhilHealth Calculation  
7. Pag-IBIG Calculation  
8. Withholding Tax Calculation  
9. Employee Session Handler  
10. Payroll Staff Session Handler  
11. Payroll Calculator  
12. Terminate Session  
13. Finalize Payroll  
14. Main Method  

---

## Package Declaration
The package declaration or the folder path for our project.

```java
package com.motorphpayroll.motorphpayroll;
```

---

## Imports
The following packages are imported to serve specific purposes with our code.

```java
import java.io.BufferedReader;				// This handles the CSV files so we can read them line by line.
import java.io.FileReader;					// This serves as the "key" that opens the file so the BuffereReader can work.
import java.time.Duration;					// This tool measures the exact gap (duration) between a clock-in and a clock-out time.
import java.time.LocalTime;					// We use this to create "time objects" so the program understands 8:00 AM is a time, not just text.
import java.time.YearMonth;					// This helps us handle calendar logic, like checking if a month has 30 or 31 days for the cutoff.
import java.time.format.DateTimeFormatter;	// This is our "translator" that turns text from the CSV (like "08:30") into a real Java time object.
import java.util.Scanner;					// As taught in our synchronous class, this is the tool  that lets the program read what the user types into the console.
```

---

## Main Class
This is the main class which holds the entirety of the program’s code

```java
public class MotorPHPayrollSystem { }
```

---

## Method Breakdown

### Method 1: Display Employee Information
This method displays employee information and is designed to be reused in payroll_staff sessions as employee salary information header.

```java
static void displayProfileHeader(String[] data) {
    System.out.println("===================================");
    System.out.println("EMPLOYEE INFORMATION");
    System.out.println("Employee #: " + data[0]);
    System.out.println("Employee Name: " + data[1] + ", " + data[2]);
    System.out.println("Birthday: " + data[3]);
}
```

---

### Method 2: Data Parsing
This method converts a String from the CSV file into a double number. It removes quotes and whitespace from the input to ensure correct parsing.

```java
static double tryParseDouble(String value) {
    try {
        return Double.parseDouble(value.replace(""", "").trim());
    } catch (Exception e) {
        return 0.0; // If data is missing/broken, we return 0.0 to prevent errors. 
    }
}
```

---

### Method 3: Worked Hours Computation
This method calculates the total hours worked by an employee during a shift. 
It strictly counts time between 8:00 AM and 5:00 PM and applies a 10-minute grace period. 
Logins before 8:11 AM are treated as 8:00 AM as instructed.

```java
static double computeHours(LocalTime loginTime, LocalTime logoutTime) {
    if (loginTime == null || logoutTime == null) return 0.0;

    // This sets the rule that our paid work hours are 8:00 AM - 5:00 PM only.    	
    LocalTime standardStart = LocalTime.of(8, 0);
    LocalTime standardEnd   = LocalTime.of(17, 0);
    LocalTime effectiveLogout = logoutTime.isAfter(standardEnd) ? standardEnd : logoutTime;

    // This sets the 10-minute grace-period.
    LocalTime gracePeriodLimit = LocalTime.of(8, 10);
    LocalTime effectiveLogin = loginTime.isBefore(standardStart) ? standardStart : loginTime;
    if (!loginTime.isAfter(gracePeriodLimit)) { effectiveLogin = standardStart; }

    // This block finds the total minutes between start and end, then calls 
    // Method 4 to see how much of that time was spent during lunch break.    	
    double lunchMinutes = calculateOverlap(effectiveLogin, logoutTime, LocalTime.of(12, 0), LocalTime.of(13, 0));
    
    // We subtract the unpaid lunch, then divide by 60 to get decimal hours.    	
    return (Duration.between(effectiveLogin, logoutTime).toMinutes() - lunchMinutes) / 60.0;
}

```

---

### Method 4: Unpaid Lunch Overlap Calculation
This method determines the number of minutes where two (2) time intervals overlap (i.e. the whole day worked hours and the 1-hour unpaid lunch).
Our team used this method to avoid improper time deduction.

```java
static double calculateOverlap(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {

    // This block finds the latest start and earliest end to determine the shared time.
    LocalTime maxStart = startA.isAfter(startB) ? startA : startB;
    LocalTime minEnd   = endA.isBefore(endB) ? endA : endB;

    // If the earliest end is after the latest start, they overlapped. Meaning, part of
    // the hours worked of an employee’s shift is under the unpaid lunch time, and we have to deduct it from their total time.
    if (minEnd.isAfter(maxStart)) {
        return Duration.between(maxStart, minEnd).toMinutes();
    }

	// If they didn't touch, we return 0 minutes.
	return 0.0;
} 

```

---

### Method 5: SSS Calculation  
This methdod computes the monthly SSS contribution based on gross salary. It utilizes the bracketed contribution table and enforces a maximum cap.

```java
static double computeSSS(double monthlyGross) {
   
	// If they didn't earn anything, they pay nothing. 
	// If they earned below the 3,250 floor, they pay the minimum 135.00.
	if (monthlyGross <= 0) return 0.0;
	if (monthlyGross < 3250) return 135.00;

	// We defined these by finding the arithmetic logic behind the 2024 SSS Table.
	// Every 500 pesos (bracketStep) the contribution increases by 22.50 (incrementPerStep).
    double baseLowerBound = 3250.0;
    double baseContribution = 157.50;
    double bracketStep = 500.0;
    double incrementPerStep = 22.50;
    double maximumContribution = 1125.00;

    // We find how much the salary exceeds the "floor" (3,250).
    double difference = monthlyGross - baseLowerBound;
    
    // We divide that difference by 500 to see how many "steps" they've climbed.
    // We use Math.floor and (int) to make sure we only count full 500-peso brackets.
    int bracketsEarned = (int) Math.floor(difference / bracketStep);
    
    // We start at the base (157.50) and add 22.50 for every step earned.
    double totalContribution = baseContribution + (bracketsEarned * incrementPerStep);

    // This sets the maximum contribution to 1,125.00.
    return Math.min(totalContribution, maximumContribution);
}

```

---

### Method 6: PhilHealth Calculation  
This method calculates the PhilHealth premium share for an employee. It applies a 3% premium rate with a minimum cap of 300 and a maximum of 1800.

```java
static double computePhilHealth(double monthlyGross) {

	// If there's no income, there's no deduction.
	if (monthlyGross <= 0) {
		return 0.0;
	}
	
	// As per the PhilHealth guidelines, the total monthly premium is 3% of the gross.
	double premium = monthlyGross * 0.03;
	
	// This sets the minimum 300-peso floor
	if (premium < 300.0) {
		premium = 300.0;
	}
	
	// This sets the 1,800-peso ceiling. Even if 3% is higher, the maximum is capped.
	if (premium > 1800.0) {
		premium = 1800.0;
	}
	
	// The total premium is shared 50/50 between the employer and employee.
	// We return only the 50% share to be deducted from the employee's pay.
	return premium / 2.0;
}

```

---

### Method 7: PagIBIG Calculation
This method calculates the Pag-IBIG contribution for an employee. It applies tiered rates based on income level and caps at 100 pesos.

```java
static double computePagIbig(double monthlyGross) {

	// If there’s no income, there’s no contribution.
	if (monthlyGross <= 0) {
		return 0.0;
	}
	
	// This sets the Pag-IBIG rate based on the salary bracket 
	double contributionRate;
	
	if (monthlyGross <= 1500) {
		// For salaries 1,500 and below, the rate is 1%.
	   	 contributionRate = 0.01;
	} else {
	   	// For salaries above 1,500, the rate increases to 2%.
	    contributionRate = 0.02;
	}
	
	// This is the initial calculation of the employee's share.
	double calculatedShare = monthlyGross * contributionRate;
	
	// This sets the 100-peso ceiling for the employee contribution.
	if (calculatedShare > 100.0) {
		/* Even if the math says the share is 500, the rule caps the 
	    employee's monthly deduction at 100. */
	    return 100.0;
	} else {
	return calculatedShare;
	}
}


```

---

### Method 8: Withholding Tax Calculation  
This method determines the monthly withholding tax using the tax table. Tax is computed on income remaining after statutory deductions.

```java
static double computePhilHealth(double monthlyGross) {

	// If there's no income, there's no deduction.
	if (monthlyGross <= 0) return 0.0;

	// As per PhilHealth guidelines, the total monthly premium is 3% of the monthly gross.
	double premium = monthlyGross * 0.03;

	// Sets the minimum (PHP 300) and maximum (PHP 1800) premium.
	if (premium < 300.0) premium = 300.0;
	if (premium > 1800.0) premium = 1800.0;

	// The total premium is shared 50/50 between the employer and employee.
	return premium / 2.0;
}

static double computeWithholdingTax(double taxableIncome) {

	// If the income is 20,833 or less, the employee is tax-exempt.
	if (taxableIncome <= 20833) {
    	return 0.0;
	} 

	// If income is above 20,833 but below 33,333. We tax 20% of the amount exceeding 20,833.
	else if (taxableIncome < 33333) {
		return (taxableIncome - 20833) * 0.20;
	} 

	// If the income is above 33,333 but below 66,667.
	else if (taxableIncome < 66667) {
    	// Fixed tax of 2,500 plus 25% of the excess over 33,333.
		return 2500 + (taxableIncome - 33333) * 0.25;
	} 

	// If the income is above 66,667 but below 166,667. We impose a fixed tax of 10,833 plus 30% of the excess over 66,667.
	else if (taxableIncome < 166667) {
		return 10833 + (taxableIncome - 66667) * 0.30;
	} 

	// If income is above 166,667 but below 666,667. We impose a fixed tax of 40,833.33 plus 32% of the excess over 166,667.
	else if (taxableIncome < 666667) {
		return 40833.33 + (taxableIncome - 166667) * 0.32;
	} 

	// If the income is 66,667 and above. We impose a fixed tax of 200,833.33 plus 35% of the excess over 666,667.
	else {
		return 200833.33 + (taxableIncome - 666667) * 0.35;
	}
}

```

---

### Method 9: Employee Session Handler 
This method manages the interactive menu for users with the "employee" role. It handles profile retrieval and secure logout procedures.

```java
static void handleEmployeeSession(Scanner scanner, String employeeFilePath) {
    
	// These flags keep the menu running and track if the user already saw their info.
	boolean sessionActive = true;
	boolean hasViewedInformation = false;

	// This loop keeps the employee inside the system until they choose to 'Exit'.
	while (sessionActive) {
		System.out.println("\nPlease enter the number of your desired action.");
        
		// If they haven't viewed their info yet, we show the full menu.
		if (!hasViewedInformation) {
			System.out.println("[1] View your employee information");
			System.out.println("[2] Exit the program");
			System.out.print("Enter option: ");
			String menuSelection = scanner.nextLine().trim();

			if (menuSelection.equals("1")) {
				System.out.print("Enter Employee #: ");
				String targetId = scanner.nextLine().trim();
				boolean employeeFound = false;

				// We open the CSV file to search for the specific employee ID.
				try (BufferedReader reader = new BufferedReader(new FileReader(employeeFilePath))) {
					reader.readLine(); // We skip the header row of the CSV.
					String row;
                    
					// We read the file line by line to find a match.
                    while ((row = reader.readLine()) != null) {
	                    String[] dataFields = row.split(",");
                        
                        // If the ID matches, we call Method 1 to show the profile.
                        if (dataFields[0].equals(targetId)) {
                            displayProfileHeader(dataFields);
                            System.out.println("===================================");
                            System.out.println("Request to view employee information is successful.");
                            employeeFound = true;
                            hasViewedInformation = true; // Mark as viewed to simplify the menu next time.
                            break;
						}
					}
					reader.close();

				} catch (Exception e) { 
					System.out.println("Error accessing employee records."); 
				}

				if (!employeeFound) {
					System.out.println("Employee number does not exist.");
				}
                
			} else if (menuSelection.equals("2")) {
               	// This triggers the logout message and breaks the loop.
               	terminateSession();
				sessionActive = false;
            }
		} else {
			// Once information is viewed, we only show the 'Exit' option.
            System.out.println("[1] Exit the program");
            System.out.print("Enter option: ");
            if (scanner.nextLine().trim().equals("1")) {
                terminateSession();
                sessionActive = false;
            }
        }
    }
}

```

---

### Method 10: Payroll Staff Session Handler 
This method manages the interactive menu for users with the "payroll_staff" role. It provides processing options for single or bulk employee payroll.


```java
static void handleStaffSession(Scanner scanner, String empFile, String attFile, DateTimeFormatter timeFormat) {
	boolean sessionActive = true;
    
	// The main loop keeps the staff member logged in until they choose 'Exit'.
	while (sessionActive) {
		System.out.println("\nPlease enter the number of your desired action.");
		System.out.println("[1] Process Payroll");
		System.out.println("[2] Exit the program");
		System.out.print("Enter option: ");
		String staffChoice = scanner.nextLine().trim();

		
		if (staffChoice.equals("1")) {
            			
			// Nested loop: Once 'Process Payroll' is picked, we open a sub-menu.
			boolean payrollModeActive = true;
			while (payrollModeActive) {
				System.out.println("\nPlease enter the number of your desired action.");
				System.out.println("[1] One employee");
				System.out.println("[2] All employees");
				System.out.println("[3] Exit the program");
				System.out.print("Enter option: ");
				String subChoice = scanner.nextLine().trim();

					// OPTION 1: Process payroll for One employee.
					if (subChoice.equals("1")) {
                    	System.out.print("Enter Employee #: ");
                    	String id = scanner.nextLine().trim();
						boolean found = false;
						try (BufferedReader reader = new BufferedReader(new FileReader(empFile))) {
                        	reader.readLine(); // Skip header
                        	String row;
                        	while ((row = reader.readLine()) != null) {
								String[] data = row.split(",");
								if (data[0].equals(id)) {
                                	// Once found, we call Method 11 to handle the math.
                                	executePayrollLogic(data, attFile, timeFormat);
                                	found = true; break;
								}
                        	}
                    	} catch (Exception e) { System.out.println("Record access error."); }
                    
                    	if (!found) System.out.println("Employee number does not exist.");
                    	else { finalizePayrollProcess(); } // Show success message
                	} 
                
					// OPTION 2: Process payroll for All employee.
                	else if (subChoice.equals("2")) {
                    	try (BufferedReader reader = new BufferedReader(new FileReader(empFile))) {
                        	reader.readLine(); String row;

                        	// Instead of searching, we just loop through every single row in the CSV.
                        	while ((row = reader.readLine()) != null) {
                            	executePayrollLogic(row.split(","), attFile, timeFormat);
                        	}
                        	finalizePayrollProcess();
                    	} catch (Exception e) { System.out.println("Record access error."); }
                	} 
                
					// OPTION 3: Exit the program.
                	else if (subChoice.equals("3")) {
                    	terminateSession();
                    	payrollModeActive = false; sessionActive = false;
                	}
            	}
		// If the user chooses to exit the program instead of processing any payroll.
		} else if (staffChoice.equals("2")) {
			terminateSession();
            sessionActive = false;
		}
	}
}

```

---

### Method 11: Payroll Calculator  
This method executes the mathematical processing of employee earnings. It connects the attendance records with all the previous math methods (i.e. the calculators for SSS, PhilHealth, Pag-IBIG, and Tax)

```java
static void executePayrollLogic(String[] employeeInfo, String attendanceFile, DateTimeFormatter timeFormat) {
    
	// We read the employee number and the Hourly Rate (the last column in the CSV).
	String empId = employeeInfo[0];
    double hourlyRate = tryParseDouble(employeeInfo[employeeInfo.length - 1]);
    
   	// We display the header (Method 1) so we know exactly whose salary we are looking at.
    displayProfileHeader(employeeInfo);
    System.out.println("---------------------------------");
    System.out.println("EMPLOYEE SALARY");

    /* This loop iterates through months 6 to 12 (June to December). 
     * It calculates the pay for every month in that range. */
   	for (int month = 6; month <= 12; month++) {
		double cutoffOneHours = 0.0, cutoffTwoHours = 0.0;
        
        // We open the Attendance CSV to find every login/logout for this specific employee.
        try (BufferedReader attReader = new BufferedReader(new FileReader(attendanceFile))) {
        	attReader.readLine(); // Skip header
            String row;
            while ((row = attReader.readLine()) != null) {
            	String[] attData = row.split(",");
                
                // If the attendance row doesn't belong to this employee, we skip it.
                if (!attData[0].equals(empId)) continue;
                
                String[] dateParts = attData[3].split("/"); // Breaking down MM/DD/YYYY
                
                // We only count records for the year 2024 and the current month in the loop.
                if (Integer.parseInt(dateParts[2]) == 2024 && Integer.parseInt(dateParts[0]) == month) {
                	// Call Method 3 to calculate hours for this specific day.
                    double dailyHours = computeHours(LocalTime.parse(attData[4], timeFormat), LocalTime.parse(attData[5], timeFormat));
                    
                    // Split the hours into 1st Cutoff (1-15) or 2nd Cutoff (16-End).
                    if (Integer.parseInt(dateParts[1]) <= 15) cutoffOneHours += dailyHours; 
                    else cutoffTwoHours += dailyHours;
				}
            }
        } catch (Exception e) { continue; }

		// Logic for converting the month number into a readable name (e.g., 6 -> June).
        String monthName = switch (month) { case 6->"June"; case 7->"July"; case 8->"August"; case 9->"September"; case 10->"October"; case 11->"November"; case 12->"December"; default->""; };
        
		// Math: Gross Pay = Total Hours * Hourly Rate.
        double grossOne = cutoffOneHours * hourlyRate;
        double grossTwo = cutoffTwoHours * hourlyRate;
        double monthlyGross = grossOne + grossTwo;

        // We call Methods 5, 6, 7, and 8 to get the statutory deductions based on total monthly income.
        double sss = computeSSS(monthlyGross);
        double philhealth = computePhilHealth(monthlyGross);
        double pagibig = computePagIbig(monthlyGross);
        double tax = computeWithholdingTax(Math.max(0, monthlyGross - (sss + philhealth + pagibig)));
        double totalDeductions = sss + philhealth + pagibig + tax;

        // This prints the final report. 
        // Note: Per company policy, all deductions are taken out of the 2nd cutoff pay.
        System.out.println("\nCutoff Date: " + monthName + " 1 to 15\nTotal Hours: " + cutoffOneHours + " hours\nGross Salary: PHP " + grossOne + "\nNet Salary: PHP " + grossOne);
        System.out.println("\nCutoff Date: " + monthName + " 16 to " + YearMonth.of(2024, month).lengthOfMonth() + "\nTotal Hours: " + cutoffTwoHours + " hours\nGross Salary: PHP " + grossTwo + "\nEach Deduction:\n    SSS: PHP " + sss + "\n    PhilHealth: PHP " + philhealth + "\n    Pag-IBIG: PHP "  + pagibig + "\n    Tax: PHP " + tax + "\nTotal Deductions: PHP " + totalDeductions + "\nNet Salary: PHP " + (grossTwo - totalDeductions));
	}
}

```

---

### Method 12: Terminate Session
This method simply terminates the prorgam but the team decided to turn it into its own method to print a short message and make it reusable.

```java
static void terminateSession() {
	System.out.println("\nClosing the program . . .");
}

```

---

### Method 13: Finalize Payroll
This method prints a short message to inform the user that the payroll processing is successful. It is also designed to be a reusable method.

```java
static void finalizePayrollProcess() {
	System.out.println("Payroll processing is successful!");
	System.out.print("\nProcess another payroll. ");
}

```

---

### Method 14: Main Method 
The main method is the program's entry point which initializes file paths, handles the login authentication, and routes the user to the appropriate session handler based on their credentials.

```java
public static void main(String[] args) {
        
	// This block defines internal resource paths for our CSV files.
    String employeeDetailsPath = "resources/MotorPH - Employee Details.csv";
    String attendanceRecordsPath = "resources/MotorPH - Attendance Record.csv";
        
    // This block initialize input scanner and time format for processing.
    Scanner inputScanner = new Scanner(System.in);
    DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("H:mm");

    //  This displays a header once at the very start of program execution.
    System.out.println("===================================");
    System.out.println("      MOTORPH PAYROLL SYSTEM       ");
    System.out.println("===================================");

    // This displays user login field.
    System.out.print("Enter username: ");
    String userEntry = inputScanner.nextLine().trim();
    System.out.print("Enter password: ");
    String passEntry = inputScanner.nextLine().trim();

    // This is the Authentication Logic which will terminate the program if credentials do not match
    if (!((userEntry.equals("employee") || userEntry.equals("payroll_staff")) && passEntry.equals("12345"))) {
    	System.out.println("Incorrect username and/or password.");
        return; // Ends the main method, thus closing the application
	}

	// Lastly, this is logic will redirect user validated user based on their role (either employee of staff).
    System.out.println("\nLog in successful!");
    if (userEntry.equals("employee")) {
            handleEmployeeSession(inputScanner, employeeDetailsPath);
	} else {
            handleStaffSession(inputScanner, employeeDetailsPath, attendanceRecordsPath, hourFormat);
    }
}

```

---

## Final Notes

- No rounding is applied to any calculations.
- Time paid window is strictly 08:00–17:00.
- Payroll is always processed from June–December 2024.
- No OOP concepts were applied in this implementation (because the team does not know anythign either >.<).
  
---

## CSV Reference
The data files are in CSV format and must be placed inside the Resources folder.

- **MotorPH - Employee Details.csv**  
  Contains: Employee Number, Last Name, First Name, Birthday, …, Gross Semi-monthly Rate, and Hourly Rate

- **MotorPH - Attendance Record.csv**  
  Contains: Employee Number, Last Name, First Name, Attendance Date (MM/DD/YYYY), Log In and Log Out Time (H:mm | 24-hour format)
  
---

## Project Plan

The team have had many challenges in aligning their schedules and learning paces together, but remained as one to meet the MotorPH's project deadline. Below is the latest project plan of the team, updated as of March 05, 2022.

Project Plan link: https://docs.google.com/spreadsheets/d/1Lux9k8_aYuvp0zqG6S2VvVciuUxU-RZNWELsfzmeJYs/edit?usp=drive_link






