package com.ryanair.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class Flight {
    private String departureAirport;
    private String arrivalAirport;
    private Date departureDateTime;
    private Date arrivalDateTime;
}
