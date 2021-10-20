-- Table: inntekt_skatt

-- DROP TABLE inntekt_skatt;

CREATE TABLE IF NOT EXISTS inntekt_skatt
(
    inntekt_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    grunnlagspakke_id integer NOT NULL,
    person_id integer NOT NULL,
    type varchar(255) NOT NULL,
    periode_fra date NOT NULL,
    periode_til date NOT NULL,
    belop float NOT NULL,
    aktiv boolean DEFAULT true NOT NULL,
    bruk_fra timestamp DEFAULT now() NOT NULL,
    bruk_til timestamp,
    hentet_tidspunkt timestamp DEFAULT now() NOT NULL,
    CONSTRAINT inntekt_skatt_pkey PRIMARY KEY (inntekt_id),
    CONSTRAINT grunnlagspakke_skatt_fkey FOREIGN KEY (grunnlagspakke_id)
        REFERENCES grunnlagspakke (grunnlagspakke_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (inntekt_id, grunnlagspakke_id)
)

    TABLESPACE pg_default;