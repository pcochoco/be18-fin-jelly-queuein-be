package com.beyond.qiin.security.jwt;

/** Redis 장애로 access-token blacklist를 검증할 수 없음을 나타낸다. */
public class BlacklistCheckUnavailableException extends RuntimeException {

    public BlacklistCheckUnavailableException(final Throwable cause) {
        super("JWT blacklist 확인을 위한 Redis 조회에 실패했습니다.", cause);
    }
}
