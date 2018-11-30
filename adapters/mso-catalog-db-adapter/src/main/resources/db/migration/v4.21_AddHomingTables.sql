CREATE TABLE IF NOT EXISTS `homing_instances` (
`SERVICE_INSTANCE_ID` varchar(50) NOT NULL,
`CLOUD_OWNER` VARCHAR(200) NOT NULL,
`CLOUD_REGION_ID` VARCHAR(200) NOT NULL,
`OOF_DIRECTIVES` longtext NULL DEFAULT NULL,
PRIMARY KEY (`SERVICE_INSTANCE_ID`)
) ;