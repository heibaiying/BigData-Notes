package com.heibaiying.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class USPopulation {

    private String state;
    private String city;
    private long population;
}
