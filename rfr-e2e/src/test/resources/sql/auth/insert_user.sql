INSERT INTO `user` (id, username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES (UUID_TO_BIN(:id, TRUE), :username, :password, TRUE, TRUE, TRUE, TRUE)
