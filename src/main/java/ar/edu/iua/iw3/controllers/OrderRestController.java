package ar.edu.iua.iw3.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.OrderDetail;
import ar.edu.iua.iw3.controllers.dto.OrderRequestDTO;
import ar.edu.iua.iw3.controllers.dto.OrderResponseDTO;
import ar.edu.iua.iw3.controllers.dto.TruckDTO;
import ar.edu.iua.iw3.controllers.mappers.OrderMapper;
import ar.edu.iua.iw3.model.business.interfaces.ITruckBusiness;
import ar.edu.iua.iw3.model.business.interfaces.IDriverBusiness;
import ar.edu.iua.iw3.model.business.interfaces.ICustomerBusiness;
import ar.edu.iua.iw3.model.business.interfaces.IProductBusiness;
import ar.edu.iua.iw3.model.Reconciliation;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.IOrderBusiness;
import ar.edu.iua.iw3.util.IStandartResponseBusiness;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping(Constants.URL_ORDERS)
@Tag(name = "Orders", description = "API de gestión de órdenes de carga")
public class OrderRestController {
    
    @Autowired
    private IOrderBusiness orderBusiness;
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private ITruckBusiness truckBusiness;
	@Autowired
	private IDriverBusiness driverBusiness;
	@Autowired
	private ICustomerBusiness customerBusiness;
	@Autowired
	private IProductBusiness productBusiness;
    @Autowired
    private IStandartResponseBusiness response;

