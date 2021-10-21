package com.ryanair.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class FlightData {
    private String number;
    private String departureTime;
    private String arrivalTime;
}
