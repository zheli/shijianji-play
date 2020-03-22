# --- !Ups

CREATE TABLE "USER" (
    "ID" bigserial NOT NULL,
    "EMAIL" text NOT NULL,
    PRIMARY KEY ("ID")
);

# --- !Downs

DROP TABLE IF EXISTS USER CASCADE;
