-- Table: ainntekt

-- DROP TABLE ainntekt;

CREATE TABLE IF NOT EXISTS ainntekt
(
    inntekt_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    grunnlagspakke_id integer NOT NULL,
    person_id varchar(50) not null,
    periode_fra date NOT NULL,
    periode_til date NOT NULL,
    aktiv boolean DEFAULT true NOT NULL,
    bruk_fra timestamp DEFAULT now() NOT NULL,
    bruk_til timestamp,
    hentet_tidspunkt timestamp DEFAULT now() NOT NULL,
    CONSTRAINT ainntekt_pkey PRIMARY KEY (inntekt_id),
    CONSTRAINT grunnlagspakke_ainntekt_fkey FOREIGN KEY (grunnlagspakke_id)
        REFERENCES grunnlagspakke (grunnlagspakke_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (inntekt_id, grunnlagspakke_id)
)

    TABLESPACE pg_default;