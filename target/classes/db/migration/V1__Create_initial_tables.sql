CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE,
                       genre VARCHAR(100) NOT NULL,
                       target_age_group VARCHAR(20),
                       price DECIMAL(10, 2) NOT NULL,
                       publication_date DATE NOT NULL,
                       author VARCHAR(100) NOT NULL,
                       pages INT NOT NULL,
                       characteristics TEXT,
                       description TEXT,
                       language VARCHAR(20)
);

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       role VARCHAR(255) NOT NULL
);

CREATE TABLE admins (
                        user_id BIGINT PRIMARY KEY,
                        CONSTRAINT fk_admins_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE clients (
                         user_id BIGINT PRIMARY KEY,
                         balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
                         CONSTRAINT fk_clients_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        order_date DATETIME NOT NULL,
                        price DECIMAL(10, 2) NOT NULL,
                        status VARCHAR(255) NOT NULL,
                        client_id BIGINT NOT NULL,
                        CONSTRAINT fk_orders_clients FOREIGN KEY (client_id) REFERENCES clients(user_id)
);

CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             quantity INT NOT NULL,
                             book_id BIGINT NOT NULL,
                             order_id BIGINT NOT NULL,
                             CONSTRAINT fk_order_items_books FOREIGN KEY (book_id) REFERENCES books(id),
                             CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id)
);