package Expensetracker;

import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class ExpenseTracker {
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
//    	System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println("Welcome to Expense Tracker!");

        boolean keepRunning = true;

        while (keepRunning) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Enter transactions manually");
            System.out.println("2. Load transactions from a file");
            System.out.println("3. Show Monthly Summary");
            System.out.println("4. Export Monthly Summary to CSV");
            System.out.println("5. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    handleManualInput(scanner);
                    break;
                case 2:
                    System.out.print("Enter file path (CSV format): ");
                    String filePath = scanner.nextLine().trim();
                    loadFromFile(filePath);
                    break;
                case 3:
                    displayMonthlySummary();
                    break;
                case 4:
                    System.out.print("Enter output CSV filename (e.g. summary.csv): ");
                    String outputFile = scanner.nextLine().trim();
                    exportSummaryToCSV(outputFile);
                    break;
                case 5:
                    keepRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        displayMonthlySummary();
    }

    private static void handleManualInput(Scanner scanner) {
        boolean input = true;

        while (input) {
            System.out.print("\nEnter transaction type (income/expense): ");
            String type = scanner.nextLine().trim().toLowerCase();

            if (!type.equals("income") && !type.equals("expense")) {
                System.out.println("Invalid type. Please enter 'income' or 'expense'.");
                continue;
            }

            String category = "";
            if (type.equals("income")) {
                System.out.print("Enter sub-category (salary/business): ");
                category = scanner.nextLine().trim().toLowerCase();
                if (!category.equals("salary") && !category.equals("business")) {
                    System.out.println("Invalid category for income.");
                    continue;
                }
            } else {
                System.out.print("Enter sub-category (food/rent/travel): ");
                category = scanner.nextLine().trim().toLowerCase();
                if (!category.equals("food") && !category.equals("rent") && !category.equals("travel")) {
                    System.out.println("Invalid category for expense.");
                    continue;
                }
            }

            System.out.print("Enter amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // clear buffer

            System.out.print("Enter date (YYYY-MM-DD): ");
            String dateInput = scanner.nextLine().trim();
            LocalDate date;
            try {
                date = LocalDate.parse(dateInput);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
                continue;
            }

            transactions.add(new Transaction(type, category, amount, date));

            System.out.print("Add another entry? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                input = false;
            }
        }
    }

    private static void loadFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }

                String type = parts[0].trim().toLowerCase();
                String category = parts[1].trim().toLowerCase();
                double amount;
                LocalDate date;

                if (!type.equals("income") && !type.equals("expense")) {
                    System.out.println("Invalid type in line: " + line);
                    continue;
                }

                if ((type.equals("income") && !(category.equals("salary") || category.equals("business"))) ||
                    (type.equals("expense") && !(category.equals("food") || category.equals("rent") || category.equals("travel")))) {
                    System.out.println("Invalid category in line: " + line);
                    continue;
                }

                try {
                    amount = Double.parseDouble(parts[2].trim());
                    date = LocalDate.parse(parts[3].trim());
                } catch (Exception e) {
                    System.out.println("Invalid amount or date in line: " + line);
                    continue;
                }

                transactions.add(new Transaction(type, category, amount, date));
            }

            System.out.println("File loaded successfully.");
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    private static void displayMonthlySummary() {
        Map<Month, Double> incomeMap = new TreeMap<>();
        Map<Month, Double> expenseMap = new TreeMap<>();

        for (Transaction t : transactions) {
            Month month = t.getDate().getMonth();

            if (t.getType().equals("income")) {
                incomeMap.put(month, incomeMap.getOrDefault(month, 0.0) + t.getAmount());
            } else {
                expenseMap.put(month, expenseMap.getOrDefault(month, 0.0) + t.getAmount());
            }
        }

        System.out.println("\n*** Monthly Summary ***");
        Set<Month> allMonths = new TreeSet<>();
        allMonths.addAll(incomeMap.keySet());
        allMonths.addAll(expenseMap.keySet());

        for (Month month : allMonths) {
            double income = incomeMap.getOrDefault(month, 0.0);
            double expense = expenseMap.getOrDefault(month, 0.0);
            double balance = income - expense;

            System.out.printf("%-10s => Income: ₹%.2f | Expense: ₹%.2f | Balance: ₹%.2f\n",
                    month, income, expense, balance);
        }
    }
    private static void exportSummaryToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Month,Income,Expense,Balance");

            Map<Month, Double> incomeMap = new TreeMap<>();
            Map<Month, Double> expenseMap = new TreeMap<>();

            for (Transaction t : transactions) {
                Month month = t.getDate().getMonth();

                if (t.getType().equals("income")) {
                    incomeMap.put(month, incomeMap.getOrDefault(month, 0.0) + t.getAmount());
                } else {
                    expenseMap.put(month, expenseMap.getOrDefault(month, 0.0) + t.getAmount());
                }
            }

            Set<Month> allMonths = new TreeSet<>();
            allMonths.addAll(incomeMap.keySet());
            allMonths.addAll(expenseMap.keySet());

            for (Month month : allMonths) {
                double income = incomeMap.getOrDefault(month, 0.0);
                double expense = expenseMap.getOrDefault(month, 0.0);
                double balance = income - expense;

                writer.printf("%s,%.2f,%.2f,%.2f\n", month, income, expense, balance);
            }

            System.out.println("Summary exported successfully to: " + filename);
        } catch (IOException e) {
            System.out.println("Failed to write file: " + e.getMessage());
        }
    }

}
