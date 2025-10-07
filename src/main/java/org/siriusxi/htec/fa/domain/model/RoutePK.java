package org.siriusxi.htec.fa.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class RoutePK implements Serializable {
    
    @NonNull
    @Basic(optional = false)
    @Column(name = "SOURCE_AIRPORT", nullable = false)
    private String source;
    
    @NonNull
    @Basic(optional = false)
    @Column(name = "DESTINATION_AIRPORT",nullable = false)
    private String destination;
    
}
