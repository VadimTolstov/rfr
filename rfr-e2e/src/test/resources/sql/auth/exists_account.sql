SELECT EXISTS (
  SELECT 1
  FROM `user` account
  WHERE account.username = :username
    AND EXISTS (
      SELECT 1
      FROM authority
      WHERE user_id = account.id AND authority = 'read'
    )
    AND EXISTS (
      SELECT 1
      FROM authority
      WHERE user_id = account.id AND authority = 'write'
    )
)
