INSERT INTO authority (user_id, authority)
SELECT id, :authority FROM `user` WHERE username = :username
