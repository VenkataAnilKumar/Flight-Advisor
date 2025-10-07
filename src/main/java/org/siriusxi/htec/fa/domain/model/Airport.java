package org.siriusxi.htec.fa.domain.model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static jakarta.persistence.FetchType.*;

/**
 * @author Venkata Anil Kumar
 * @version 1.0
 **/
@Entity
@Table(catalog = "FLIGHTDB", schema = "PUBLIC")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = {"destinationRoutes", "sourceRoutes"})
public class Airport implements Serializable {
    
    public enum Dst {
        E, A, S, O, Z, N, U
    }
    
    public Airport(Integer airportId) {
        this.airportId = airportId;
    }
    
    @Serial
    private static final long serialVersionUID = 6913599167936010779L;
    
    @Id
    @Basic(optional = false)
    @Column(name = "AIRPORT_ID", nullable = false)
    private Integer airportId;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false)
    private String name;
    
    @NonNull
    @Basic(optional = false)
    @Column(name = "CITY", nullable = false, length = 100)
    private String cityName;
    
    @NonNull
    @Basic(optional = false)
    @Column(name = "COUNTRY", nullable = false, length = 100)
    private String countryName;
    
    @Column(length = 3)
    private String iata;
    
    @Column(length = 4)
    private String icao;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal latitude;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal longitude;
    
    @NonNull
    private Integer altitude;
    
    @NonNull
    private Float timezone;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false)
    private Dst dst;
    
    @NonNull
    @Column(length = 50)
    private String tz;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false, length = 50)
    private String type;
    
    @NonNull
    @Basic(optional = false)
    @Column(name = "DATA_SOURCE", nullable = false)
    private String dataSource;
    
    @OneToMany(mappedBy = "destinationAirport", fetch = LAZY)
    private List<Route> destinationRoutes;
    
    @OneToMany(mappedBy = "sourceAirport", fetch = LAZY)
    private List<Route> sourceRoutes;
    
    @JoinColumn(name = "CITY_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false, fetch = LAZY)
    private City city;
    
    @JoinColumn(name = "COUNTRY_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false, fetch = LAZY)
    private Country country;
}
