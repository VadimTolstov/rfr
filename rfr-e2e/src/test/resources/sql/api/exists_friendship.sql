SELECT EXISTS (
  SELECT 1
  FROM friendship
  WHERE status = 'ACCEPTED'
    AND (
      (requester_id = (SELECT id FROM `user` WHERE username = :requesterUsername)
        AND addressee_id = (SELECT id FROM `user` WHERE username = :addresseeUsername))
      OR
      (requester_id = (SELECT id FROM `user` WHERE username = :addresseeUsername)
        AND addressee_id = (SELECT id FROM `user` WHERE username = :requesterUsername))
    )
)
