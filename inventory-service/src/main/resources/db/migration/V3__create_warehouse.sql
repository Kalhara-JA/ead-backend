-- Create the warehouse table
CREATE TABLE t_warehouse (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    manager_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- Add a foreign key to the inventory table
ALTER TABLE t_inventory
ADD COLUMN warehouse_id BIGINT(20),
ADD CONSTRAINT fk_warehouse_inventory
FOREIGN KEY (warehouse_id) REFERENCES t_warehouse(id);


-- Insert some data into the warehouse table
INSERT INTO t_warehouse (name, address, manager_name)
VALUES
    ('Warehouse 1', '123 Main St', 'John Doe'),
    ('Warehouse 2', '456 Elm St', 'Jane Smith');


-- Update the inventory table to reference the warehouse table
UPDATE t_inventory SET warehouse_id = 1 WHERE location = 'warehouse_1';
UPDATE t_inventory SET warehouse_id = 2 WHERE location = 'warehouse_2';
