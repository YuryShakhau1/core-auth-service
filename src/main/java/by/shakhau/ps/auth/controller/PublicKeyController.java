package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.service.impl.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/public-key")
@AllArgsConstructor
public class PublicKeyController {

    private final JwtService jwtService;

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPublicKey() {
        return ResponseEntity.ok(jwtService.getPublicKeyAsString());
    }
}
