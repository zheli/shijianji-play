# --- !Ups

CREATE TABLE "USER" (
    "USER_ID" bigserial NOT NULL,
    "EMAIL" text NOT NULL,
    PRIMARY KEY ("USER_ID")
);

# --- !Downs

DROP TABLE IF EXISTS USER CASCADE;
