package pl.edu.ur.coopspace_backend.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Maps generic server errors to a simple HTTP response body.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    /**
     * Generic error mapping endpoint used by the servlet container.
     *
     * @param request the current HTTP servlet request
     * @return a simple text response containing the HTTP status and reason
     */
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            return ResponseEntity.status(statusCode).body(statusCode + " - " + HttpStatus.valueOf(statusCode).getReasonPhrase());
        }
        return ResponseEntity.status(500).body("500 - Internal Server Error");
    }
}
