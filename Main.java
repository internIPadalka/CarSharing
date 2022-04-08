package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        int mainMenuChoice;
        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection("jdbc:h2:~/Car Sharing/Car Sharing/task/src/carsharing/db/carsharing");
            try {
                statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS COMPANY " +
                        "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        " name VARCHAR(255) UNIQUE NOT NULL " + ")");
                statement.executeUpdate("ALTER TABLE COMPANY " + "ALTER COLUMN id RESTART WITH 1");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                while (true) {
                    System.out.println("\n1. Log in as a manager \n0. Exit");
                    mainMenuChoice = Integer.parseInt(scanner.nextLine());
                    if (mainMenuChoice == 0) {
                        break;
                    } else if (mainMenuChoice == 1) {
                        logInAsManager(connection);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logInAsManager(Connection connection) throws SQLException {
        int choice;
        while (true) {
            System.out.println("\n1. Company list \n2. Create a company \n0. Back");
            choice = Integer.parseInt(scanner.nextLine());

            if (choice == 0) {
                break;
            } else if (choice == 1) {
                listAllCompanies(connection);
            } else if (choice == 2) {
                createACompany(connection);
            }
        }

    }

    private void createACompany(Connection connection) {
        String companyName;
        PreparedStatement preparedStatement;

        System.out.println("Enter the company name:");
        companyName = scanner.nextLine();

        try {
            preparedStatement = connection.prepareStatement("INSERT INTO COMPANY (name) " + "VALUES (?)");
            preparedStatement.setString(1, companyName);
            preparedStatement.executeUpdate();
            System.out.println("The company was created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void listAllCompanies(Connection connection) {
        Statement statement;
        ResultSet companies;
        List<String> companyList = new ArrayList<>();


        try {
            statement = connection.createStatement();
            try {
                companies = statement.executeQuery("SELECT * FROM COMPANY");
                if (!companies.next()) {
                    System.out.println("The company list is empty");
                } else {
                    while (true) {
                        System.out.println(companies.getInt("id") + ". " + companies.getString("name"));
                        companyList.add(companies.getString("name"));
                        if (!companies.next()) {
                            break;
                        }
                    }
                    System.out.println("0. Back");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
