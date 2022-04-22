package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final Scanner scanner = new Scanner(System.in);

    private final String createTableIfNotExistsCompany = "CREATE TABLE IF NOT EXISTS COMPANY " + "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " + " name VARCHAR(255) UNIQUE NOT NULL " + ")";
    private final String alterTableCompanyWithID1 = "ALTER TABLE COMPANY " + "ALTER COLUMN id RESTART WITH 1";
    private final String createTableIfNotExistsCar = "CREATE TABLE IF NOT EXISTS CAR " + "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " + " name VARCHAR(255) UNIQUE NOT NULL, " + " company_id INT NOT NULL," + " CONSTRAINT fk_company FOREIGN KEY (company_id)" + " REFERENCES COMPANY(id)" + ")";
    private final String alterTableCarWithID1 = "ALTER TABLE CAR " + "ALTER COLUMN id RESTART WITH 1";
    private final String createTableIfNotExistsCustomer = "CREATE TABLE IF NOT EXISTS CUSTOMER " + "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " + " name VARCHAR(255) UNIQUE NOT NULL, " + " rented_car_id INT," + " CONSTRAINT fk_car FOREIGN KEY (rented_car_id)" + " REFERENCES CAR(id)" + ")";
    private final String alterTableCustomerWithID1 = "ALTER TABLE CUSTOMER " + "ALTER COLUMN id RESTART WITH 1";
    private final String alterTableCustomerWithCarIDNULL = "ALTER TABLE CUSTOMER " + "ALTER rented_car_id SET DEFAULT NULL";

    private final String selectAllFromTableCompanies = "SELECT * FROM COMPANY";
    private final String selectAllFromTableCustomer = "SELECT * FROM CUSTOMER";

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {

        int mainMenuChoice;

        Statement statement;

        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:" + System.getProperty("user.dir") + "/Car Sharing/task/src/carsharing/db/carsharing");
            try {
                statement = connection.createStatement();
                statement.executeUpdate(createTableIfNotExistsCompany);
                statement.executeUpdate(alterTableCompanyWithID1);
                statement.executeUpdate(createTableIfNotExistsCar);

                statement.executeUpdate(alterTableCarWithID1);

                statement.executeUpdate(createTableIfNotExistsCustomer);

                statement.executeUpdate(alterTableCustomerWithID1);

                statement.executeUpdate(alterTableCustomerWithCarIDNULL);

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Exception after creation main table");
            }

            try {
                while (true) {
                    System.out.println("\n1. Log in as a manager \n2. Log in as a customer \n3. Create a customer \n0. Exit");
                    mainMenuChoice = Integer.parseInt(scanner.nextLine());
                    if (mainMenuChoice == 0) {
                        break;
                    } else if (mainMenuChoice == 1) {
                        logInAsManager(connection);
                    } else if (mainMenuChoice == 2) {
                        logInAsCustomer(connection);
                    } else if (mainMenuChoice == 3) {
                        createACustomer(connection);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Exception in choice menu");
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception after establishing connection");
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
            System.out.println("Exception after adding new company");
        }
    }

    private int listAllCompanies(Connection connection, String mOrC) {

        Statement statement;

        ResultSet companies;

        List<String> companyList = new ArrayList<>();

        int chosenCompany;
        int statusCodeCompanyListIsEmpty = -1;
        int statusCodeExceptionIsCached = 0;

        try {
            statement = connection.createStatement();
            companies = statement.executeQuery(selectAllFromTableCompanies);
            if (!companies.next()) {
                System.out.println("The company list is empty");
                return statusCodeCompanyListIsEmpty;
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
            System.out.println("Exception in listing companies");
        }
        return statusCodeExceptionIsCached;
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

        String insertCompany = "INSERT INTO CAR (name, company_id) " + "VALUES (?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCompany)) {
            preparedStatement.setString(1, carName);
            preparedStatement.setInt(2, chosenCompany);
            preparedStatement.executeUpdate();
            System.out.println("The car was added!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception in creating car");
        }
    }

    private int listAllCar(Connection connection, int chosenCompany, String compName, String mOrC) {

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
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception in listing cars");
        }
        return 0;
    }

    private void createACustomer(Connection connection) {

        PreparedStatement preparedStatement;

        String customerName;
        String insertCustomer;

        System.out.println("Enter the customer name:");
        customerName = scanner.nextLine();
        insertCustomer = "INSERT INTO CUSTOMER (name) " + "VALUES (?)";
        try {
            preparedStatement = connection.prepareStatement(insertCustomer);
            preparedStatement.setString(1, customerName);
            preparedStatement.executeUpdate();
            System.out.println("The customer was added!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception in adding new customer");
        }
    }

    private void logInAsCustomer(Connection connection) throws SQLException {

        Statement statement;

        ResultSet customers;

        int chosenCustomer = 0;

        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            customers = statement.executeQuery(selectAllFromTableCustomer);
            if (!customers.next()) {
                System.out.println("The customer list is empty!");
            } else {
                System.out.println("\nCustomer list:");
                while (true) {
                    System.out.println(customers.getInt("id") + ". " + customers.getString("name"));
                    if (!customers.next()) {
                        break;
                    }
                }
                System.out.println("0. Back");
                chosenCustomer = Integer.parseInt(scanner.nextLine());
                if (chosenCustomer != 0) {
                    customers.absolute(chosenCustomer);
                    customerMenu(connection, chosenCustomer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception in login");
        }
    }

    private void customerMenu(Connection connection, int chosenCustomer) {

        Statement statement;

        boolean hasRentedCar = false;

        int carId = -1;
        int choice = 0;
        int neededCarId;

        ResultSet cus;
        ResultSet car;
        ResultSet rentedCar;
        ResultSet belongToComp;

        try {
            statement = connection.createStatement();
            while (true) {
                cus = statement.executeQuery("SELECT * FROM CUSTOMER WHERE id = " + chosenCustomer);
                cus.next();
                carId = cus.getInt("rented_car_id");
                if (carId != 0) {
                    hasRentedCar = true;
                }
                System.out.println("\n1. Rent a car \n2. Return a rented car \n3. My rented car \n0. Back");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) {
                    break;
                } else if (choice == 3) {
                    if (!hasRentedCar) {
                        System.out.println("You didn't rent a car!");
                    } else {
                        try {
                            rentedCar = statement.executeQuery("SELECT * FROM CAR WHERE id = " + carId);
                            System.out.println("You rented a car:");
                            rentedCar.next();
                            System.out.println(rentedCar.getString("name"));
                            belongToComp = statement.executeQuery("SELECT * FROM COMPANY WHERE id = '" + rentedCar.getInt("company_id") + "'");
                            System.out.println("Company:");
                            belongToComp.next();
                            System.out.println(belongToComp.getString("name"));
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("Exception if you don't have car");
                        }
                    }

                } else if (choice == 1) {
                    if (hasRentedCar) {
                        System.out.println("You've already rented a car!");
                    } else {
                        neededCarId = listAllCompanies(connection, "customer");
                        if (neededCarId != 0) {
                            statement.executeUpdate("UPDATE CUSTOMER SET rented_car_id = " + neededCarId + " WHERE id = " + chosenCustomer);
                            try {
                                car = statement.executeQuery("SELECT * FROM CAR WHERE id = " + neededCarId);
                                car.next();
                                System.out.println("You rented '" + car.getString("name") + "'");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                System.out.println("Exception in choosing rented car");
                            }
                        }
                    }
                } else if (choice == 2) {
                    if (!hasRentedCar) {
                        System.out.println("You didn't rent a car!");
                    } else {
                        statement.executeUpdate("UPDATE CUSTOMER SET rented_car_id = NULL " + "WHERE id = " + chosenCustomer);
                        System.out.println("You've returned a rented car!");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception in choosing a car");
        }
    }
}
