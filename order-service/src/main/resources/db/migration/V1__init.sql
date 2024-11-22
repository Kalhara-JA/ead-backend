CREATE TABLE `t_orders` (
                            `id`           BIGINT(20) NOT NULL AUTO_INCREMENT,
                            `order_number` VARCHAR(255) NOT NULL UNIQUE,
                            `user_email`   VARCHAR(255),
                            `shipping_address`   VARCHAR(255),
                            `total`     DECIMAL(19, 2) NOT NULL,
                            `order_date`   DATE,
                            `status`       VARCHAR(50),
                            PRIMARY KEY (`id`)
);


CREATE TABLE `t_order_items` (
                                 `id`        BIGINT(20) NOT NULL AUTO_INCREMENT,
                                 `sku_code`  VARCHAR(255) NOT NULL,
                                 `quantity`  INT(11) NOT NULL,
                                 `order_id`  BIGINT(20) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 CONSTRAINT `fk_order_items_orders`
                                     FOREIGN KEY (`order_id`) REFERENCES `t_orders` (`id`)
                                         ON DELETE CASCADE ON UPDATE CASCADE
);
