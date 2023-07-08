package ru.chipization.achip.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.dto.request.RegisterRequest;
import ru.chipization.achip.exception.BadRequestException;
import ru.chipization.achip.exception.AlreadyExistException;
import ru.chipization.achip.service.RegisterService;

@RestController
@CrossOrigin("*")
@RequestMapping("/registration")
@AllArgsConstructor
public class RegistrationController {
    @Autowired
    private RegisterService registerService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.status(201).body(registerService.register(request));
        } catch (AlreadyExistException eae) {
            return ResponseEntity.status(409).body(eae.getMessage());
        } catch (BadRequestException bre) {
            return ResponseEntity.status(400).body(bre.getMessage());
        }
    }
}