    @Operation(
        summary = "Listar todas las órdenes",
        description = "Obtiene una lista de todas las órdenes registradas en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        try {
			java.util.List<Order> orders = orderBusiness.list();
			java.util.List<OrderResponseDTO> dtos = orders.stream().map(o -> orderMapper.toDto(o)).toList();
			return new ResponseEntity<>(dtos, HttpStatus.OK);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	// Punto 1 -- Estado 1
	@Operation(
        summary = "Crear una nueva orden",
        description = "Crea una orden en estado 1 (pendiente de pesaje inicial). Registra la hora de recepción automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
        @ApiResponse(responseCode = "302", description = "La orden ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
	@PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> add(@RequestBody OrderRequestDTO ordenDto) {
		try {
            // map DTO to entity
            Order orden = orderMapper.toEntity(ordenDto);

            //   Product: buscar por nombre si existe, sino persistir
            if (ordenDto.getProduct() != null && ordenDto.getProduct().getProductName() != null) {
                try {
                    orden.setProduct(productBusiness.load(ordenDto.getProduct().getProductName()));
                } catch (NotFoundException nf) {
                    // No existe -> persistir (JPA genera id automáticamente)
                    orden.setProduct(productBusiness.add(orden.getProduct()));
                }
            }

            //   Truck: buscar por licensePlate si existe, sino persistir
            if (ordenDto.getTruck() != null) {
                // Asegurar back-references en cisternas ANTES de persistir
                if (orden.getTruck().getTruncker() != null) {
                    orden.getTruck().getTruncker().forEach(c -> c.setTruck(orden.getTruck()));
                }

                if (ordenDto.getTruck().getLicensePlate() != null) {
                    try {
                        orden.setTruck(truckBusiness.loadLicensePlate(ordenDto.getTruck().getLicensePlate()));
                    } catch (NotFoundException nf) {
                        // No existe -> persistir (JPA genera id)
                        orden.setTruck(truckBusiness.add(orden.getTruck()));
                    }
                } else {
                    orden.setTruck(truckBusiness.add(orden.getTruck()));
                }
            }

            //   Driver: buscar por DNI si existe, sino persistir
            if (ordenDto.getDriver() != null && ordenDto.getDriver().getDni() > 0) {
                try {
                    orden.setDriver(driverBusiness.load(ordenDto.getDriver().getDni()));
                } catch (NotFoundException nf) {
                    // No existe -> persistir (JPA genera id)
                    orden.setDriver(driverBusiness.add(orden.getDriver()));
                }
            }

            //   Customer: buscar por socialNumber si existe, sino persistir
            if (ordenDto.getCustomer() != null && ordenDto.getCustomer().getSocialNumber() > 0) {
                try {
                    orden.setCustomer(customerBusiness.load(ordenDto.getCustomer().getSocialNumber()));
                } catch (NotFoundException nf) {
                    // No existe -> persistir (JPA genera id)
                    orden.setCustomer(customerBusiness.add(orden.getCustomer()));
                }
            }

            // Setear valores iniciales
            orden.setState(1); // Pendiente de pesaje inicial
            orden.setTimeInitialReception(LocalDateTime.now());

            Order saved = orderBusiness.add(orden);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("location", Constants.URL_ORDERS + "/" + saved.getId());
            OrderResponseDTO body = orderMapper.toDto(saved);
            return new ResponseEntity<>(body, responseHeaders, HttpStatus.CREATED);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (FoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.FOUND, e, e.getMessage()), HttpStatus.FOUND);
        }
    }

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> load(@PathVariable int id) {
		try {
			Order orden = orderBusiness.load(id);
			return new ResponseEntity<>(orderMapper.toDto(orden), HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

    @DeleteMapping(value = "/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		try {
			orderBusiness.delete(id);
			return new ResponseEntity<String>(HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

	// Punto 2 -- Estado 2
	@Operation(
        summary = "Registrar pesaje inicial (tara)",
        description = "Registra el peso del camión vacío. Genera una contraseña de activación de 5 dígitos y cambia el estado a 2 (carga en progreso)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pesaje inicial registrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno o estado inválido")
    })
	@PostMapping(value = "/{id}/initial-weighing")
	public ResponseEntity<?> registerInitialWeighing(
        @Parameter(description = "ID de la orden") @PathVariable int id, 
        @Parameter(description = "Peso de la tara (kg)") @RequestBody Double tare) {
		try {
			Order o = orderBusiness.registerInitialWeighing(id, tare);
			return new ResponseEntity<>(o, HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

	// Punto 3 -- Estado == 2
	@Operation(
        summary = "Agregar detalle de carga en tiempo real",
        description = """
            Registra datos del caudalímetro durante la carga. Requiere contraseña de activación en el header.
            
            **Validaciones aplicadas:**
            - flow >= 0
            - massAccumulated >= 0 y >= valor anterior
            - 0 < density < 1
            - Orden debe estar en estado 2
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle registrado exitosamente"),
        @ApiResponse(responseCode = "401", description = "Contraseña de activación incorrecta o faltante"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error de validación o estado inválido")
    })
	@PostMapping(value = "/{id}/detail")
    public ResponseEntity<?> addDetail(
        @Parameter(description = "ID de la orden") @PathVariable int id, 
        @RequestBody OrderDetail detail, 
        @Parameter(description = "Contraseña de activación (5 dígitos)") @RequestHeader(name = "X-Activation-Password", required = false) Integer password) {
        try {
            Order o = orderBusiness.addDetail(id, detail, password);
            return new ResponseEntity<>(o, HttpStatus.OK);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(response.build(HttpStatus.UNAUTHORIZED, e, e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (BusinessException e) {
            return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

	// Punto 3 -- Estado == 2
	@Operation(
        summary = "Obtener preset de la orden para iniciar carga",
        description = "Retorna el valor preset (cantidad esperada a cargar) de la orden. Requiere contraseña de activación si la orden tiene una asignada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preset obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Contraseña de activación incorrecta o faltante"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error de validación o estado inválido")
    })
	@PostMapping(value = "/{id}/start")
	public ResponseEntity<?> startOrder(
        @Parameter(description = "ID de la orden") @PathVariable int id, 
        @Parameter(description = "Contraseña de activación (5 dígitos)") @RequestHeader(name = "X-Activation-Password", required = false) Integer password) {
		try {
			Double preset = orderBusiness.getPreset(id, password);
			java.util.Map<String, Object> body = new java.util.HashMap<>();
			body.put("orderId", id);
			body.put("preset", preset);
			return new ResponseEntity<>(body, HttpStatus.OK);
		} catch (NotFoundException e) {	
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		} catch (UnauthorizedException e) {
			return new ResponseEntity<>(response.build(HttpStatus.UNAUTHORIZED, e, e.getMessage()), HttpStatus.UNAUTHORIZED);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Punto 4 -- Estado 3
	@Operation(
        summary = "Cerrar orden de carga",
        description = "Marca la orden como cerrada para carga. Cambia el estado de 2 (carga en progreso) a 3 (pendiente de pesaje final)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden cerrada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error de validación o estado inválido")
    })
	@PostMapping(value = "/{id}/close")
	public ResponseEntity<?> closeOrder(@Parameter(description = "ID de la orden") @PathVariable int id) {
		try {
			Order o = orderBusiness.closeOrder(id);
			return new ResponseEntity<>(o, HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

	// Punto 5 -- Estado 4
	@Operation(
        summary = "Registrar pesaje final",
        description = "Registra el peso del camión lleno y calcula la conciliación automáticamente. Cambia el estado a 4 (finalizada). El pesaje final debe ser mayor que el inicial."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pesaje final registrado y conciliación calculada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error de validación o estado inválido")
    })
	@PostMapping(value = "/{id}/final-weighing")
	public ResponseEntity<?> finalizeWeighing(
        @Parameter(description = "ID de la orden") @PathVariable int id, 
        @Parameter(description = "Peso final (kg)") @RequestBody Double finalWeighing) {
		try {
			Reconciliation r = orderBusiness.finalizeWeighing(id, finalWeighing);
			return new ResponseEntity<>(r, HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

	// Punto 5 -- Estado == 4
	@Operation(
        summary = "Consultar conciliación de una orden finalizada",
        description = """
            Obtiene la conciliación calculada de una orden. Solo funciona si la orden está en estado 4 (finalizada).
            
            **Datos incluidos:**
            - Pesajes inicial y final
            - Producto cargado según caudalímetro
            - Neto por balanza
            - Diferencia entre balanza y caudalímetro
            - Promedios de temperatura, densidad y caudal
            
            Este endpoint es idempotente y no modifica datos.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conciliación obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "500", description = "La orden no está finalizada (estado != 4)")
    })
	@GetMapping(value = "/{id}/reconciliation")
	public ResponseEntity<?> getReconciliation(@Parameter(description = "ID de la orden") @PathVariable int id) {
		try {
			Reconciliation r = orderBusiness.getReconciliation(id);
			return new ResponseEntity<>(r, HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-admin")
    public String soloAdmin() {
        return "OK ADMIN";
    }

}