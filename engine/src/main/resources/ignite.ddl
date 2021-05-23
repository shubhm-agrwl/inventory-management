CREATE TABLE IF NOT EXISTS `truck_inventory` (
      `id` VARCHAR(100) NOT NULL,
      `truck_id` VARCHAR(100) NOT NULL,
      `truck_registration` BIGINT NULL,
      `site_entry` BIGINT NULL,
      `site_exit` BIGINT NULL,
      `refinery_entry` BIGINT NULL,
      `refinery_exit` BIGINT NULL,
      `warehouse_entry` BIGINT NULL,
      `warehouse_exit` BIGINT NULL,
      `sync` BOOLEAN NOT NULL,
      PRIMARY KEY (`id`));