# 📖 Book Store - Spring Boot Web Application

A complete web application for an online book store, built with the Spring ecosystem. It provides a feature-rich experience for both customers and administrators, from browsing and searching to order and user management.

## ✨ Features

- **Customer-Facing:**
    - `User Registration & Login`: Secure account creation and session-based authentication.
    - `Book Catalog`: Browse and search books by title, author, or genre.
    - `Shopping Cart`: Add books to a persistent draft order.
    - `Order Management`: View order history and submit new orders.
    - `Profile Management`: View personal details and top up account balance.
- **Administrator Panel:**
    - `Book Management`: Full CRUD (Create, Read, Update, Delete) for the book catalog.
    - `Order Management`: View and manage all customer orders with balance visibility.
    - `Client Management`: View and manage all client accounts (block/unblock).
    - `Search Functionality`: Powerful search across all management pages.

## 🛠️ Tech Stack

- **Backend:**
    - `Java 21`
    - `Spring Boot 3`
    - `Spring Security` (Session Management)
    - `Spring Data JPA` (Hibernate)
    - `Maven`
- **Frontend:**
    - `Thymeleaf`
    - `Bootstrap 5`
- **Database:**
    - `MySQL 8+`
    - `Flyway` (Database Migrations)
- **Tooling:**
    - `Lombok`
    - `MapStruct`

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your Windows machine:

1.  **Java JDK 21** or later. You can verify your installation by running `java -version` in the Command Prompt.
2.  **Apache Maven** 3.8 or later. You can verify by running `mvn -v`.
3.  **MySQL Server 8.0** or later. The MySQL Installer for Windows is a recommended way to set it up.
4.  An IDE of your choice (e.g., IntelliJ IDEA, Eclipse, VS Code).

---

## 🚀 Getting Started on Windows

Follow these steps to set up and run the application on your local Windows environment.

### 1. Database Setup

The application requires a MySQL database.

**1.1. Create the Database:**
Open your preferred MySQL client (like MySQL Workbench or the command line client) and execute the following command to create an empty database for the application:

```sql
CREATE DATABASE bookdb;
```

**1.2. Verify Database Credentials:**
By default, the application is configured to connect using the username `root` and password `root`. If your MySQL installation uses different credentials, you **must** update them in the `src/main/resources/application.properties` file:

```properties
# src/main/resources/application.properties

spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 2. Configure the Default Administrator Account

The application automatically creates a default administrator on its first run. For security, the admin password must be configured as an environment variable.

**2.1. Set the Environment Variable:**
You need to set the `ADMIN_PASSWORD` variable. This will be the password for the default admin user (`admin@example.com`).

**Option A: Using Command Prompt (Temporary)**
Open a Command Prompt window and run the following command. This variable will only exist for the lifetime of this terminal window.

```cmd
set ADMIN_PASSWORD="YourSecurePassword123!"
```

**Option B: Using PowerShell (Temporary)**
Open a PowerShell window and run the following command.

```powershell
$env:ADMIN_PASSWORD="YourSecurePassword123!"
```

**Option C: Setting it Permanently in Windows (Recommended)**
1.  Search for "Edit the system environment variables" in the Start Menu and open it.
2.  In the System Properties window, click the `Environment Variables...` button.
3.  In the "User variables" section, click `New...`.
4.  Enter `ADMIN_PASSWORD` as the "Variable name".
5.  Enter your desired secure password as the "Variable value".
6.  Click `OK` on all windows. You may need to restart your IDE or terminal for the change to take effect.

**Option D: Setting it in IntelliJ IDEA**
1.  Go to `Run` -> `Edit Configurations...`
2.  Select your Spring Boot application configuration.
3.  Click on the `Environment variables` field and then the browse icon.
4.  Click the `+` icon and add a new variable:
    -   **Name:** `ADMIN_PASSWORD`
    -   **Value:** `YourSecurePassword123!`
5.  Click `OK` to save.

### 3. Build and Run the Application

Now you are ready to launch the project.

**3.1. Build the Project:**
Open a terminal (Command Prompt or PowerShell) in the root directory of the project and run the Maven `clean install` command. This will compile the code and download all necessary dependencies.

```bash
mvn clean install
```

**3.2. Run the Application:**
After a successful build, run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

Alternatively, you can locate the main application class `BookServiceApplication.java` in your IDE and run it directly.

### 4. First Launch and Database Initialization

When the application starts for the first time, you will see a lot of activity in the console. Here is what happens automatically:
1.  **Flyway Migrations:** Flyway connects to your empty `bookdb` database, finds the SQL scripts in `src/main/resources/db/migration`, and executes them to create all the tables (`books`, `users`, etc.) and populate the `books` table with sample data.
2.  **Admin User Creation:** The `DataInitializer` runs and creates the default admin user with the email `admin@example.com` and the password you set as an environment variable.

### 5. Accessing the Application

Once the application is running, open your web browser and navigate to the following URLs:

- **Homepage:** `http://localhost:8080/`
- **Login Page:** `http://localhost:8080/auth/login`

To access the administrator panels, log in with the following credentials:
- **Email:** `admin@example.com`
- **Password:** The value you set for the `ADMIN_PASSWORD` environment variable.

You are now ready to use and explore the Book Store application