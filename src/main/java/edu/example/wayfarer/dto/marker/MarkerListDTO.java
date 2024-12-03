package edu.example.wayfarer.dto.marker;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public record MarkerListDTO(
        Long scheduleId,
        List<MarkerResponseDTO> markerList
) {}
