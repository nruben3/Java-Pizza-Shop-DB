// Fig. 24.29: DisplayQueryResultsController.java
// Controller for the DisplayQueryResults app

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.PatternSyntaxException;

public class PizzaGUIController {
    // database URL, username and password
    private static final String DATABASE_URL = "jdbc:derby://localhost:1527/pizzadb";
    private static final String USERNAME = "app";
    private static final String PASSWORD = "app";
    // default query retrieves all data from Authors table
    private static final String DEFAULT_QUERY = "SELECT * FROM orders";
    private static final String NUM_ORDERS_PRICE_PER_DAY_QUERY = "select date(ordertime) as Order_Day, count(*) as Order_Number, sum(totalprice) as Order_Total \n" + "from orders \n" + "group by date(ordertime) \n" + "order by order_day desc";
    private static final String ORDER_SEARCH_BY = "select firstname, lastname, ordertime, pizza.pizzades, ordereditems.pizzaqn, sides.sidesdes, ordereditems.sidesqn, drinks.drinkdes, ordereditems.drinkqn \n"
            + "from customers \n"
            + "inner join orders on customers.phonenumber = orders.phonenumber \n"
            + "inner join ordereditems on ordereditems.orderid = orders.orderid \n"
            + "inner join pizza on pizza.pizzaid = ordereditems.pizzaid \n"
            + "inner join sides on sides.sidesid=ordereditems.sidesid \n"
            + "inner join drinks on drinks.drinkid=ordereditems.drinkid \n";
    @FXML
    private BorderPane borderPane;
    @FXML
    private TextArea queryTextArea;
    @FXML
    private TextField filterTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;
    private ResultSetTableModel tableModel;
    private TableRowSorter<TableModel> sorter;

    public void initialize () {
        queryTextArea.setText(DEFAULT_QUERY);

        // create ResultSetTableModel and display database table
        try {
            // create TableModel for results of DEFAULT_QUERY
            tableModel = new ResultSetTableModel(DATABASE_URL,
                    USERNAME, PASSWORD, DEFAULT_QUERY);

            // create JTable based on the tableModel
            JTable resultTable = new JTable(tableModel);

            // set up row sorting for JTable
            sorter = new TableRowSorter<>(tableModel);
            resultTable.setRowSorter(sorter);

            // configure SwingNode to display JTable, then add to borderPane
            SwingNode swingNode = new SwingNode();
            swingNode.setContent(new JScrollPane(resultTable));
            borderPane.setCenter(swingNode);
        } catch (SQLException sqlException) {
            displayAlert("Database Error",
                    sqlException.getMessage());
            tableModel.disconnectFromDatabase(); // close connection
            System.exit(1); // terminate application
        }
    }

    // query the database and display results in JTable
    @FXML
    void submitQueryButtonPressed () {
        // perform a new query
        try {
            tableModel.setQuery(queryTextArea.getText());
        } catch (SQLException sqlException) {
            displayAlert("Database Error",
                    sqlException.getMessage());

            // try to recover from invalid user query
            // by executing default query
            try {
                tableModel.setQuery(DEFAULT_QUERY);
                queryTextArea.setText(DEFAULT_QUERY);
            } catch (SQLException sqlException2) {
                displayAlert("Database Error",
                        sqlException2.getMessage());
                tableModel.disconnectFromDatabase(); // close connection
                System.exit(1); // terminate application
            }
        }
    }

    // apply specified filter to results
    @FXML
    void applyFilterButtonPressed () {
        String text = filterTextField.getText();

        if (text.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                sorter.setRowFilter(RowFilter.regexFilter(text));
            } catch (PatternSyntaxException pse) {
                displayAlert("Regex Error",
                        "Bad regex pattern");
            }
        }
    }

    @FXML
    void simpleQueriesButtonPressed () {

        try {
            tableModel.setQuery(NUM_ORDERS_PRICE_PER_DAY_QUERY);
            queryTextArea.setText(NUM_ORDERS_PRICE_PER_DAY_QUERY);
        } catch (SQLException sqlException2) {
            displayAlert("Database Error",
                    sqlException2.getMessage());
            tableModel.disconnectFromDatabase(); // close connection  
            System.exit(1); // terminate application
        }
    }

    @FXML
    void dateFilterButtonPressed () throws SQLException {

        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        String searchString = DEFAULT_QUERY + " where date(OrderTime) >= '" + from + "' AND date(OrderTime) <= '" + to + "'";

        if (from == null || to == null) {
            tableModel.setQuery(DEFAULT_QUERY);
            queryTextArea.setText(DEFAULT_QUERY);
        } else {
            try {
                tableModel.setQuery(searchString);
                queryTextArea.setText(searchString);
            } catch (SQLException sqlException2) {
                displayAlert("Database Error",
                        sqlException2.getMessage());
                tableModel.disconnectFromDatabase(); // close connection
                System.exit(1); // terminate application
            }
        }
    }

    private void Search (String phone, String phoneString) throws SQLException {
        if (phone.length() == 0) {
            tableModel.setQuery(ORDER_SEARCH_BY);
            queryTextArea.setText(ORDER_SEARCH_BY);
        } else {
            try {
                tableModel.setQuery(phoneString);
                queryTextArea.setText(phoneString);
            } catch (SQLException sqlException2) {
                displayAlert("Database Error",
                        sqlException2.getMessage());
                tableModel.disconnectFromDatabase(); // close connection
                System.exit(1); // terminate application
            }
        }
    }

    @FXML
    void phoneQueries () throws SQLException {
        String phone = phoneTextField.getText();
        String phoneString = ORDER_SEARCH_BY + " where customers.phonenumber = '" + phone + "'";

        Search(phone, phoneString);
    }

    @FXML
    void lastNameQueries () throws SQLException {
        String last = lastNameTextField.getText();
        String nameString = ORDER_SEARCH_BY + " where lastname = '" + last + "'";

        Search(last, nameString);
    }

    // display an Alert dialog
    private void displayAlert (
            String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}