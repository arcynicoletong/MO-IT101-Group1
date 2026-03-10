package com.motorphpayroll.motorphpayroll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * MOTORPH PAYROLL SYSTEM
 * 
 * @author H1101_Group 1
 * 
 * This program handles profile viewing and payroll processing for MotorPH
 * payroll staff and employees. For the detailed block-by-block explanation,
 * please refer to our README file.
 */
public class MotorPHPayrollSystem {
    
    /* ----------------------- METHOD 1: FILE READER -------------------------
     * This method reads a file and stores the information in an
     * ArrayList of String arrays.
    */
    static void readFile(String filename, ArrayList<String[]> data) {
        String row;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            reader.readLine();
            while ((row = reader.readLine()) != null) {
                String[] dataFields = row.split(",");
                data.add(dataFields);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("An error occurred while reading the file (" + filename + "): " + e);
        }
    }
    
    /* --------------- METHOD 2: DISPLAY EMPLOYEE INFORMATION -----------------
     * This method displays employee information and is designed to be reused 
     * in payroll_staff sessions as employee salary information header.
    */
    static void displayProfileHeader(String[] data) {
        System.out.println("\n===================================");
        System.out.println("EMPLOYEE INFORMATION");
        System.out.println("Employee #: " + data[0]);
        System.out.println("Employee Name: " + data[1] + ", " + data[2]);
        System.out.println("Birthday: " + data[3]);
    }

