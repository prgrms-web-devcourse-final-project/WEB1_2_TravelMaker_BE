package edu.example.wayfarer.dto.marker;

public record MarkerRequestDTO(
        String email,
        Long scheduleId,
        double lat,
        double lng
) {}

