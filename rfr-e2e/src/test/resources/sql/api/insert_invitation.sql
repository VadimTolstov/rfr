INSERT INTO friendship
    (id, requester_id, addressee_id, status, created_at, responded_at)
SELECT UUID_TO_BIN(UUID(), TRUE),
       requester.id,
       addressee.id,
       'PENDING',
       CURRENT_TIMESTAMP(6),
       NULL
FROM `user` requester
JOIN `user` addressee ON addressee.username = :addresseeUsername
WHERE requester.username = :requesterUsername
