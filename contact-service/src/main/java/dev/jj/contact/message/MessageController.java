package dev.jj.contact.message;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
class MessageController {

    private final MessageService service;

    MessageController(MessageService service) {
        this.service = service;
    }

    /**
     * web-public inoltra qui il form. L'IP reale del visitatore arriva nell'header X-Client-Ip
     * (il servizio non è raggiungibile da Internet: solo web-public e gateway lo chiamano).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MessageResponse create(@Valid @RequestBody MessageRequest request,
                           @RequestHeader(value = "X-Client-Ip", required = false) String clientIp) {
        return service.create(request, clientIp);
    }
}
