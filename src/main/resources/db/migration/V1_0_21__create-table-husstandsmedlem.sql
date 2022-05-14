-- Table: husstandsmedlem

-- DROP TABLE husstandsmedlem;

CREATE TABLE IF NOT EXISTS husstandsmedlem
(
    husstandsmedlem_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    husstand_id integer NOT NULL,
    navn varchar(255),
    periode_fra date,
    periode_til date,
    opprettet_av varchar(255),
    opprettet_tidspunkt timestamp DEFAULT now() NOT NULL,
    CONSTRAINT husstandsmedlem_pkey PRIMARY KEY (husstandsmedlem_id),
    CONSTRAINT husstand_fkey FOREIGN KEY (husstand_id)
        REFERENCES husstand (person_db_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (husstandsmedlem_id, husstand_id)
)

    TABLESPACE pg_default;
