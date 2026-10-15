CREATE TABLE vehicles (
    id           UUID PRIMARY KEY,
    brand        VARCHAR(60)    NOT NULL,
    model        VARCHAR(60)    NOT NULL,
    vehicle_year INTEGER        NOT NULL,
    color        VARCHAR(30)    NOT NULL,
    price        NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    version      BIGINT         NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL
);

-- atende a listagem administrativa, do cadastro mais recente para o mais antigo
CREATE INDEX idx_vehicles_created_at ON vehicles (created_at DESC, id);
