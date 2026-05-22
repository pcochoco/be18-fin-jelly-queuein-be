import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const users = new SharedArray('users', function () {
  return open('../../qiin-be/output/user_tokens.csv')
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

//test option
export const options = {
  scenarios: {
    instant_reservation_load: {
      executor: 'constant-vus', //vu 수 고정 
      vus: 20, //virtual user 20 
      duration: '30s', //30초동안 요청
    },
  },
  thresholds: { //성능 기준 
    http_req_failed: ['rate<0.10'], //실패율 10% 이내  
    http_req_duration: ['p(95)<1500'], //95% 요청은 1.5초 안
  },
  //통계 출력 설정 
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
};

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

//startAt, endAt 생성 - reservation csv 따로 생성 x 
function buildFutureTimeSlot() {
  const base = new Date();

  // 현재 기준 7일 뒤부터 테스트
  base.setUTCDate(base.getUTCDate() + 7);

  // 0~20일 사이로 날짜 분산
  base.setUTCDate(base.getUTCDate() + randomInt(0, 20));

  // 09시~17시 시작, 1시간 예약
  const startHour = randomInt(9, 17);

  base.setUTCHours(startHour, 0, 0, 0);

  //예약 종료 시간 
  const startAt = new Date(base);
  
  //예약 시작 시간 
  const endAt = new Date(startAt);
  endAt.setUTCHours(startAt.getUTCHours() + 1); 

  return {
    startAt: startAt.toISOString(),
    endAt: endAt.toISOString(),
  };
}

//main test logic - vu가 반복 실행하는 함수 (main의 역할)
export default function () {
  //vu 1~20 -> user 배열을 순환해서 사용
  const user = users[(__VU - 1) % users.length]; //__VU : k6에서 자동으로 주는 값, 현재 실행 중 가상 유저의 번호 

  //asset id 
  const assetId = randomInt(1, 50);

  const slot = buildFutureTimeSlot();

  const url = `${BASE_URL}/api/v1/reservations/${assetId}/instant-confirm`;

  //create reservation dto 
  const payload = JSON.stringify({
    startAt: slot.startAt,
    endAt: slot.endAt,
    description: 'k6 instant reservation performance test',
    attendantIds: [],
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${user.accessToken}`,
    },
  };

  //http post 요청 실행 결과를 res로 저장
  const res = http.post(url, payload, params);

  check(res, {
    '201 created': r => r.status === 201, //생성 성공
    'not server error': r => r.status < 500, //500 : 실패
  });

  sleep(1); //각 vu 1초 쉰 뒤 반복 
}

export function handleSummary(data) {
  return {
    '../results/summary.json': JSON.stringify(data, null, 2), //결과를 json으로 저장 
  };
} 