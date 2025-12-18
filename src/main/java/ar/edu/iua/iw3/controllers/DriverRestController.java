package ar.edu.iua.iw3.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import ar.edu.iua.iw3.model.Driver;
import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.Truck;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.IDriverBusiness;
import ar.edu.iua.iw3.model.persistence.OrderRepository;
import ar.edu.iua.iw3.util.IStandartResponseBusiness;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.URL_DRIVERS)
@Tag(name = "Drivers", description = "Gestión de conductores")
public class DriverRestController {

    @Autowired
    private IDriverBusiness driverBusiness;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private IStandartResponseBusiness response;

    @Operation(summary = "Crear un nuevo conductor", description = "Registra un nuevo conductor en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Conductor creado exitosamente"),
        @ApiResponse(responseCode = "302", description = "El conductor ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> add(@RequestBody Driver driver) {
        try {
            Driver saved = driverBusiness.add(driver);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Listar conductores", description = "Obtiene el listado de todos los conductores")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list() {
        try {
            java.util.List<Driver> drivers = driverBusiness.list();
            return new ResponseEntity<>(drivers, HttpStatus.OK);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Obtener camiones de un conductor", description = "Lista los camiones asociados a un conductor (via órdenes)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido"),
        @ApiResponse(responseCode = "404", description = "Conductor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "/{id}/trucks", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTrucks(@PathVariable String id) {
        try {
            Driver driver = driverBusiness.load(id);
            List<Order> orders = orderRepository.findByDriver(driver);
            List<Truck> trucks = orders.stream()
                .map(Order::getTruck)
                .filter(t -> t != null)
                .distinct()
                .collect(Collectors.toList());
            return new ResponseEntity<>(trucks, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
