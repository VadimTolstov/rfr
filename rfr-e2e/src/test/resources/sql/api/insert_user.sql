INSERT INTO `user` (id, username, country_id)
SELECT UUID_TO_BIN(:id, TRUE), :username, id FROM country WHERE code = 'ru'
