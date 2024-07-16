import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TaskManager extends JFrame {
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextField taskNameField, taskDescriptionField, taskCategoryField;
    private JTextArea taskDetailsArea;
    private JComboBox<String> categoryComboBox;
    private Connection dbConnection;

    public TaskManager() {
        setTitle("Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);

        initComponents();
        initDatabase();
        loadTasksFromDatabase();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Task List
        JPanel taskListPanel = new JPanel(new BorderLayout());
        taskListPanel.setBorder(BorderFactory.createTitledBorder("Task List"));
        tableModel = new DefaultTableModel(new Object[]{"Task Name", "Description", "Category"}, 0);
        taskTable = new JTable(tableModel);
        JScrollPane taskScrollPane = new JScrollPane(taskTable);
        taskListPanel.add(taskScrollPane, BorderLayout.CENTER);
        mainPanel.add(taskListPanel, BorderLayout.CENTER);

        // Task Entry
        TaskEntryPanel taskEntryPanel = new TaskEntryPanel();
        mainPanel.add(taskEntryPanel, BorderLayout.EAST);

        // Get Description Button
        JButton getDescriptionButton = new JButton("Get Description");
        getDescriptionButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow != -1) {
                String taskName = (String) tableModel.getValueAt(selectedRow, 0);
                displayTaskDetails(taskName);
            } else {
                JOptionPane.showMessageDialog(TaskManager.this, "Please select a task to get its description.");
            }
        });
        taskEntryPanel.addButton(getDescriptionButton);

        // Delete Button
        JButton deleteTaskButton = new JButton("Finish Task");
        deleteTaskButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow != -1) {
                String taskName = (String) tableModel.getValueAt(selectedRow, 0);
                deleteTask(taskName);
            } else {
                JOptionPane.showMessageDialog(TaskManager.this, "Please select a task to Finish.");
            }
        });
        taskEntryPanel.addButton(deleteTaskButton);

        // Export Button
        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export to CSV");
            int userSelection = fileChooser.showSaveDialog(TaskManager.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                exportDataToCSV(fileToSave);
                JOptionPane.showMessageDialog(TaskManager.this, "Data exported to CSV successfully.");
            }
        });
        taskEntryPanel.addButton(exportButton);

        // Category Management
        CategoryPanel categoryPanel = new CategoryPanel();
        mainPanel.add(categoryPanel, BorderLayout.WEST);

        add(mainPanel);
    }

    private void initDatabase() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;database=JDFA2TaskDB;integratedSecurity=true;encrypt=true;trustServerCertificate=true";
            dbConnection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromDatabase() {
        try {
            Statement statement = dbConnection.createStatement();

            // Load tasks
            ResultSet resultSet = statement.executeQuery("SELECT TaskName, Description, Category FROM Tasks");
            tableModel.setRowCount(0);
            while (resultSet.next()) {
                String taskName = resultSet.getString("TaskName");
                String description = resultSet.getString("Description");
                String category = resultSet.getString("Category");
                tableModel.addRow(new Object[]{taskName, description, category});
            }
            resultSet.close();

            // Load categories
            resultSet = statement.executeQuery("SELECT DISTINCT Category FROM Tasks");
            categoryComboBox.removeAllItems(); // Clear existing items
            categoryComboBox.addItem("All"); // Add "All" option
            while (resultSet.next()) {
                String category = resultSet.getString("Category");
                categoryComboBox.addItem(category); // Add categories to combo box
            }
            resultSet.close();

            File file = new File("C:\\Users\\Asher\\IdeaProjects\\My Stuff\\JD522FA2\\src\\task.txt");

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromTextFile(File file) {
        try (InputStream inputStream = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line into task components
                String[] taskComponents = line.split(",");
                if (taskComponents.length >= 3) { // Ensure at least TaskName, Description, and Category are present
                    String taskName = taskComponents[0];
                    String description = taskComponents[1];
                    String category = taskComponents[2];
                    tableModel.addRow(new Object[]{taskName, description, category});
                }
            }

            // Display file properties
            displayFileProperties(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayFileProperties(File file) {
        try {
            Path path = file.toPath();
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            System.out.println("File Size: " + attributes.size() + " bytes");
            System.out.println("Creation Time: " + attributes.creationTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTask() {
        String taskName = taskNameField.getText();
        String description = taskDescriptionField.getText();
        String category = taskCategoryField.getText();
        String details = taskDetailsArea.getText();

        try {
            // Save to database
            PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO Tasks (TaskName, Description, Category, Details) VALUES (?, ?, ?, ?)");
            statement.setString(1, taskName);
            statement.setString(2, description);
            statement.setString(3, category);
            statement.setString(4, details);
            statement.executeUpdate();
            statement.close();

            // Save to file
            saveTasksToFile(new File("C:\\Users\\Asher\\IdeaProjects\\My Stuff\\JD522FA2\\src\\task.txt"));

            // Reload tasks from both database and file
            loadTasksFromDatabase();
            clearInputFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveTasksToFile(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.print(tableModel.getValueAt(i, j));
                    if (j < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void filterTasks() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        try {
            Statement statement = dbConnection.createStatement();
            String query = "SELECT TaskName, Description, Category FROM Tasks";
            assert selectedCategory != null;
            if (!selectedCategory.equals("All")) {
                query += " WHERE Category = '" + selectedCategory + "'";
            }
            ResultSet resultSet = statement.executeQuery(query);
            tableModel.setRowCount(0);
            while (resultSet.next()) {
                String taskName = resultSet.getString("TaskName");
                String description = resultSet.getString("Description");
                String category = resultSet.getString("Category");
                tableModel.addRow(new Object[]{taskName, description, category});
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exportDataToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Write header
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.print(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            // Write data
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.print(tableModel.getValueAt(i, j));
                    if (j < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deleteTask(String taskName) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("DELETE FROM Tasks WHERE TaskName = ?");
            statement.setString(1, taskName);
            statement.executeUpdate();
            statement.close();

            // Reload tasks from the database to update the UI
            loadTasksFromDatabase();

            // Update the text file with the latest data
            saveTasksToFile(new File("C:\\Users\\Asher\\IdeaProjects\\My Stuff\\JD522FA2\\src\\task.txt"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayTaskDetails(String taskName) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM Tasks WHERE TaskName = ?");
            statement.setString(1, taskName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String taskDescription = resultSet.getString("Description");
                String taskCategory = resultSet.getString("Category");
                String taskDetails = resultSet.getString("Details");

                // Display details in a dialog or a separate window
                JTextArea detailsArea = new JTextArea(10, 30);
                detailsArea.setText("Task Name: " + taskName + "\n"
                        + "Description: " + taskDescription + "\n"
                        + "Category: " + taskCategory + "\n"
                        + "Details: " + taskDetails);
                detailsArea.setEditable(false);
                JOptionPane.showMessageDialog(TaskManager.this, new JScrollPane(detailsArea), "Task Details", JOptionPane.INFORMATION_MESSAGE);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearInputFields() {
        taskNameField.setText("");
        taskDescriptionField.setText("");
        taskCategoryField.setText("");
        taskDetailsArea.setText("");
    }

    // Inner class for Task Entry Panel
    private class TaskEntryPanel extends JPanel {
        public TaskEntryPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Task Entry"));
            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            inputPanel.add(new JLabel("Task Name:"));
            taskNameField = new JTextField();
            inputPanel.add(taskNameField);
            inputPanel.add(new JLabel("Description:"));
            taskDescriptionField = new JTextField();
            inputPanel.add(taskDescriptionField);
            inputPanel.add(new JLabel("Category:"));
            taskCategoryField = new JTextField();
            inputPanel.add(taskCategoryField);
            inputPanel.add(new JLabel("Details:"));
            taskDetailsArea = new JTextArea(5, 20);
            JScrollPane detailsScrollPane = new JScrollPane(taskDetailsArea);
            inputPanel.add(detailsScrollPane);
            add(inputPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton addTaskButton = new JButton("Add Task");
            addTaskButton.addActionListener(e -> addTask());
            buttonPanel.add(addTaskButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public void addButton(JButton button) {
            ((JPanel) getComponent(1)).add(button);
        }
    }

    // Inner class for Category Panel
    private class CategoryPanel extends JPanel {
        public CategoryPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Categories"));
            categoryComboBox = new JComboBox<>();
            categoryComboBox.addItem("All");
            add(categoryComboBox, BorderLayout.NORTH);
            JButton filterButton = new JButton("Filter");
            filterButton.addActionListener(e -> filterTasks());
            add(filterButton, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManager taskManager = new TaskManager();
            taskManager.setVisible(true);
        });
    }
}

