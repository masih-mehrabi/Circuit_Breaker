package circuit_breaker.resource;


import circuit_breaker.model.Droid;
import circuit_breaker.model.DroidFactory;
import circuit_breaker.model.DroidType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

@RestController
@RequestMapping(value = "/droid/", consumes = "application/json")
public class DroidFactoryResource {
    private final Function<DroidType, Droid> func = DroidFactory::produceDroid;
    
    private final CircuitBreaker<DroidType, Droid> circuitBreaker;
    
    public DroidFactoryResource(CircuitBreaker<DroidType, Droid> circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }
    @PostMapping("r2")
    public ResponseEntity<Droid> produceR2(@RequestBody DroidType droidType) {
        if (!droidType.equals(DroidType.R2)) {
            throw new BadRequestException();
        }
        return circuitBreaker.protectedCall(droidType, func);
    }

    @PostMapping("3po")
    public ResponseEntity<Droid> produce3PO(@RequestBody DroidType droidType) {
        if (!droidType.equals(DroidType.THREE_PO)) {
            throw new BadRequestException();
        }
        return circuitBreaker.protectedCall(droidType, func);
    
    }

    @PostMapping("astromech")
    public ResponseEntity<Droid> produceAstromech(@RequestBody DroidType droidType) {
        if (!droidType.equals(DroidType.ASTROMECH)) {
            throw new BadRequestException();
        }
        return circuitBreaker.protectedCall(droidType, func);
    }


}
