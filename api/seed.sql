CREATE DATABASE IF NOT EXISTS orders;
USE orders;

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0
);

INSERT INTO products (name, description, price, stock) VALUES
('Laptop', 'A powerful laptop', 999.99, 50),
('Smartphone', 'A modern smartphone', 499.99, 200),
('Headphones', 'Noise-cancelling headphones', 199.99, 100);

CREATE TABLE IF NOT EXISTS clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE        
);

INSERT INTO clients (name, address, email, phone_number) VALUES
('Alice Johnson', '123 Main St, Springfield', 'alice@example.com', '555-1234'),
('Bob Smith', '456 Elm St, Metropolis', 'bob@example.com', '555-5678'),
('Charlie Brown', '789 Oak St, Gotham', 'charlie@example.com', '555-9012');

CREATE TABLE IF NOT EXISTS purchases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT,
    product_name VARCHAR(255) NOT NULL,
    amount INT NOT NULL,
    date DATE,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

INSERT INTO purchases (client_id, product_name, amount, date) VALUES
(1, 'Laptop', 1, '2024-01-15'),
(1, 'Headphones', 2, '2024-02-10'),
(2, 'Smartphone', 1, '2024-03-05'),
(3, 'Laptop', 1, '2024-03-20'),
(3, 'Smartphone', 1, '2024-04-25');
