import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const TOKEN_CSV_PATH = __ENV.TOKEN_CSV_PATH || '../../qiin-be/output/user_tokens.csv';

const PAGE = __ENV.PAGE || '0';
const SIZE = __ENV.SIZE || '20';

// 전후 비교에서는 같은 검색 조건으로 고정해서 실행
const RESERVATION_STATUS = __ENV.RESERVATION_STATUS || '';
const START_DATE = __ENV.START_DATE || '';
const END_DATE = __ENV.END_DATE || '';
const APPLICANT_NAME = __ENV.APPLICANT_NAME || '';
const ASSET_NAME = __ENV.ASSET_NAME || '';
const CATEGORY_ID = __ENV.CATEGORY_ID || '';

const users = new SharedArray('users', function () {
  return open(TOKEN_CSV_PATH)
    .split('\n')
    .slice(1)
    .filter(Boolean)
    .map(line => {
      const [userId, accessToken] = line.split(',');

      return {
        userId: userId.trim(),
        accessToken: accessToken.trim(),
      };
    });
});

//1명 실행 용도
// export const options = {
//  scenarios: {
//    applied_reservations_nplus1_load: {
//      executor: 'shared-iterations',
//      vus: 1,
//      iterations: 1,
//    },
//  },
// };

export const options = {
  scenarios: {
    applied_reservations_nplus1_load: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 5),
      duration: __ENV.DURATION || '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<1500'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

function addParam(params, key, value) {
  if (value !== undefined && value !== null && value !== '') {
    params.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
  }
}

function buildQuery() {
  const params = [];

  addParam(params, 'page', PAGE);
  addParam(params, 'size', SIZE);

  addParam(params, 'reservationStatus', RESERVATION_STATUS);
  addParam(params, 'startDate', START_DATE);
  addParam(params, 'endDate', END_DATE);
  addParam(params, 'applicantName', APPLICANT_NAME);
  addParam(params, 'assetName', ASSET_NAME);
  addParam(params, 'categoryId', CATEGORY_ID);

  return params.join('&');
}

export default function () {
  const user = users[(__VU - 1) % users.length];

  const query = buildQuery();
  const url = `${BASE_URL}/api/v1/reservations/pending?${query}`;

  const params = {
    headers: {
      Authorization: `Bearer ${user.accessToken}`,
      Accept: 'application/json',
    },
  };

  const res = http.get(url, params);

  check(res, {
    '200 ok': r => r.status === 200,
    'not server error': r => r.status < 500,
    'response body exists': r => r.body && r.body.length > 0,
  });

  sleep(1);
}

export function handleSummary(data) {
  const out = __ENV.OUT || './results/applied-reservations-after-reservable-bulk-vu5-summary.json';

  //applied-reservations-after-asset-status-projection.json
  //applied-reservations-after-reservable-bulk.json


  return {
    [out]: JSON.stringify(data, null, 2),
    stdout: JSON.stringify({
      api: 'GET /api/v1/reservations/pending',
      purpose: 'applied reservations N+1 / bulk query comparison',
      cache: 'spring.cache.type=none',
      tokenCsvPath: TOKEN_CSV_PATH,
      page: PAGE,
      size: SIZE,
      reservationStatus: RESERVATION_STATUS || null,
      startDate: START_DATE || null,
      endDate: END_DATE || null,
      applicantName: APPLICANT_NAME || null,
      assetName: ASSET_NAME || null,
      categoryId: CATEGORY_ID || null,
      metrics: {
        http_reqs: data.metrics.http_reqs?.count,
        iterations: data.metrics.iterations?.count,
        checks_rate: data.metrics.checks?.rate,
        http_req_failed_rate: data.metrics.http_req_failed?.rate,
        http_req_duration_avg: data.metrics.http_req_duration?.avg,
        http_req_duration_min: data.metrics.http_req_duration?.min,
        http_req_duration_med: data.metrics.http_req_duration?.med,
        http_req_duration_p90: data.metrics.http_req_duration?.percentiles?.['90'],
        http_req_duration_p95: data.metrics.http_req_duration?.percentiles?.['95'],
        http_req_duration_p99: data.metrics.http_req_duration?.percentiles?.['99'],
        http_req_duration_max: data.metrics.http_req_duration?.max,
      },
    }, null, 2),
  };
}