package org.siriusxi.htec.fa.domain.model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;

/**
 * @author Venkata Anil Kumar
 * @version 1.0
 **/
@Entity
@Table
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = {"airports","country", "comments"})
public class City implements Serializable {
    
    public City(Integer id) {
        this.id = id;
    }
    
    public City(String name, String description, Country country) {
        this(name, country);
        this.description = description;
    }
    
    @Serial
    private static final long serialVersionUID = 1322727266984495327L;
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    
    @NonNull
    @Basic(optional = false)
    @Column(nullable = false, length = 100)
    private String name;
    
    @Basic(optional = false)
    @Column(length = 100)
    private String description;
    
    @OneToMany(cascade = ALL, mappedBy = "city", fetch = LAZY)
    private List<Comment> comments;
    
    @OneToMany(cascade = ALL, mappedBy = "city", fetch = LAZY)
    private List<Airport> airports;
    
    @NonNull
    @JoinColumn(name = "COUNTRY_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false, fetch = LAZY)
    private Country country;
}