    /* ---------------------- METHOD 3: DATA PARSING ---------------------------
     * This method converts a String from the CSV file into a double number.
     * It removes quotes and whitespace from the input to ensure correct parsing.
    */
    static double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("\"", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /* ---------------- METHOD 4: WORKED HOURS COMPUTATION ---------------------
     * This method calculates the total hours worked by an employee during a shift.
     * It strictly counts time between 8:00 AM and 5:00 PM and applies a 10-minute 
     * grace period. Logins before 8:11 AM are treated as 8:00 AM as instructed.
    */
    static double computeHours(LocalTime loginTime, LocalTime logoutTime) {
        if (loginTime == null || logoutTime == null) return 0.0;
	
        LocalTime standardStart = LocalTime.of(8, 0);
        LocalTime standardEnd   = LocalTime.of(17, 0);
        LocalTime effectiveLogout = logoutTime.isAfter(standardEnd) ? standardEnd : logoutTime;

        LocalTime gracePeriodLimit = LocalTime.of(8, 10);
        LocalTime effectiveLogin = loginTime.isBefore(standardStart) ? standardStart : loginTime;
        if (!loginTime.isAfter(gracePeriodLimit)) {effectiveLogin = standardStart;}
	
        double lunchMinutes = calculateOverlap(effectiveLogin, effectiveLogout, LocalTime.of(12, 0), LocalTime.of(13, 0));
	
        return (Duration.between(effectiveLogin, effectiveLogout).toMinutes() - lunchMinutes) / 60.0;
    }


    /* --------------- METHOD 5: UNPAID LUNCH OVERLAP CALCULATION --------------
     * This method determines the number of minutes where two (2) time intervals 
     * (i.e. the whole day worked hours and the 1-hour unpaid lunch)overlap. 
     * Our team used this method to avoid improper 1-hour time deduction.
    */
    static double calculateOverlap(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        LocalTime maxStart = startA.isAfter(startB) ? startA : startB;
        LocalTime minEnd   = endA.isBefore(endB) ? endA : endB;

        if (minEnd.isAfter(maxStart)) {
            return Duration.between(maxStart, minEnd).toMinutes();
        }
        return 0.0;
    }

    /* -------------------- METHOD 6: SSS CALCULATION --------------------------
     * This methdod computes the monthly SSS contribution based on gross salary.
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

    /* ---------------- METHOD 7: PHILHEALTH CALCULATION -----------------------
     * This method calculates the PhilHealth premium share for an employee.
     * Applies a 3% premium rate with a minimum cap of 300 and a maximum of 1800.
    */
    static double computePhilHealth(double monthlyGross) {
        if (monthlyGross <= 0) return 0.0;
        double premium = monthlyGross * 0.03;
        if (premium < 300.0) premium = 300.0;
        if (premium > 1800.0) premium = 1800.0;
        return premium / 2.0;
    }

    /* ---------------- METHOD 8: PAG-IBIG CALCULATION ------------------------
     * This method calculates the Pag-IBIG contribution for the employee.
     * Applies tiered rates based on income level and caps at 100 pesos.
    */
    static double computePagIbig(double monthlyGross) {

            if (monthlyGross < 1000) {
                return 0.0;
            }

            double contributionRate;

            if (monthlyGross <= 1500) {
                contributionRate = 0.01;
            } else {
                contributionRate = 0.02;
            }

            double calculatedShare = monthlyGross * contributionRate;

            if (calculatedShare > 100.0) {
                return 100.0;
            } else {
                return calculatedShare;
            }
    }

    /* ---------------- METHOD 9: WITHHOLDING TAX CALCULATION ------------------
     * Determines the monthly withholding tax using the BIR graduated table.
     * Tax is computed on income remaining after statutory deductions.
    */
    static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0.0;
        else if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        else return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    /* ---------------- METHOD 10: EMPLOYEE SESSION HANDLER ------------------------
     * This method manages the interactive menu for users with the "employee" role.
     * Handles profile retrieval and secure logout procedures.
    */
    static void handleEmployeeSession(Scanner scanner, String employeeFilePath) {
        ArrayList<String[]> employeeInformation = new ArrayList<>();
        readFile(employeeFilePath,employeeInformation);
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
                    for (String[] employee : employeeInformation) {
                        if (employee[0].equals(targetId)) {
                            displayProfileHeader(employee);
                            System.out.println("===================================");
                            System.out.println("Request to view employee information is successful.");
                            employeeFound = true;
                            hasViewedInformation = true;
                            break;
                        }
                    }
                    if (!employeeFound) {
                        System.out.println("Employee number does not exist.");
                    }     
                } else if (menuSelection.equals("2")) {
                    terminateSession();
                    sessionActive = false;
                } else {
                    invalidInput();
                }
            } else {
                System.out.println("[1] Exit the program");
                System.out.print("Enter option: ");
                if (scanner.nextLine().trim().equals("1")) {
                    terminateSession();
                    sessionActive = false;
                } else {
                    invalidInput();
                }
            }
        }
    }

    /* -------------- METHOD 11: PAYROLL STAFF SESSION HANDLER -----------------
     * This method manages the interactive menu for users with the "payroll_staff" role.
     * Provides processing options for single or bulk employee payroll.
    */
    static void handleStaffSession(Scanner scanner, String empFile, String attFile, DateTimeFormatter timeFormat) {
        ArrayList<String[]> employeeInformation = new ArrayList<>();
        readFile(empFile,employeeInformation);
        
        ArrayList<String[]> attendanceInformation = new ArrayList<>();
        readFile(attFile,attendanceInformation);
                
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
                        for (String[] employee : employeeInformation) {
                            if (employee[0].equals(id)) {
                                executePayrollLogic(employee, attendanceInformation, timeFormat);
                                found = true; break;
                            }
                        }
                        if (!found) System.out.println("Employee number does not exist.");
                        else { finalizePayrollProcess(); }
                    } else if (subChoice.equals("2")) {
                        for (String[] employee : employeeInformation) {
                            executePayrollLogic(employee, attendanceInformation, timeFormat);
                        }
                        finalizePayrollProcess();
                    } else if (subChoice.equals("3")) {
                        terminateSession();
                        payrollModeActive = false; sessionActive = false;
                    } else {
                        invalidInput();
                    }
                }
            } else if (staffChoice.equals("2")) {
                terminateSession();
                sessionActive = false;
            } else {
                invalidInput();
            }
        }
    }

    /* ------------------ METHOD 12: PAYROLL CALCULATOR ------------------------
     * This method executes the mathematical processing of employee earnings.
     * It connects the attendance records with all the previous math methods 
     * (i.e. the calculators for SSS, PhilHealth, Pag-IBIG, and Tax)
    */
    static void executePayrollLogic(String[] employeeInfo, ArrayList<String[]> attendanceFile, DateTimeFormatter timeFormat) {
        String empId = employeeInfo[0];
        double hourlyRate = tryParseDouble(employeeInfo[employeeInfo.length - 1]);
        displayProfileHeader(employeeInfo);
        System.out.println("---------------------------------");
        System.out.println("EMPLOYEE SALARY");

        for (int month = 6; month <= 12; month++) {
            double cutoffOneHours = 0.0, cutoffTwoHours = 0.0;
            for (String[] attendanceRecord : attendanceFile) {
                if (!attendanceRecord[0].equals(empId)) continue;
                String[] dateParts = attendanceRecord[3].split("/");
                if (Integer.parseInt(dateParts[2]) == 2024 && Integer.parseInt(dateParts[0]) == month) {
                    double dailyHours = computeHours(LocalTime.parse(attendanceRecord[4], timeFormat), LocalTime.parse(attendanceRecord[5], timeFormat));
                    if (Integer.parseInt(dateParts[1]) <= 15) cutoffOneHours += dailyHours; 
                    else cutoffTwoHours += dailyHours;
                }
            }
            
            String monthName = switch (month) { case 6->"June"; case 7->"July"; case 8->"August"; case 9->"September"; case 10->"October"; case 11->"November"; case 12->"December"; default->""; };
            double grossOne = cutoffOneHours * hourlyRate, grossTwo = cutoffTwoHours * hourlyRate, monthlyGross = grossOne + grossTwo;
            double sss = computeSSS(monthlyGross), philhealth = computePhilHealth(monthlyGross), pagibig = computePagIbig(monthlyGross);
            double tax = computeWithholdingTax(Math.max(0, monthlyGross - (sss + philhealth + pagibig)));
            double totalDeductions = sss + philhealth + pagibig + tax;

            System.out.println("\nCutoff Date: " + monthName + " 1 to 15\nTotal Hours: " + cutoffOneHours + " hours" + "\nGross Salary: PHP " + grossOne + "\nNet Salary: PHP " + grossOne);
            System.out.println("\nCutoff Date: " + monthName + " 16 to " + YearMonth.of(2024, month).lengthOfMonth() + "\nTotal Hours: " + cutoffTwoHours + " hours" + "\nGross Salary: PHP " + grossTwo + "\nEach Deduction:\n    SSS: PHP " + sss + "\n    PhilHealth: PHP " + philhealth + "\n    Pag-IBIG: PHP "  + pagibig + "\n    Tax: PHP " + tax + "\nTotal Deductions: PHP " + totalDeductions + "\nNet Salary: PHP " + (grossTwo - totalDeductions));
        }
        System.out.println("===================================");
    }

    /* ------------------- METHOD 13: TERMINATE SESSION ------------------------
     * This method simply terminates the program but the team decided to turn it
     * into its own method to print a short message and make it reusable.
    */
    static void terminateSession() {
	System.out.println("\nClosing the program . . .");
    }

    /* -------------------- METHOD 14: FINALIZE PAYROLL ------------------------
     * This method prints a short message to inform the user that the payroll 
     * processing is successful. It is also designed to be a reusable method.
    */    
    static void finalizePayrollProcess() {
	System.out.println("Payroll processing is successful!");
	System.out.print("\nProcess another payroll. ");
    }
    
    /* -------------------- METHOD 15: INVALID INPUT ------------------------
     * This method prints a short message to inform the user that their input 
     * is invalid (i.e., not among the given options) to avoid repeated code.
     * It is also designed to be a reusable method.
    */   
    static void invalidInput() {
        System.out.println("Kindly choose among the options available.");
    }

    /* --------------------- METHOD 16: MAIN METHOD ---------------------------
     * The main method is the program's entry point which initializes file paths, 
     * handles the login authentication, and routes the user to the appropriate 
     * session handler based on their credentials.
    */
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

        // Lastly, this is logic will redirect user based on role (either employee of staff).
        System.out.println("\nLog in successful!");
        if (userEntry.equals("employee")) {
            handleEmployeeSession(inputScanner, employeeDetailsPath);
        } else {
            handleStaffSession(inputScanner, employeeDetailsPath, attendanceRecordsPath, hourFormat);
        }
    }
}
