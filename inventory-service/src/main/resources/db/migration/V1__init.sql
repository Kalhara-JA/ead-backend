CREATE TABLE `t_inventory` (
                               `id`       BIGINT(20) NOT NULL AUTO_INCREMENT,
                               `sku_code` VARCHAR(255) NOT NULL,
                               `quantity` INT(11) NOT NULL,
                               `location` VARCHAR(255) NOT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uniq_sku_location` (`sku_code`, `location`)
);
