package com.robotech.robotech_backend.dto;

import java.util.List;

public record AdminUsuariosPageResponse(
        List<UsuarioDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
