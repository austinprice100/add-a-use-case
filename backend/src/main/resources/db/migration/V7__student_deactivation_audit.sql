-- Record when a student was deactivated and who deactivated her (UC-STU-deactivate-student).
-- The users share one table (single-table inheritance on dtype), so the columns sit on
-- peer_evaluation_user, but only Student maps them. Both stay NULL for an enabled student, and
-- for a student disabled before this migration, whose deactivation date is unknown: there is
-- no backfill. The foreign key keeps the default RESTRICT so the audit record is never erased
-- silently. No index: every lookup is by the student's primary key.

ALTER TABLE `peer_evaluation_user`
  ADD COLUMN `deactivated_at` datetime(6) DEFAULT NULL,
  ADD COLUMN `deactivated_by_id` int DEFAULT NULL,
  ADD CONSTRAINT `fk_peer_evaluation_user_deactivated_by`
    FOREIGN KEY (`deactivated_by_id`) REFERENCES `peer_evaluation_user` (`id`);
