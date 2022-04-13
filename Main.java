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
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS CAR " +
                        "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        " name VARCHAR(255) UNIQUE NOT NULL, " +
                        " company_id INT NOT NULL," +
                        " CONSTRAINT fk_company FOREIGN KEY (company_id)" +
                        " REFERENCES COMPANY(id)" +
                        ")");

                statement.executeUpdate("ALTER TABLE CAR " +
                        "ALTER COLUMN id RESTART WITH 1");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS CUSTOMER " +
                        "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        " name VARCHAR(255) UNIQUE NOT NULL, " +
                        " rented_car_id INT," +
                        " CONSTRAINT fk_car FOREIGN KEY (rented_car_id)" +
                        " REFERENCES CAR(id)" +
                        ")");

                statement.executeUpdate("ALTER TABLE CUSTOMER " +
                        "ALTER COLUMN id RESTART WITH 1");

                statement.executeUpdate("ALTER TABLE CUSTOMER " +
                        "ALTER rented_car_id SET DEFAULT NULL");
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
                listAllCompanies(connection, "manager");
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

    private int listAllCompanies(Connection connection, String mOrC) {
        Statement statement;
        ResultSet companies;
        List<String> companyList = new ArrayList<>();
        int chosenCompany;
        try {
            statement = connection.createStatement();
            companies = statement.executeQuery("SELECT * FROM COMPANY");
            if (!companies.next()) {
                System.out.println("The company list is empty");
                return -1;
            } else {
                System.out.println("\nChoose the company:");
                while (true) {
                    System.out.println(companies.getInt("id") + ". " + companies.getString("name"));
                    companyList.add(companies.getString("name"));
                    if (!companies.next()) {
                        break;
                    }
                }
                System.out.println("0. Back");
                chosenCompany = Integer.parseInt(scanner.nextLine());
                if (chosenCompany != 0) {
                    String compName = companyList.get(chosenCompany - 1);

                    if (mOrC.equals("manager")) {
                        carMenu(connection, chosenCompany, compName);
                    } else {
                        return listAllCar(connection, chosenCompany, compName, "customer");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void carMenu(Connection connection, int chosenCompany, String compName) throws SQLException {
        int choiceCar;
        System.out.println("'" + compName + "' company");
        while (true) {
            System.out.println("\n1. Car list \n2. Create a car \n0. Back");
            choiceCar = Integer.parseInt(scanner.nextLine());
            if (choiceCar == 0) {
                break;
            } else if (choiceCar == 1) {
                listAllCar(connection, chosenCompany, compName, "manager");
            } else if (choiceCar == 2) {
                createACar(connection, chosenCompany);
            }
        }
    }

    private void createACar(Connection connection, int chosenCompany) {
        String carName;
        System.out.println("\nEnter the car name:");
        carName = scanner.nextLine();
        String insertCompany = "INSERT INTO CAR (name, company_id) " +
                "VALUES (?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCompany)) {
            preparedStatement.setString(1, carName);
            preparedStatement.setInt(2, chosenCompany);
            preparedStatement.executeUpdate();
            System.out.println("The car was added!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int listAllCar(Connection connection, int chosenCompany, String compName, String mOrC){
        Statement statement;
        ResultSet cars;
        int count = 1;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            cars = statement.executeQuery("SELECT * FROM CAR WHERE company_id = '" + chosenCompany + "'");
                if (!cars.next()) {
                    if (mOrC.equals("manager")) {
                        System.out.println("The car list is empty!");
                        return -1;
                    } else {
                        System.out.println("No available cars in the '" + compName + "' company.");
                        return -1;
                    }

                } else {
                    if (mOrC.equals("customer")) {
                        System.out.println("\nChoose a car:");
                    }
                    while (true) {
                        System.out.println(count + ". " + cars.getString("name") + ", car id: " + cars.getInt("id"));
                        count++;

                        if (!cars.next()) {
                            break;
                        }
                    }
                    if (mOrC.equals("customer")) {
                        cars.absolute(Integer.parseInt(scanner.nextLine()));
                        return cars.getInt("id");
                    }
                }
            } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }
}
