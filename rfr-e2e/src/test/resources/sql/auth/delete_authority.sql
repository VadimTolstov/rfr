DELETE FROM authority
WHERE user_id = (SELECT id FROM `user` WHERE username = :username)
