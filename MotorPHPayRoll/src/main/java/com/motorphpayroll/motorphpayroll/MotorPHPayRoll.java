/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.motorphpayroll.motorphpayroll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 *
 * @author ArcyNicoleTong
 */
public class MotorPHPayRoll {
    public static void main(String[] args) {

        // CSV file paths
        String empFile  = "resources/MotorPH - Employee Details.csv";
        String attFile  = "resources/MotorPH - Attendance Record.csv";

        Scanner sc = new Scanner(System.in);

        // LOGIN
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        if (!((username.equals("employee") || username.equals("payroll_staff")) && password.equals("12345"))) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        // EMPLOYEE MENU
        if (username.equals("employee")) {
            System.out.println("\nOptions:");
            System.out.println("1. Enter your employee number");
            System.out.println("2. Exit the program");
            System.out.print("Enter option: ");
            String opt = sc.nextLine().trim();

            if (opt.equals("1")) {
                System.out.print("Enter Employee #: ");
                String givenEmp = sc.nextLine().trim();

                // Find employee in Employee Details.csv file
                String empNo = "";
                String fName = "";
                String lName = "";
                String bday  = "";
                boolean found = false;

                try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {
                    br.readLine(); // skip header
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] data = line.split(",");
                        if (data[0].equals(givenEmp)) {
                            empNo = data[0];
                            lName = data[1];
                            fName = data[2];
                            bday  = data[3];
                            found = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading employee file: " + e.getMessage());
                    return;
                }

                if (!found) {
                    System.out.println("Employee number does not exist.");
                    return;
                }

                // Show only required profile fields (Employee Details Presentation)
                System.out.println("\n===================================");
                System.out.println("Employee Number: " + empNo);
                System.out.println("Employee Name: " + lName + ", " + fName);
                System.out.println("Birthday: " + bday);
                System.out.println("===================================");
                return; // terminate after showing
            } else {
                // Exit the program
                return;
            }
        }

        // PAYROLL STAFF MENUS
        System.out.println("\nOptions:");
        System.out.println("1. Process Payroll");
        System.out.println("2. Exit the program");
        System.out.print("Enter option: ");
        String mainChoice = sc.nextLine().trim();

        if (!mainChoice.equals("1")) {
            return; // Exit program
        }

        // Process Payroll sub-options
        System.out.println("\nProcess Payroll:");
        System.out.println("1. One employee");
        System.out.println("2. All employees");
        System.out.println("3. Exit the program");
        System.out.print("Enter option: ");
        String subChoice = sc.nextLine().trim();

        if (subChoice.equals("1")) {
            // ONE EMPLOYEE
            System.out.print("Enter Employee #: ");
            String givenEmp = sc.nextLine().trim();

            String empNo = "";
            String fName = "";
            String lName = "";
            String bday  = "";
            double hourlyRate = 0.0;
            boolean found = false;

            try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {
                br.readLine(); // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] data = line.split(",");
                    if (data[0].equals(givenEmp)) {
                        empNo = data[0];
                        lName = data[1];
                        fName = data[2];
                        bday  = data[3];
                        String last = data[data.length - 1]; // Hourly Rate is in last column of Employee Details .csv file
                        hourlyRate = tryParseDouble(last);
                        found = true;
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading employee file: " + e.getMessage());
                return;
            }

            if (!found) {
                System.out.println("Employee number does not exist.");
                return;
            }

            // This part shows the employee profile
            System.out.println("\n===================================");
            System.out.println("Employee #: " + empNo);
            System.out.println("Employee Name: " + lName + ", " + fName);
            System.out.println("Birthday: " + bday);

            // This part prepares the time format (example: 8:05, 17:00)
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("H:mm");

            // This will compute per month starting from June (6) to December (12) for year 2024
            for (int month = 6; month <= 12; month++) {
                int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();
                double cut1 = 0.0; // hours for 1-15
                double cut2 = 0.0; // hours for 16-end

                // Read attendance and sum hours for this employee and month
                try (BufferedReader br = new BufferedReader(new FileReader(attFile))) {
                    br.readLine(); // skip header
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] data = line.split(",");
                        if (!data[0].equals(empNo)) continue; // match employee

                        // Date format: MM/DD/YYYY | This part reads the date (*rec means recorded)
                        String[] dateParts = data[3].split("/");
                        int recMonth = Integer.parseInt(dateParts[0]);
                        int recDay   = Integer.parseInt(dateParts[1]);
                        int recYear  = Integer.parseInt(dateParts[2]);
                        if (recYear != 2024 || recMonth != month) continue;

                        // This part reads login and logout time
                        LocalTime in  = LocalTime.parse(data[4], timeFmt);
                        LocalTime out = LocalTime.parse(data[5], timeFmt);
                        
                         // This part computes hours worked
                        double hrs = computeHours(in, out);
                        
                         // This part assigns hours to the correct cutoff
                        if (recDay <= 15) cut1 += hrs;
                        else cut2 += hrs;
                    }
                } catch (Exception e) {
                    System.out.println("Error reading attendance file for month " + month);
                    continue;
                }

                // This part converts number month to name
                String mName = switch (month) {
                    case 6 -> "June";
                    case 7 -> "July";
                    case 8 -> "August";
                    case 9 -> "September";
                    case 10 -> "October";
                    case 11 -> "November";
                    case 12 -> "December";
                    default -> "Month";
                };
               
                // Payroll math (NO rounding)
                double gross1 = cut1 * hourlyRate;
                double gross2 = cut2 * hourlyRate;
                double monthlyGross = gross1 + gross2;

                // Monthly statutory deductions computed from monthly gross
                double sssMonthly  = computeSSS(monthlyGross);
                double philMonthly = computePhilHealthEmployeeShare(monthlyGross);
                double pagibigMon  = computePagIbig(monthlyGross);

                double taxableMonthly = monthlyGross - (sssMonthly + philMonthly + pagibigMon);
                if (taxableMonthly < 0) taxableMonthly = 0.0;
                double wtaxMonthly = computeWithholdingTax(taxableMonthly);

                double totalDeductions = sssMonthly + philMonthly + pagibigMon + wtaxMonthly;

                // Net per cutoff (apply deductions ONLY on 2nd cutoff)
                double net1 = gross1; // first payout has no deductions
                double net2 = gross2 - totalDeductions;

                // Output per cutoff
                System.out.println("\nCutoff Date: " + mName + " 1 to 15");
                System.out.println("Total Hours Worked: " + cut1);
                System.out.println("Gross Salary: " + gross1);
                System.out.println("Net Salary: " + net1);

                System.out.println("\nCutoff Date: " + mName + " 16 to " + daysInMonth);
                System.out.println("Total Hours Worked: " + cut2);
                System.out.println("Gross Salary: " + gross2);
                System.out.println("Each Deduction:");
                System.out.println("    SSS: " + sssMonthly);
                System.out.println("    PhilHealth: " + philMonthly);
                System.out.println("    Pag-IBIG: " + pagibigMon);
                System.out.println("    Tax: " + wtaxMonthly);
                System.out.println("Total Deductions: " + totalDeductions);
                System.out.println("Net Salary: " + net2);
            }

            System.out.println("===================================");
            return;
        }
        else if (subChoice.equals("2")) {
            // ALL EMPLOYEES
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("H:mm");

            try (BufferedReader brEmp = new BufferedReader(new FileReader(empFile))) {
                brEmp.readLine(); // skip header
                String lineEmp;
                while ((lineEmp = brEmp.readLine()) != null) {
                    if (lineEmp.trim().isEmpty()) continue;

                    String[] ed = lineEmp.split(",");

                    String empNo = ed[0];
                    String lName = ed[1];
                    String fName = ed[2];
                    String bday  = ed[3];
                    String last  = ed[ed.length - 1];
                    double hourlyRate = tryParseDouble(last);

                    // Header for this employee
                    System.out.println("\n===================================");
                    System.out.println("Employee #: " + empNo);
                    System.out.println("Employee Name: " + lName + ", " + fName);
                    System.out.println("Birthday: " + bday);

                    for (int month = 6; month <= 12; month++) {
                        int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();
                        double cut1 = 0.0;
                        double cut2 = 0.0;

                        // Scan attendance for this employee and month
                        try (BufferedReader brAtt = new BufferedReader(new FileReader(attFile))) {
                            brAtt.readLine(); // skip header
                            String line;
                            while ((line = brAtt.readLine()) != null) {
                                if (line.trim().isEmpty()) continue;
                                String[] data = line.split(",");
                                if (!data[0].equals(empNo)) continue;

                                String[] dateParts = data[3].split("/");
                                int recMonth = Integer.parseInt(dateParts[0]);
                                int recDay   = Integer.parseInt(dateParts[1]);
                                int recYear  = Integer.parseInt(dateParts[2]);
                                if (recYear != 2024 || recMonth != month) continue;

                                LocalTime in  = LocalTime.parse(data[4], timeFmt);
                                LocalTime out = LocalTime.parse(data[5], timeFmt);

                                double hrs = computeHours(in, out);

                                if (recDay <= 15) cut1 += hrs;
                                else cut2 += hrs;
                            }
                        } catch (Exception e) {
                            System.out.println("Error reading attendance file for month " + month);
                            continue;
                        }

                        String mName = switch (month) {
                            case 6 -> "June";
                            case 7 -> "July";
                            case 8 -> "August";
                            case 9 -> "September";
                            case 10 -> "October";
                            case 11 -> "November";
                            case 12 -> "December";
                            default -> "Month";
                        };

                        double gross1 = cut1 * hourlyRate;
                        double gross2 = cut2 * hourlyRate;
                        double monthlyGross = gross1 + gross2;

                        double sssMonthly  = computeSSS(monthlyGross);
                        double philMonthly = computePhilHealthEmployeeShare(monthlyGross);
                        double pagibigMon  = computePagIbig(monthlyGross);

                        double taxableMonthly = monthlyGross - (sssMonthly + philMonthly + pagibigMon);
                        if (taxableMonthly < 0) taxableMonthly = 0.0;
                        double wtaxMonthly = computeWithholdingTax(taxableMonthly);

                        double totalDeductions = sssMonthly + philMonthly + pagibigMon + wtaxMonthly;

                        double net1 = gross1; // no deductions on 1st cutoff
                        double net2 = gross2 - totalDeductions;

                        System.out.println("\nCutoff Date: " + mName + " 1 to 15");
                        System.out.println("Total Hours Worked: " + cut1);
                        System.out.println("Gross Salary: " + gross1);
                        System.out.println("Net Salary: " + net1);

                        System.out.println("\nCutoff Date: " + mName + " 16 to " + daysInMonth);
                        System.out.println("Total Hours Worked: " + cut2);
                        System.out.println("Gross Salary: " + gross2);
                        System.out.println("Each Deduction:");
                        System.out.println("    SSS: " + sssMonthly);
                        System.out.println("    PhilHealth: " + philMonthly);
                        System.out.println("    Pag-IBIG: " + pagibigMon);
                        System.out.println("    Tax: " + wtaxMonthly);
                        System.out.println("Total Deductions: " + totalDeductions);
                        System.out.println("Net Salary: " + net2);
                    }
                    System.out.println("===================================");
                }
            } catch (Exception e) {
                System.out.println("Error reading employee file: " + e.getMessage());
            }
            return;
        }
        else {
            // Exit program
            return;
        }
    }

    // HOURS COMPUTATION
    static double computeHours(LocalTime in, LocalTime out) {
        if (in == null || out == null) return 0.0;

        // Count only within 08:00–17:00
        LocalTime dayStart = LocalTime.of(8, 0);
        LocalTime dayEnd   = LocalTime.of(17, 0);

        if (out.isAfter(dayEnd)) out = dayEnd;

        LocalTime effectiveIn = in.isBefore(dayStart) ? dayStart : in;

        // 5-minute grace: 08:00–08:05 treated as 08:00
        LocalTime grace = LocalTime.of(8, 5);
        if (!in.isAfter(grace)) effectiveIn = dayStart;

        if (!out.isAfter(effectiveIn)) return 0.0;

        double totalMinutes = Duration.between(effectiveIn, out).toMinutes();

        // Deduct overlap with lunch 12:00–13:00 only
        LocalTime lunchStart = LocalTime.of(12, 0);
        LocalTime lunchEnd   = LocalTime.of(13, 0);
        double lunchOverlap = overlapMinutes(effectiveIn, out, lunchStart, lunchEnd);

        double workedMinutes = totalMinutes - lunchOverlap;
        if (workedMinutes < 0) workedMinutes = 0.0;

        return workedMinutes / 60.0; // no rounding
    }

    // Overlap minutes
    static double overlapMinutes(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2) {
        LocalTime s = a1.isAfter(b1) ? a1 : b1; // later start
        LocalTime e = a2.isBefore(b2) ? a2 : b2; // earlier end
        if (e.isAfter(s)) return Duration.between(s, e).toMinutes();
        return 0.0;
    }

    // DEDUCTIONS

    static double tryParseDouble(String s) {
        try {
            return Double.parseDouble(s.replace("\"","").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    // SSS (monthly employee share) using bracket math
    static double computeSSS(double monthlySalary) {
        if (monthlySalary <= 0) return 0.0;

        if (monthlySalary < 3250) {
            return 135.00;
        }

        double baseLower = 3250.0;
        double baseContrib = 157.50; // for 3,250–3,750
        double step = 500.0;
        double addPerStep = 22.50;
        double maxContrib = 1125.00; // Over 24,750

        double diff = monthlySalary - baseLower;
        int bands = (int)Math.floor(diff / step);
        double contrib = baseContrib + (bands * addPerStep);

        if (contrib > maxContrib) contrib = maxContrib;

        return contrib;
    }

    // PhilHealth employee share per month (3% premium, clamped 300–1800; employee pays 50%)
    static double computePhilHealthEmployeeShare(double monthlySalary) {
        if (monthlySalary <= 0) return 0.0;

        double rate = 0.03; // 3%
        double monthlyPremium = monthlySalary * rate;

        if (monthlyPremium < 300.0) monthlyPremium = 300.0;
        if (monthlyPremium > 1800.0) monthlyPremium = 1800.0;

        return monthlyPremium / 2.0;
    }

    // Pag-IBIG employee share (1% for 1,000–1,500; 2% for >1,500; capped at 100)
    static double computePagIbig(double monthlySalary) {
        if (monthlySalary <= 0) return 0.0;

        double rate;
        if (monthlySalary >= 1000 && monthlySalary <= 1500) {
            rate = 0.01;
        } else if (monthlySalary > 1500) {
            rate = 0.02;
        } else {
            rate = 0.0;
        }

        double employeeShare = monthlySalary * rate;
        if (employeeShare > 100.0) employeeShare = 100.0;
        return employeeShare;
    }

    // Monthly withholding tax (after SSS, PH, Pag-IBIG)
    static double computeWithholdingTax(double taxableMonthly) {
        double tax = 0.0;
        if (taxableMonthly <= 20833) {
            tax = 0.0;
        } else if (taxableMonthly < 33333) {
            tax = (taxableMonthly - 20833) * 0.20;
        } else if (taxableMonthly < 66667) {
            tax = 2500 + (taxableMonthly - 33333) * 0.25;
        } else if (taxableMonthly < 166667) {
            tax = 10833 + (taxableMonthly - 66667) * 0.30;
        } else if (taxableMonthly < 666667) {
            tax = 40833.33 + (taxableMonthly - 166667) * 0.32;
        } else {
            tax = 200833.33 + (taxableMonthly - 666667) * 0.35;
        }
        return tax;
    }
}
