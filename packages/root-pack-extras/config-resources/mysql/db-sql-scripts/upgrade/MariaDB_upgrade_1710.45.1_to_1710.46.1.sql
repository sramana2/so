SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

ALTER SCHEMA `mso_requests`  DEFAULT CHARACTER SET latin1  DEFAULT COLLATE latin1_swedish_ci ;

ALTER TABLE `mso_requests`.`active_requests` 
CHANGE COLUMN `CLIENT_REQUEST_ID` `CLIENT_REQUEST_ID` VARCHAR(45) NULL DEFAULT NULL AFTER `REQUEST_ID`;

ALTER TABLE `mso_requests`.`infra_active_requests` 
CHANGE COLUMN `CLIENT_REQUEST_ID` `CLIENT_REQUEST_ID` VARCHAR(45) NULL DEFAULT NULL AFTER `REQUEST_ID`,
CHANGE COLUMN `ACTION` `ACTION` VARCHAR(45) NULL DEFAULT NULL ,
CHANGE COLUMN `LAST_MODIFIED_BY` `LAST_MODIFIED_BY` VARCHAR(100) NULL DEFAULT NULL ,
ADD COLUMN `CONFIGURATION_ID` VARCHAR(45) NULL DEFAULT NULL AFTER `REQUESTOR_ID`,
ADD COLUMN `CONFIGURATION_NAME` VARCHAR(200) NULL DEFAULT NULL AFTER `CONFIGURATION_ID`;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
