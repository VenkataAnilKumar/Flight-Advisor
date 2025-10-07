package org.siriusxi.htec.fa.domain.model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

/**
 * @author Venkata Anil Kumar
 * @version 1.0
 **/
@Entity
@Table
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = {"airports","cities"})
public class Country implements Serializable {
    
    public Country(Integer id) {
        this.id = id;
    }
    
    @Serial
    private static final long serialVersionUID = -9057344199173138205L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false, length = 100)
    private String name;
    
    @OneToMany(cascade = ALL, mappedBy = "country", fetch = LAZY)
    private List<Airport> airports;
    
    @OneToMany(cascade = ALL, mappedBy = "country", fetch = LAZY)
    private List<City> cities;
}
