SELECT EXISTS (
  SELECT 1
  FROM friendship
  WHERE status = 'PENDING'
    AND requester_id = (SELECT id FROM `user` WHERE username = :requesterUsername)
    AND addressee_id = (SELECT id FROM `user` WHERE username = :addresseeUsername)
)
