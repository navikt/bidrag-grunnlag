-- Table: person

-- DROP TABLE person;

CREATE TABLE IF NOT EXISTS person
(
    person_db_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    grunnlagspakke_id integer NOT NULL,
    person_id varchar(50),
    navn varchar(255),
    foedselsdato date,
    doedsdato date,
    aktiv boolean DEFAULT true NOT NULL,
    bruk_fra timestamp DEFAULT now() NOT NULL,
    bruk_til timestamp,
    opprettet_av varchar(255),
    opprettet_tidspunkt timestamp DEFAULT now() NOT NULL,
    CONSTRAINT person_pkey PRIMARY KEY (person_db_id),
    CONSTRAINT grunnlagspakke_person_fkey FOREIGN KEY (grunnlagspakke_id)
        REFERENCES grunnlagspakke (grunnlagspakke_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (person_db_id, grunnlagspakke_id)
)

    TABLESPACE pg_default;