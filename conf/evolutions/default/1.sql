# --- !Ups

CREATE TABLE "users" (
    "id" bigserial NOT NULL,
    "email" text NOT NULL
    PRIMARY KEY ("id")
);

# --- !Downs

DROP TABLE IF EXISTS users CASCADE;
