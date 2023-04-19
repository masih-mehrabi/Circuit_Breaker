package circuit_breaker.resource;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "DroidType doesn't match for endpoint")
public class BadRequestException extends RuntimeException {

}
