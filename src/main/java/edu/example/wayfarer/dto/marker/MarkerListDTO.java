package edu.example.wayfarer.dto.marker;

import java.util.List;

public record MarkerListDTO(
        Long scheduleId,
        List<MarkerResponseDTO> markerList
) {}
