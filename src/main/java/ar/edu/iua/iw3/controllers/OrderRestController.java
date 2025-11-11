package ar.edu.iua.iw3.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping(Constants.URL_ORDERS)
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
	@PostMapping(value = "/{id}/initial-weighing")
	public ResponseEntity<?> registerInitialWeighing(@PathVariable int id, @RequestBody Double tare) {
		try {
			Order o = orderBusiness.registerInitialWeighing(id, tare);
			return new ResponseEntity<>(o, HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

	// Punto 3 -- Estado X
	@PostMapping(value = "/{id}/detail")
    public ResponseEntity<?> addDetail(@PathVariable int id, @RequestBody OrderDetail detail, @RequestHeader(name = "X-Activation-Password", required = false) Integer password) {
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

	// Punto 3 -- Estado X
	@PostMapping(value = "/{id}/start")
	public ResponseEntity<?> startOrder(@PathVariable int id, @RequestHeader(name = "X-Activation-Password", required = false) Integer password) {
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
	@PostMapping(value = "/{id}/close")
	public ResponseEntity<?> closeOrder(@PathVariable int id) {
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
	@PostMapping(value = "/{id}/final-weighing")
	public ResponseEntity<?> finalizeWeighing(@PathVariable int id, @RequestBody Double finalWeighing) {
		try {
			Reconciliation r = orderBusiness.finalizeWeighing(id, finalWeighing);
			return new ResponseEntity<>(r, HttpStatus.OK);
		} catch (BusinessException e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException e) {
			return new ResponseEntity<>(response.build(HttpStatus.NOT_FOUND, e, e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}
}
