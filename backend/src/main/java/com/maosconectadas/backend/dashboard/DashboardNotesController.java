package com.maosconectadas.backend.dashboard;

import com.maosconectadas.backend.auth.UserAccount;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/notes")
public class DashboardNotesController {
    private final DashboardNotesService service;

    public DashboardNotesController(DashboardNotesService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardNotesResponse findNotes(@AuthenticationPrincipal UserAccount owner) {
        return service.findNotes(owner);
    }

    @PutMapping
    public DashboardNotesResponse saveNotes(
        @Valid @RequestBody DashboardNotesRequest request,
        @AuthenticationPrincipal UserAccount owner
    ) {
        return service.saveNotes(request, owner);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotes(@AuthenticationPrincipal UserAccount owner) {
        service.deleteNotes(owner);
    }
}
