use requestdb;

ALTER TABLE active_requests
MODIFY STATUS_MESSAGE LONGTEXT;


ALTER TABLE infra_active_requests
MODIFY STATUS_MESSAGE LONGTEXT;
