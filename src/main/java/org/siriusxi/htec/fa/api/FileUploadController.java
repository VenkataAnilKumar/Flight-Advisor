package org.siriusxi.htec.fa.api;

import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.siriusxi.htec.fa.domain.dto.upload.airport.AirportDto;
import org.siriusxi.htec.fa.domain.dto.upload.airport.verifer.AirportBeanVerifier;
import org.siriusxi.htec.fa.domain.dto.upload.route.RouteDto;
import org.siriusxi.htec.fa.domain.dto.upload.route.verifer.RouteBeanVerifier;
import org.siriusxi.htec.fa.domain.mapper.AirportMapper;
import org.siriusxi.htec.fa.domain.mapper.RouteMapper;
import org.siriusxi.htec.fa.domain.model.Country;
import org.siriusxi.htec.fa.domain.model.Role;
import org.siriusxi.htec.fa.repository.AirportRepository;
import org.siriusxi.htec.fa.repository.CityRepository;
import org.siriusxi.htec.fa.repository.CountryRepository;
import org.siriusxi.htec.fa.repository.RouteRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.security.RolesAllowed;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

/**
 * Files Upload controller used to handle all data feeding the system like airports and routes.
 *
 * @author Venkata Anil Kumar
 * @version 1.0
 *
 * TODO: Improve performance of the file processing.
 */
@Slf4j
@Tag(name = "Files Upload Management",
    description = """
        A set of API calls, used to feed the system with data
        files like airports and routes.""")
@RolesAllowed(Role.ADMIN)
@RestController
@RequestMapping(value = "v1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public class FileUploadController {
    
    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final RouteMapper routeMapper;
    private final AirportMapper airportMapper;
    
    public FileUploadController(RouteRepository routeRepository, RouteMapper routeMapper,
                                AirportRepository airportRepository, AirportMapper airportMapper,
                                CityRepository cityRepository, CountryRepository countryRepository) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
        this.routeMapper = routeMapper;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.airportMapper = airportMapper;
    }
    
    @Operation(summary = "Upload file that contains flights airports.",
        security = {@SecurityRequirement(name = "bearer-key")},
        responses = {
            @ApiResponse(responseCode = "200",
                description = "OK; File is uploaded and parsed successfully.",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400",
                description = "Bad Request; File is not valid.",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "417",
                description = "Expectation Failed; Error while parsing or processing the file.",
                content = @Content(schema = @Schema(implementation = String.class)))})
    @PostMapping("airports")
    public ResponseEntity<String> uploadAirports(@Parameter(name = "file", required = true,
        description = "Select airport file to upload.") @RequestPart("file") MultipartFile file) {
        
        if (file.isEmpty())
            return new ResponseEntity<>("Please upload a valid file.", BAD_REQUEST);
        else {
            try {
                // convert `CsvToBean` object to list of airports
                List<AirportDto> airports = parseCsvContent(file.getInputStream(),
                    AirportDto.class, new AirportBeanVerifier());
                    
                /*
                 Save All Airports to database after:
                    1. Add countries and cities to database.
                    2. Add new added city and country ids to dto.
                    3. Map Airport Dto to Airport model.
                    4. Return all airports as list to be saved to DB.
                 */
                airportRepository.saveAll(
                    airports
                        .stream()// Save countries and cities to database
                        .peek(airportDto -> {
                            
                            Country country = countryRepository
                                .findOrSaveBy(airportDto.getCountry());
                            airportDto.setCountryId(country.getId());
                            
                            airportDto.setCityId(cityRepository.findOrSaveBy(country,
                                airportDto.getCity()).getId());
                        })
                        // converting to
                        .map(airportMapper::toModel)
                        // Then collect them to list
                        .collect(Collectors.toList()));
                
            } catch (Exception ex) {
                log.error("Error while reading airports file.", ex);
                return new ResponseEntity<>("Error while reading or processing airport file.",
                    EXPECTATION_FAILED);
            }
        }
        
        return new ResponseEntity<>("File uploaded successfully.", OK);
    }
    
    @Operation(summary = "Upload file that contains flights routes.",
        security = {@SecurityRequirement(name = "bearer-key")},
        responses = {
            @ApiResponse(responseCode = "200",
                description = "OK; File is uploaded and parsed successfully.",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400",
                description = "Bad Request; File is not valid.",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "417",
                description = "Expectation Failed; Error while parsing or processing the file.",
                content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "424",
                description = "Failed Dependency; System doesn't have any airports.",
                content = @Content(schema = @Schema(implementation = String.class)))})
    @PostMapping("routes")
    public ResponseEntity<String> uploadRoutes(@Parameter(name = "file", required = true,
        description = "Select flights routes file to upload.")
                                               @RequestPart("file") MultipartFile file) {
        
        if (file.isEmpty())
            return new ResponseEntity<>("Please upload a valid file.", BAD_REQUEST);
        else if (airportRepository.count() == 0) {
            return new ResponseEntity<>("""
                "Can't upload flight routes, system doesn't has Airports defined.
                Please upload airport file, to feed the system with airports."
                """, FAILED_DEPENDENCY);
        } else {
            
            // parse CSV file to create a list of `RouteDto` objects
            try {
                
                // convert `CsvToBean` object to list of routeDto
                List<RouteDto> routes = parseCsvContent(file.getInputStream(),
                    RouteDto.class, new RouteBeanVerifier());
                
                /*
                 1. Convert to Route model.
                 2. Save routes in DB.
                 */
                routeRepository
                    .saveAll(routes
                        .stream()
                        // filter routes doesn't exists
                        .filter(dto ->
                            airportRepository.findById(dto.getSrcAirportId()).isPresent() &&
                                airportRepository.findById(dto.getDestAirportId()).isPresent())
                        // converting to
                        .map(routeMapper::toModel)
                        // Then collect them to list
                        .collect(Collectors.toList()));
                
            } catch (Exception ex) {
                log.error("Error while reading routes file.", ex);
                return new ResponseEntity<>("Error while reading or processing routes file.",
                    EXPECTATION_FAILED);
            }
        }
        
        return new ResponseEntity<>("File uploaded successfully.", OK);
    }
    
    /**
     * This method is used to convert CSV content from any input stream to a bean list of type T.
     *
     * @param content      csv content from input stream.
     * @param clazz        the bean type class.
     * @param beanVerifier to verify and exclude unwanted data for a specific bean type.
     * @param <T>          is the bean type.
     * @return List of bean type T.
     *
     * @throws Exception If any data is invalid or problem parsing the content of the input stream.
     *
     * @since v1.0
     */
    private <T> List<T> parseCsvContent(InputStream content, Class<T> clazz,
                                        BeanVerifier<T> beanVerifier) throws Exception {
        
        List<T> dtoList;
        // parse CSV file to create a list of `TypeDto` objects
        try (Reader reader = new BufferedReader(new InputStreamReader(content))) {
            // create csv bean reader
            CsvToBean<T> csvToBeans = new CsvToBeanBuilder<T>(reader)
                .withType(clazz)
                .withVerifier(beanVerifier)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
            
            // convert `CsvToBean` object to a dto list
            dtoList = csvToBeans.parse();
        }
        return dtoList;
    }
}
