CREATE TABLE IF NOT EXISTS `immunization_decider`.`decision_requests`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT UNSIGNED NOT NULL,
    `request_id`    VARCHAR(64)     NOT NULL,
    `source_ref_id` VARCHAR(64)     NOT NULL,
    `status`        VARCHAR(64)     NOT NULL,
    `error`         VARCHAR(2048),
    `started_at`    DATETIME(6)     NOT NULL,
    `finished_at`   DATETIME(6),

    PRIMARY KEY (`id`),
    UNIQUE INDEX `source_ref_id_idx` (`source_ref_id`)
);

CREATE TABLE IF NOT EXISTS `immunization_decider`.`decision_results`
(
    `decision_request_id`     BIGINT UNSIGNED NOT NULL,
    `user_id`                 BIGINT UNSIGNED NOT NULL,
    `request_id`              VARCHAR(64)     NOT NULL,
    `source_ref_id`           VARCHAR(64)     NOT NULL,
    `available_immunizations` JSON            NOT NULL,

    PRIMARY KEY (`decision_request_id`),
    UNIQUE INDEX `source_ref_id_idx` (`source_ref_id`)
);

