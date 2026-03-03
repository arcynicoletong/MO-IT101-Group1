package com.motorphpayroll.motorphpayroll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * MotorPH Payroll System - Final Optimized Version for Group 1.
 * This program handles employee authentication, profile viewing, and 
 * monthly payroll processing using procedural programming principles.
 * * @author Group1
 */
public class MotorPHPayrollSystem {

    /* ---------------- METHOD 1: WORKED HOURS COMPUTATION ---------------------
     * This method calculates the total hours worked by an employee during a shift.
     * It strictly counts time between 8:00 AM and 5:00 PM and applies a 10-minute grace period.
     * Logins before 8:11 AM (even though late) are treated as 8:00 AM.
    */
    static double computeHours(LocalTime loginTime, LocalTime logoutTime) {
        if (loginTime == null || logoutTime == null) return 0.0;

        LocalTime standardStart = LocalTime.of(8, 0);
        LocalTime standardEnd   = LocalTime.of(17, 0);

        LocalTime effectiveLogout = logoutTime.isAfter(standardEnd) ? standardEnd : logoutTime;

        LocalTime gracePeriodLimit = LocalTime.of(8, 10);
        LocalTime effectiveLogin = loginTime.isBefore(standardStart) ? standardStart : loginTime;
        
        if (!loginTime.isAfter(gracePeriodLimit)) {
            effectiveLogin = standardStart;
        }

        if (!effectiveLogout.isAfter(effectiveLogin)) return 0.0;

        double totalMinutes = Duration.between(effectiveLogin, effectiveLogout).toMinutes();

        LocalTime lunchStart = LocalTime.of(12, 0);
        LocalTime lunchEnd   = LocalTime.of(13, 0);
        double lunchMinutes = calculateOverlap(effectiveLogin, effectiveLogout, lunchStart, lunchEnd);

        double netWorkedMinutes = totalMinutes - lunchMinutes;
        return Math.max(0.0, netWorkedMinutes / 60.0);
    }

    /* ----------- METHOD 2: WORKED HOURS AND LUNCH OVERLAP CALCULATION --------
     * This method determines the number of minutes where two time intervals overlap.
     * It is primarily used to identify how much of a work shift occurred during 
     * their 1-hour lunchtime, instead of just subtracting 1-hour for each of their shift.
    */
    static double calculateOverlap(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        LocalTime maxStart = startA.isAfter(startB) ? startA : startB;
        LocalTime minEnd   = endA.isBefore(endB) ? endA : endB;

        if (minEnd.isAfter(maxStart)) {
            return Duration.between(maxStart, minEnd).toMinutes();
        }
        return 0.0;
    }

