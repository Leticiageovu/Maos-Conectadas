package com.maosconectadas.backend.dashboard;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DashboardNotesRequest(
    @NotNull @Size(max = 5000) String notes
) {}
