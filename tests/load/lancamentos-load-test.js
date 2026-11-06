import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';

export const options = {
  scenarios: {
    carga: {
      executor: 'constant-arrival-rate',
      rate: 50,
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 100,
      maxVUs: 150,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const payload = JSON.stringify({
    valor: 10.00,
    tipo: 'CREDITO',
    dataLancamento: '2026-11-06',
    descricao: 'Teste de carga k6',
  });

  const credentials = `${__ENV.APP_USER}:${__ENV.APP_PASSWORD}`;
  const encoded = encoding.b64encode(credentials);

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Basic ${encoded}`,
    },
  };

  const response = http.post(
    'http://localhost:8080/api/lancamentos',
    payload,
    params
  );

  check(response, {
    'status 201': (r) => r.status === 201,
  });
}