package com.flybook.controller;

import com.flybook.exception.FlybookException;
import com.flybook.model.dto.request.AuthDTORequest;
import com.flybook.model.dto.request.ClientDTORequest;
import com.flybook.model.dto.response.ClientDTOResponse;
import com.flybook.service.ClientService;
import com.flybook.service.impl.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/client")
@RequiredArgsConstructor
public class ClientController {

    private final JwtService jwtService;
    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthDTORequest authRequest) {
        log.info("Fetching token for user {}", authRequest);
        Authentication authentication = null;

        try {
             authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.info("Bad credentials: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");
        }

        log.info("Authentication {}", authentication);

        if (authentication.isAuthenticated()) {
            return ResponseEntity.ok(jwtService.generateToken(authRequest.getEmail()));
        } else {
            throw new UsernameNotFoundException("Invalid user request");
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ClientDTOResponse> getClient(@PathVariable Long id) throws FlybookException {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    @PostMapping("/signup")
    public ResponseEntity<ClientDTOResponse> addClient(@Valid @RequestBody ClientDTORequest clientDTORequest) throws FlybookException {
        log.info("Adding client: {}", clientDTORequest.toString());
        return ResponseEntity.ok(clientService.addClient(clientDTORequest));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<ClientDTOResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDTORequest clientDTORequest) throws FlybookException {
        log.info("Updating client: {}", clientDTORequest.toString());
        return ResponseEntity.ok(clientService.updateClient(id, clientDTORequest));
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.info("Deleting client {}", id);
        try {
            clientService.deleteClient(id);
            return ResponseEntity.accepted().build();
        } catch (FlybookException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
