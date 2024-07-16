Task Manager Application

The Task Manager application is a Java-based desktop tool for managing tasks, storing them in a SQL Server database, and exporting them to CSV files.
Features

    View a list of tasks including their descriptions and categories.
    Add new tasks with details such as task name, description, category, and additional notes.
    Filter tasks by category to streamline task management.
    View detailed information of a selected task.
    Export tasks to a CSV file for external use.

Technologies Used

    Java SE
    JDBC for database connectivity (SQL Server)
    Swing for the GUI components

Setup Instructions

    Java Development Kit (JDK):
        Ensure you have Java Development Kit (JDK) installed on your system. You can download it from Oracle's website.

    Database Setup:
        The application is configured to connect to a SQL Server database . Make sure you have SQL Server running locally or adjust the database connection URL 
        (initDatabase method) accordingly.

    IDE Setup:
        Import the project into your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).

    Dependencies:
        Ensure all necessary libraries are included. This application uses standard Java libraries and JDBC for database connectivity.

    Run the Application:
        Run the TaskManager class which contains the main method.
        The application will launch, and you can start managing tasks using the graphical user interface.

How to Use

    Task List: Displays a list of tasks with their descriptions and categories.
    Task Entry: Allows adding new tasks with details.
    Category Management: Filters tasks based on selected categories.
    Export: Export tasks to a CSV file.
    Task Details: Click on a task to view detailed information.

Additional Notes

    This application demonstrates basic CRUD operations with a SQL Server database.
    File task.txt is used for loading tasks from a text file.
    Ensure proper database and file permissions are set up for smooth operation.

Author

    Developed by Ash
