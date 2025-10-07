package org.siriusxi.htec.fa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.siriusxi.htec.fa.domain.dto.response.CountryView;
import org.siriusxi.htec.fa.domain.mapper.CountryMapper;
import org.siriusxi.htec.fa.domain.model.Role;
import org.siriusxi.htec.fa.repository.CountryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * City controller used to handle cities API functions.
 *
 * @author Venkata Anil Kumar
 * @version 1.0
 * <p>
 * FIXME: Swagger documentation
 */
@Slf4j
@Tag(name = "Country Management")
@RolesAllowed(Role.ADMIN)
@RestController
@RequestMapping("v1/countries")
public class CountryController {
    
    private final CountryRepository countryRepository;
    private final CountryMapper mapper;
    
    public CountryController(CountryRepository countryRepository, CountryMapper mapper) {
        this.countryRepository = countryRepository;
        this.mapper = mapper;
    }
    
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    @GetMapping
    public Set<CountryView> getAllCountries() {
        return mapper
                   .toViews(countryRepository
                                .findAllByNameIgnoreCaseIsLike("%%"));
    }
    
    @Operation(security = {@SecurityRequirement(name = "bearer-key")})
    @GetMapping("search")
    public Set<CountryView> searchCountries(@RequestParam @NotBlank String name) {
        return mapper
                   .toViews(countryRepository
                                .findAllByNameIgnoreCaseIsLike("%" + name + "%"));
    }
    
}
