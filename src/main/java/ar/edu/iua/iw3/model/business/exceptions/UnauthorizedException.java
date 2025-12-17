package ar.edu.iua.iw3.model.business.exceptions;

import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnauthorizedException extends Exception {

    @Builder
    public UnauthorizedException(String message, Throwable ex) {
        super(message, ex);
    }

    @Builder
    public UnauthorizedException(String message) {
        super(message);
    }

    @Builder
    public UnauthorizedException(Throwable ex) {
        super(ex);
    }
}
