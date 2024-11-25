package chat.messageserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public ResourceNotFoundException() {
        super();
    }

    /**
     * 메시지를 받는 생성자
     *
     * @param message 예외 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인(Throwable)을 받는 생성자
     *
     * @param message 예외 메시지
     * @param cause   원인
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인(Throwable)을 받는 생성자
     *
     * @param cause 원인
     */
    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * 메시지, 원인, 예외 생성 가능 여부 등을 받는 생성자
     *
     * @param message            예외 메시지
     * @param cause              원인
     * @param enableSuppression  억제 가능 여부
     * @param writableStackTrace 스택 트레이스 작성 가능 여부
     */
    protected ResourceNotFoundException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
