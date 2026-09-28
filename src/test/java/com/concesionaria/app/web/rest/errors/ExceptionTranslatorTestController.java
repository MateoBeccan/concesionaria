package com.concesionaria.app.web.rest.errors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.sql.SQLIntegrityConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exception-translator-test")
public class ExceptionTranslatorTestController {

    @GetMapping("/concurrency-failure")
    public void concurrencyFailure() {
        throw new ConcurrencyFailureException("test concurrency failure");
    }

    @GetMapping("/comprobante-active-venta-conflict")
    public void comprobanteActiveVentaConflict() {
        throw hibernateConstraintViolation("ux_comprobante_venta_emitida_tipo");
    }

    @GetMapping("/comprobante-active-pago-conflict")
    public void comprobanteActivePagoConflict() {
        throw new DataIntegrityViolationException("wrapped data integrity violation", hibernateConstraintViolation("ux_comprobante_pago_emitido_tipo"));
    }

    @GetMapping("/comprobante-numero-conflict")
    public void comprobanteNumeroConflict() {
        throw hibernateConstraintViolation("ux_comprobante_numero");
    }

    @GetMapping("/generic-data-integrity-violation")
    public void genericDataIntegrityViolation() {
        throw new DataIntegrityViolationException(
            "generic data integrity violation",
            new SQLIntegrityConstraintViolationException("Duplicate entry '1' for key 'otra_tabla.ux_generica'", "23000", 1062)
        );
    }

    @PostMapping("/method-argument")
    public void methodArgument(@Valid @RequestBody TestDTO testDTO) {
        // empty method
    }

    @GetMapping("/missing-servlet-request-part")
    public void missingServletRequestPartException(@RequestPart("part") String part) {
        // empty method
    }

    @GetMapping("/missing-servlet-request-parameter")
    public void missingServletRequestParameterException(@RequestParam("param") String param) {
        // empty method
    }

    @GetMapping("/access-denied")
    public void accessdenied() {
        throw new AccessDeniedException("test access denied!");
    }

    @GetMapping("/unauthorized")
    public void unauthorized() {
        throw new BadCredentialsException("test authentication failed!");
    }

    @GetMapping("/response-status")
    public void exceptionWithResponseStatus() {
        throw new TestResponseStatusException();
    }

    @GetMapping("/internal-server-error")
    public void internalServerError() {
        throw new RuntimeException();
    }

    public static class TestDTO {

        @NotNull
        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "test response status")
    @SuppressWarnings("serial")
    public static class TestResponseStatusException extends RuntimeException {}

    private static ConstraintViolationException hibernateConstraintViolation(String constraintName) {
        SQLIntegrityConstraintViolationException sqlException = new SQLIntegrityConstraintViolationException(
            "Duplicate entry '1-2' for key 'comprobante." + constraintName + "'",
            "23000",
            1062
        );
        return new ConstraintViolationException(
            "could not execute statement [insert into comprobante (...)] [Duplicate entry '1-2' for key 'comprobante." + constraintName + "']",
            sqlException,
            "insert into comprobante (...) values (...)",
            constraintName
        );
    }
}
