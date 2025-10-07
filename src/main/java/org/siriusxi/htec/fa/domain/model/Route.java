package org.siriusxi.htec.fa.domain.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import static jakarta.persistence.FetchType.*;

/**
 * @author Venkata Anil Kumar
 * @version 1.0
 **/
@Entity
@Table
@Data
@NoArgsConstructor
public class Route implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 8818845493466966108L;
    
    @EmbeddedId
    protected RoutePK routePK;
    
    @Column(name = "AIRLINE_CODE", length = 3)
    private String airlineCode;
    
    @Column(name = "AIRLINE_ID")
    private Integer airlineId;
    
    @Column(name = "CODE_SHARE")
    private Boolean codeShare;
    
    private Integer stops;
    
    @Column(length = 100)
    private String equipment;
    
    @Max(value = 99999)
    @Min(value = 5)
    @Column(precision = 6, scale = 3)
    private BigDecimal price;
    
    @JoinColumn(name = "DESTINATION_AIRPORT_ID", referencedColumnName = "AIRPORT_ID")
    @ManyToOne(fetch = LAZY)
    private Airport destinationAirport;
    
    @JoinColumn(name = "SOURCE_AIRPORT_ID", referencedColumnName = "AIRPORT_ID")
    @ManyToOne(fetch = LAZY)
    private Airport sourceAirport;
}
