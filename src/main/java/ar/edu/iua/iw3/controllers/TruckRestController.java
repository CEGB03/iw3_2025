package ar.edu.iua.iw3.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import ar.edu.iua.iw3.model.Truck;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.interfaces.ITruckBusiness;
import ar.edu.iua.iw3.util.IStandartResponseBusiness;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(Constants.URL_TRUCKS)
@Tag(name = "Trucks", description = "Gestión de camiones")
public class TruckRestController {

    @Autowired
    private ITruckBusiness truckBusiness;

    @Autowired
    private IStandartResponseBusiness response;

    @Operation(summary = "Crear un nuevo camión", description = "Registra un nuevo camión en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Camión creado exitosamente"),
        @ApiResponse(responseCode = "302", description = "El camión ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> add(@RequestBody Truck truck) {
        try {
            Truck saved = truckBusiness.add(truck);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Listar camiones", description = "Obtiene el listado de todos los camiones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list() {
        try {
            java.util.List<Truck> trucks = truckBusiness.list();
            return new ResponseEntity<>(trucks, HttpStatus.OK);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
