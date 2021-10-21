package com.ryanair.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class Day {
    private String day;
    private List<FlightData> flights;

}
