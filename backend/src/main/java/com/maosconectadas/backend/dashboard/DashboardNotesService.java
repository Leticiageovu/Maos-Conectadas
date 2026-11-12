package com.maosconectadas.backend.dashboard;

import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.auth.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DashboardNotesService {
    private final UserAccountRepository userRepository;

    public DashboardNotesService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardNotesResponse findNotes(UserAccount owner) {
        UserAccount account = findAccount(owner);
        return new DashboardNotesResponse(notesOrEmpty(account));
    }

    public DashboardNotesResponse saveNotes(DashboardNotesRequest request, UserAccount owner) {
        UserAccount account = findAccount(owner);
        account.setDashboardNotes(request.notes());
        userRepository.save(account);
        return new DashboardNotesResponse(notesOrEmpty(account));
    }

    public void deleteNotes(UserAccount owner) {
        UserAccount account = findAccount(owner);
        account.setDashboardNotes(null);
        userRepository.save(account);
    }

    private UserAccount findAccount(UserAccount owner) {
        return userRepository.findById(owner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    private String notesOrEmpty(UserAccount account) {
        return account.getDashboardNotes() == null ? "" : account.getDashboardNotes();
    }
}
