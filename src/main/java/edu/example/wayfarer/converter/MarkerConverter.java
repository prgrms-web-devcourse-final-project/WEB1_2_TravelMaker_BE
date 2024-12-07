package edu.example.wayfarer.converter;

import edu.example.wayfarer.dto.marker.MarkerListDTO;
import edu.example.wayfarer.dto.marker.MarkerRequestDTO;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.entity.Marker;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.entity.enums.Color;

import java.util.List;

public class MarkerConverter {

    public static Marker toMarker(
            MarkerRequestDTO markerRequestDTO,
            Member member,
            Schedule schedule,
            Color color
    ) {
        return Marker.builder()
                .member(member)
                .schedule(schedule)
                .lat(markerRequestDTO.lat())
                .lng(markerRequestDTO.lng())
                .color(color)
                .confirm(false)
                .build();
    }

    public static MarkerResponseDTO toMarkerResponseDTO(
            Marker marker,
            Integer itemOrder
    ) {
        return new MarkerResponseDTO(
                marker.getMarkerId(),
                marker.getMember().getEmail(),
                marker.getMember().getProfileImage(),
                marker.getSchedule().getScheduleId(),
                marker.getLat(),
                marker.getLng(),
                marker.getColor().getHexCode(),
                marker.getConfirm(),
                itemOrder,
                marker.getCreatedAt(),
                marker.getUpdatedAt()
        );
    }

    public static MarkerListDTO toMarkerListDTO(
            Long scheduleId,
            List<MarkerResponseDTO> markerList
    ) {
        return new MarkerListDTO(
                scheduleId,
                markerList
        );
    }
}
