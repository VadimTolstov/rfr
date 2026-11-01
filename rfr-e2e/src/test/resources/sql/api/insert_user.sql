INSERT INTO `user` (username, country_id)
SELECT :username, id FROM country WHERE code = 'ru'
