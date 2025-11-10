package ar.edu.iua.iw3.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Reconciliation {
    private Double initialWeighing;
    private Double finalWeighing;
    private Double productLoaded;
    private Double netByScale;
    private Double differenceScaleFlow;
    private Double avgTemperature;
    private Double avgDensity;
    private Double avgFlow;
}
