import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Bonus {
    public static void main(String[] args) throws FileNotFoundException {
        //Set up connection to database
        String jdbcUrl = "jdbc:oracle:thin:@oracle.cs.ua.edu:1521:xe";
        String username = "npmckivergan";
        String password = "12285017";

        Connection connection;
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);

            Scanner scanner = new Scanner(System.in);
		int ssn = -1;

        PreparedStatement statement;
        String query;
        ResultSet resultSet;

        String name = "";

        while (true) {
			System.out.print("Enter an SSN: ");
			try {
				ssn = scanner.nextInt();
                System.out.println();
			}
			catch (Exception e) {
				System.out.println("Invalid input");
                System.out.println();
				scanner.next(); // Consume invalid input
		        continue;
			}
            if (ssn == 0) {
                System.out.println("Good Bye!");
			    break;
			}
            //Check if the SSN belongs to an employee
            query = "SELECT * FROM employee WHERE ssn = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, ssn);
            resultSet = statement.executeQuery();

            //Check if the SSN corresponds to an employee
            if (resultSet.next()) {

                //Get the employees name
                query = "SELECT fname, lname FROM employee WHERE ssn = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, ssn);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    name = resultSet.getString("fname") + " " + resultSet.getString("lname");
                }

                //Check if the employee is a manager
                query = "SELECT e.fname, e.lname, d.dname " +
                       "FROM employee e " +
                       "JOIN department d ON e.ssn = d.mgrssn " +
                       "WHERE e.ssn = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, ssn);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    //SSN belongs to a manager
                    System.out.println(name + " is the manager of the " + resultSet.getString("dname") + " department.");
                }
                else {
                    //SSN does not belong to a manager
                    System.out.println(name + " is not a manager.");
                }
                System.out.println();

                //Check if the employee has dependents
                query = "SELECT dependent_name FROM dependent WHERE essn = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, ssn);
                resultSet = statement.executeQuery();
                int count = 0;
                while (resultSet.next()) {
                    count++;
                }      
                if (count == 0) {
                    // Employee has no dependents
                    System.out.println(name + " has no dependents.");
                } else {
                    // Employee has dependents
                    System.out.println(name + " has the following " + count + " dependents.");
                }
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    System.out.println("\t" + resultSet.getString("dependent_name"));
                }
                System.out.println();    

                //Check if the employee has worked on any projects  
                query = "SELECT DISTINCT project.pname " +
                         "FROM project " +
                         "JOIN works_on ON project.pnumber = works_on.pno " +
                         "WHERE works_on.essn = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, ssn);
                resultSet = statement.executeQuery();
                count = 0;
                while (resultSet.next()) {
                        count++;
                    }

                    if (count == 0) {
                        // Employee has not worked on any projects
                        System.out.println(name + " has not worked on any projects.");
                    } else {
                        // Employee has worked on projects
                        System.out.println(name + " has worked on the following " + count + " projects.");
                    }
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    System.out.println("\t" + resultSet.getString("pname"));
                }
                System.out.println();

                //Check if any other employees have worked on all the same projects as the current employee SSN
                query = "SELECT e.fname, e.lname " +
                         "FROM employee e " +
                         "JOIN works_on w ON e.ssn = w.essn " +
                         "WHERE w.pno IN ( " +
                         "    SELECT DISTINCT pno " +
                         "    FROM works_on " +
                         "    WHERE essn = ? " +
                         ") " +
                         "GROUP BY e.ssn, e.fname, e.lname " +
                         "HAVING COUNT(DISTINCT w.pno) = ( " +
                         "    SELECT COUNT(DISTINCT pno) " +
                         "    FROM works_on " +
                         "    WHERE essn = ? " +
                         ") " +
                         "AND e.ssn <> ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, ssn);
                statement.setInt(2, ssn);
                statement.setInt(3, ssn);
                resultSet = statement.executeQuery();
                count = 0;
                while (resultSet.next()) {
                    count++;
                }
                if (count == 0) {
                    System.out.println("No employee has worked on all the projects " + name + " has worked on.");
                }
                else {
                    System.out.println("The following " + count + " employees have worked on all the projects " + name + " has worked on.");
                }
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    System.out.println("\t" + resultSet.getString("fname") + " " + resultSet.getString("lname"));
                }
                System.out.println();
            }
            else {
                System.out.println("No Employee with SSN=" + ssn + ".");
                System.out.println();
            }
		}
        }
        catch (Exception e) {
            System.out.println("Connection failed");
            System.exit(1);
        }
    }
}
