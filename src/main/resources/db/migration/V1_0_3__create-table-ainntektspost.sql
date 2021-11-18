-- Table: inntektspost

-- DROP TABLE ainntektspost;

CREATE TABLE IF NOT EXISTS ainntektspost
(
    inntektspost_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    inntekt_id integer NOT NULL,
    utbetalingsperiode char(7),
    opptjeningsperiode_fra date,
    opptjeningsperiode_til date,
    opplysningspliktig_id varchar(255),
    virksomhet_id varchar(255),
    inntekt_type varchar(255) NOT NULL,
    fordel_type varchar(255),
    beskrivelse varchar(255),
    belop float NOT NULL,
    CONSTRAINT ainntektspost_pkey PRIMARY KEY (inntektspost_id),
    CONSTRAINT ainntekt_fkey FOREIGN KEY (inntekt_id)
        REFERENCES ainntekt (inntekt_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (inntekt_id, utbetalingsperiode, opplysningspliktig_id, inntekt_type, fordel_type, beskrivelse)
)

    TABLESPACE pg_default;
