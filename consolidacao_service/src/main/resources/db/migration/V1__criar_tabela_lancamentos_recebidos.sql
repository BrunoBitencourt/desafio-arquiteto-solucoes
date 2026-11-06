
CREATE TABLE lancamentos_recebidos (
    id BIGINT PRIMARY KEY,
    valor NUMERIC(19, 2) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    data_lancamento DATE NOT NULL,
    CONSTRAINT chk_recebidos_valor CHECK (valor > 0),
    CONSTRAINT chk_recebidos_tipo CHECK (tipo IN ('CREDITO', 'DEBITO'))
);
