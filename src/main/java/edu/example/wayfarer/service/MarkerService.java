package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.marker.MarkerListDTO;
import edu.example.wayfarer.dto.marker.MarkerRequestDTO;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.dto.marker.MarkerUpdateDTO;

import java.util.List;

public interface MarkerService {

    MarkerResponseDTO create(MarkerRequestDTO markerRequestDTO);

    MarkerResponseDTO read(Long markerId);

    List<MarkerResponseDTO> getListBySchedule(Long scheduleId);

    List<MarkerListDTO> getListByRoom(String roomId);

    MarkerResponseDTO update(MarkerUpdateDTO markerUpdateDTO);

    void delete(Long markerId);

}