    /* ---------------------- METHOD 3: DATA PARSING ---------------------------
     * This method converts a String from the CSV file into a double-precision number.
     * It sanitizes the input by removing quotes and whitespace to ensure correct parsing.
    */
    static double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("\"", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /* -------------------- METHOD 4: SSS CALCULATION --------------------------
     * This method computes the monthly SSS employee contribution based on gross salary.
     * It utilizes the bracketed contribution table and enforces a maximum cap.
    */
    static double computeSSS(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        if (monthlyGross < 3250) return 135.00;

        double baseLowerBound = 3250.0;
        double baseContribution = 157.50;
        double bracketStep = 500.0;
        double incrementPerStep = 22.50;
        double maximumContribution = 1125.00;

        double difference = monthlyGross - baseLowerBound;
        int bracketsEarned = (int) Math.floor(difference / bracketStep);
        double totalContribution = baseContribution + (bracketsEarned * incrementPerStep);

        return Math.min(totalContribution, maximumContribution);
    }

    /* ---------------- METHOD 5: PHILHEALTH CALCULATION -----------------------
     * This method calculates the PhilHealth premium share for the employee.
     * It applies a 3% premium rate with a minimum cap of 300 and a maximum of 1800.
    */
    static double computePhilHealth(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        double premium = monthlyGross * 0.03;
        if (premium < 300.0) premium = 300.0;
        if (premium > 1800.0) premium = 1800.0;
        return premium / 2.0;
    }

    /* ---------------- METHOD 6: PAG-IBIG CALCULATION ------------------------
     * This method calculates the Pag-IBIG contribution for the employee.
     * It applies a rate of 1% or 2% based on income level and caps at 100 pesos.
    */
    static double computePagIbig(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        double contributionRate = (monthlyGross <= 1500) ? 0.01 : 0.02;
        double calculatedShare = monthlyGross * contributionRate;
        return Math.min(calculatedShare, 100.0);
    }

    /* ---------------- METHOD 7: WITHHOLDING TAX CALCULATION ------------------
     * This method determines the monthly withholding tax using the BIR graduated table.
     * Tax is computed on the taxable income remaining after all statutory deductions.
    */
    static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0.0;
        else if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        else return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

/* ---------------- METHOD 8: EMPLOYEE SESSION HANDLER ------------------------
     * This method manages the interactive menu for users with the "employee" role.
     * It allows for profile retrieval and ensures the user can only exit after viewing.
    */
    static void handleEmployeeSession(Scanner scanner, String employeeFilePath) {
        boolean sessionActive = true;
        boolean hasViewedInformation = false;

        while (sessionActive) {
            System.out.println("\nPlease enter the number of your desired action.");
            if (!hasViewedInformation) {
                System.out.println("[1] View your employee information");
                System.out.println("[2] Exit the program");
                System.out.print("Enter option: ");
                String menuSelection = scanner.nextLine().trim();

                if (menuSelection.equals("1")) {
                    System.out.print("Enter Employee #: ");
                    String targetId = scanner.nextLine().trim();
                    boolean employeeFound = false;
                    try (BufferedReader reader = new BufferedReader(new FileReader(employeeFilePath))) {
                        reader.readLine();
                        String row;
                        while ((row = reader.readLine()) != null) {
                            String[] dataFields = row.split(",");
                            if (dataFields[0].equals(targetId)) {
                                displayProfileHeader(dataFields);
                                System.out.println("===================================");
                                System.out.println("Request to view employee information is successful.");
                                employeeFound = true;
                                hasViewedInformation = true;
                                break;
                            }
                        }
                    } catch (Exception e) { System.out.println("Error accessing employee records."); }
                    
                    if (!employeeFound) {
                        System.out.println("Employee number does not exist.");
                    }
                    
                } else if (menuSelection.equals("2")) {
                    terminateSession();
                    sessionActive = false;
                }
            } else {
                System.out.println("[1] Exit the program");
                System.out.print("Enter option: ");
                if (scanner.nextLine().trim().equals("1")) {
                    terminateSession();
                    sessionActive = false;
                }
            }
        }
    }

    /* ---------------- METHOD 9: STAFF SESSION HANDLER ------------------------
     * This method manages the interactive menu for users with the "payroll_staff" role.
     * It provides the sub-menus for calculating payroll for specific or all employees.
    */
    static void handleStaffSession(Scanner scanner, String empFile, String attFile, DateTimeFormatter timeFormat) {
        boolean sessionActive = true;
        while (sessionActive) {
            System.out.println("\nPlease enter the number of your desired action.");
            System.out.println("[1] Process Payroll");
            System.out.println("[2] Exit the program");
            System.out.print("Enter option: ");
            String staffChoice = scanner.nextLine().trim();

            if (staffChoice.equals("1")) {
                boolean payrollModeActive = true;
                while (payrollModeActive) {
                    System.out.println("\nPlease enter the number of your desired action.");
                    System.out.println("[1] One employee");
                    System.out.println("[2] All employees");
                    System.out.println("[3] Exit the program");
                    System.out.print("Enter option: ");
                    String subChoice = scanner.nextLine().trim();

                    if (subChoice.equals("1")) {
                        System.out.print("Enter Employee #: ");
                        String id = scanner.nextLine().trim();
                        boolean found = false;
                        try (BufferedReader reader = new BufferedReader(new FileReader(empFile))) {
                            reader.readLine(); String row;
                            while ((row = reader.readLine()) != null) {
                                String[] data = row.split(",");
                                if (data[0].equals(id)) {
                                    executePayrollLogic(data, attFile, timeFormat);
                                    found = true; break;
                                }
                            }
                        } catch (Exception e) { System.out.println("Record access error."); }
                        if (!found) System.out.println("Employee number does not exist.");
                        else { finalizePayrollProcess(); }
                    } else if (subChoice.equals("2")) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(empFile))) {
                            reader.readLine(); String row;
                            while ((row = reader.readLine()) != null) {
                                executePayrollLogic(row.split(","), attFile, timeFormat);
                            }
                            finalizePayrollProcess();
                        } catch (Exception e) { System.out.println("Record access error."); }
                    } else if (subChoice.equals("3")) {
                        terminateSession();
                        payrollModeActive = false; sessionActive = false;
                    }
                }
            } else if (staffChoice.equals("2")) {
                terminateSession();
                sessionActive = false;
            }
        }
    }

    /* ------------------ METHOD 10: PAYROLL CALCULATOR ------------------------
     * This internal method executes the mathematical processing of employee earnings.
     * It iterates through the months of 2024 to generate a complete salary report.
    */
    static void executePayrollLogic(String[] employeeInfo, String attendanceFile, DateTimeFormatter timeFormat) {
        String empId = employeeInfo[0];
        double hourlyRate = tryParseDouble(employeeInfo[employeeInfo.length - 1]);
        displayProfileHeader(employeeInfo);
        System.out.println("---------------------------------");
        System.out.println("EMPLOYEE SALARY");

        for (int month = 6; month <= 12; month++) {
            double cutoffOneHours = 0.0, cutoffTwoHours = 0.0;
            try (BufferedReader attReader = new BufferedReader(new FileReader(attendanceFile))) {
                attReader.readLine(); String row;
                while ((row = attReader.readLine()) != null) {
                    String[] attData = row.split(",");
                    if (!attData[0].equals(empId)) continue;
                    String[] dateParts = attData[3].split("/");
                    if (Integer.parseInt(dateParts[2]) == 2024 && Integer.parseInt(dateParts[0]) == month) {
                        double dailyHours = computeHours(LocalTime.parse(attData[4], timeFormat), LocalTime.parse(attData[5], timeFormat));
                        if (Integer.parseInt(dateParts[1]) <= 15) cutoffOneHours += dailyHours; 
                        else cutoffTwoHours += dailyHours;
                    }
                }
            } catch (Exception e) { continue; }

            String monthName = switch (month) { case 6->"June"; case 7->"July"; case 8->"August"; case 9->"September"; case 10->"October"; case 11->"November"; case 12->"December"; default->""; };
            double grossOne = cutoffOneHours * hourlyRate, grossTwo = cutoffTwoHours * hourlyRate, monthlyGross = grossOne + grossTwo;
            double sss = computeSSS(monthlyGross), philhealth = computePhilHealth(monthlyGross), pagibig = computePagIbig(monthlyGross);
            double tax = computeWithholdingTax(Math.max(0, monthlyGross - (sss + philhealth + pagibig)));
            double totalDeductions = sss + philhealth + pagibig + tax;

            System.out.println("\nCutoff Date: " + monthName + " 1 to 15\nTotal Hours: " + cutoffOneHours + " hours" + "\nGross Salary: PHP " + grossOne + "\nNet Salary: PHP " + grossOne);
            System.out.println("\nCutoff Date: " + monthName + " 16 to " + YearMonth.of(2024, month).lengthOfMonth() + "\nTotal Hours: " + cutoffTwoHours + "hours" + "\nGross Salary: PHP " + grossTwo + "\nEach Deduction:\n    SSS: PHP " + sss + "\n    PhilHealth: PHP " + philhealth + "\n    Pag-IBIG: PHP "  + pagibig + "\n    Tax: PHP " + tax + "\nTotal Deductions: PHP " + totalDeductions + "\nNet Salary: PHP " + (grossTwo - totalDeductions));
        }
        System.out.println("===================================");
    }

    /* ---------------- METHOD 11: DISPLAY REQUEST OUTPUTS ---------------------
     * These helper methods standardize console outputs to reduce code repetition.
    */
    static void displayProfileHeader(String[] data) {
        System.out.println("\n===================================");
        System.out.println("EMPLOYEE INFORMATION");
        System.out.println("Employee #: " + data[0]);
        System.out.println("Employee Name: " + data[1] + ", " + data[2]);
        System.out.println("Birthday: " + data[3]);
    }

    static void terminateSession() {
        System.out.println("\nClosing the program . . .");
    }

    static void finalizePayrollProcess() {
        System.out.println("Payroll processing is successful!");
        System.out.print("\nProcess another payroll. ");
    }
    
    public static void main(String[] args) {
        // Define internal resource paths for CSV files
        String employeeDetailsPath = "resources/MotorPH - Employee Details.csv";
        String attendanceRecordsPath = "resources/MotorPH - Attendance Record.csv";
        
        // Initialize input scanner and time format for processing
        Scanner inputScanner = new Scanner(System.in);
        DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("H:mm");

        // PERSISTENT HEADER: This displays once at the very start of program execution.
        System.out.println("===================================");
        System.out.println("      MOTORPH PAYROLL SYSTEM       ");
        System.out.println("===================================");

        
        System.out.print("Enter username: ");
        String userEntry = inputScanner.nextLine().trim();
        System.out.print("Enter password: ");
        String passEntry = inputScanner.nextLine().trim();

        // Authentication Logic: Terminates the program if credentials do not match
        if (!((userEntry.equals("employee") || userEntry.equals("payroll_staff")) && passEntry.equals("12345"))) {
            System.out.println("Incorrect username and/or password.");
            return; // Ends the main method, thus closing the application
        }

        // Redirection logic based on user role
        System.out.println("\nLog in successful!");
        if (userEntry.equals("employee")) {
            handleEmployeeSession(inputScanner, employeeDetailsPath);
        } else {
            handleStaffSession(inputScanner, employeeDetailsPath, attendanceRecordsPath, hourFormat);
        }
    }
}
