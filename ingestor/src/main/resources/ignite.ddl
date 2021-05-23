CREATE TABLE IF NOT EXISTS `forms` (
  `id` VARCHAR(100) NOT NULL,
  `form_content` VARCHAR NOT NULL,
  `updated_at` BIGINT NOT NULL,
  PRIMARY KEY (`id`));