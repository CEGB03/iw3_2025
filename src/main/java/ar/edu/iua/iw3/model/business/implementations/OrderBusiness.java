package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.OrderDetail;
import ar.edu.iua.iw3.model.Reconciliation;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.IOrderBusiness;
import ar.edu.iua.iw3.model.business.interfaces.ITemperatureAlarmBusiness;
import ar.edu.iua.iw3.model.persistence.OrderRepository;
import ar.edu.iua.iw3.model.persistence.OrderDetailRepository;
import ar.edu.iua.iw3.model.persistence.OrderStateLogRepository;
import ar.edu.iua.iw3.model.OrderStateLog;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderBusiness implements IOrderBusiness {
    
    @Autowired
    private OrderRepository orderDAO;

    @Autowired
    private OrderDetailRepository orderDetailDAO;

    @Autowired
    private OrderStateLogRepository stateLogDAO;

    @Autowired
    private ITemperatureAlarmBusiness temperatureAlarmBusiness;

    private void saveStateLog(Order order, int fromState, int toState, String actor, String notes) {
        try {
            if (stateLogDAO == null) {
                return;
            }
            OrderStateLog log = new OrderStateLog(order, fromState, toState, LocalDateTime.now(), actor, notes);
            stateLogDAO.save(log);
        } catch (Exception e) {
            log.warn("Failed to save OrderStateLog: " + e.getMessage());
        }
    }

    @Override
    public Double getPreset(int id, Integer password) throws NotFoundException, BusinessException, UnauthorizedException {
        Order o = load(id);
        if (o.getState() != 2) {
            throw BusinessException.builder().message("Orden no en estado 2").build();
        }
        if (o.getActivationPassword() != null) {
            if (password == null || !o.getActivationPassword().equals(password)) {
                throw UnauthorizedException.builder().message("Password incorrecta o faltante").build();
            }
        }
        return o.getPreset();
    }

    @Override
    public List<Order> list() throws BusinessException{
        try {
            return orderDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Order load(int id) throws NotFoundException, BusinessException {
        Optional<Order> r;
        try {
            r = orderDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if(r.isEmpty()){
            throw NotFoundException.builder().message("No se encuentra la orden con id " + id).build();
        }
        return r.get();
    }

    @Override
    public Order add(Order orden) throws FoundException, BusinessException {
        try {
            load(orden.getId());
            throw FoundException.builder().message("La orden con id " + orden.getId() + " ya existe").build();
        } catch (NotFoundException e) {
            
        }
    
        try {
            // Ensure new orders are created in the initial state and record reception time
            if (orden.getState() == 0) {
                orden.setState(1); // Estado 1: Pendiente de pesaje inicial
            }
            if (orden.getTimeInitialReception() == null) {
                orden.setTimeInitialReception(LocalDateTime.now());
            }
            Order saved = orderDAO.save(orden);
            // audit: new order created (0 -> 1)
            try {
                saveStateLog(saved, 0, saved.getState(), "system", "Order created");
            } catch (Exception ex) {
                log.warn("Could not save state log: " + ex.getMessage());
            }
            return saved;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public void delete(int id) throws NotFoundException, BusinessException {
        load(id);
        try {
            orderDAO.deleteById(id);

    } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Order registerInitialWeighing(int id, Double tare) throws NotFoundException, BusinessException {
        Order o = load(id);
        try {
            if (o.getState() != 1) {
                throw BusinessException.builder().message("Orden no en estado 1").build();
            }
            if (tare == null || tare <= 0) {
                throw BusinessException.builder().message("Pesaje inicial debe ser mayor que cero").build();
            }
            o.setInitialWeighing(tare);
            o.setTimeInitialWeighing(LocalDateTime.now());
            // generate 5-digit password
            int pass = new Random().nextInt(90000) + 10000;
            o.setActivationPassword(pass);
            int from = o.getState();
            o.setState(2);
            Order saved = orderDAO.save(o);
            try {
                saveStateLog(saved, from, 2, "TMS", "Initial weighing registered");
            } catch (Exception ex) {
                log.warn("Could not save state log: " + ex.getMessage());
            }
            return saved;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Order addDetail(int id, OrderDetail detail, Integer password) 
            throws NotFoundException, BusinessException, UnauthorizedException {
        
        Order order = load(id);
        
        // Validar estado: debe estar en estado 2 (pesaje inicial registrado, carga en progreso)
        if (order.getState() != 2) {
            throw BusinessException.builder()
                    .message("Orden no está en estado de carga (estado 2)")
                    .build();
        }
        
        //  Validar password si la orden tiene una contraseña de activación
        if (order.getActivationPassword() != null) {
            if (password == null || !order.getActivationPassword().equals(password)) {
                log.warn("Password incorrecta o faltante para orden {}. Esperado: {}, Recibido: {}", 
                         id, order.getActivationPassword(), password);
                throw UnauthorizedException.builder()
                        .message("Password incorrecta o faltante")
                        .build();
            }
        }
        
        // VALIDACIONES: Si alguna falla, NO GUARDAR NADA
        
        // 1. Validar flow >= 0
        if (detail.getFlow() == null || detail.getFlow() < 0) {
            log.warn("Descartado detalle para orden {}: flow={} debe ser >= 0", id, detail.getFlow());
            throw BusinessException.builder()
                    .message("El caudal (flow) debe ser mayor o igual a 0")
                    .build();
        }
        
        // 2. Validar mass_accumulated >= 0
        if (detail.getMassAccumulated() == null || detail.getMassAccumulated() < 0) {
            log.warn("Descartado detalle para orden {}: massAccumulated={} debe ser >= 0", id, detail.getMassAccumulated());
            throw BusinessException.builder()
                    .message(String.format("La masa acumulada debe ser mayor o igual a 0 (recibido: %s)", detail.getMassAccumulated()))
                    .build();
        }
        
        // 3. Validar mass_accumulated >= mass_accumulated del registro anterior
        List<OrderDetail> previousDetails = orderDetailDAO.findByOrderId(id);
        if (!previousDetails.isEmpty()) {
            // Obtener el último detalle registrado
            OrderDetail lastDetail = previousDetails.stream()
                    .max((d1, d2) -> d1.getTimeStamp().compareTo(d2.getTimeStamp()))
                    .orElse(null);
            
            if (lastDetail != null && lastDetail.getMassAccumulated() != null) {
                if (detail.getMassAccumulated() < lastDetail.getMassAccumulated()) {
                    log.warn("Descartado detalle para orden {}: massAccumulated={} debe ser >= {}",
                            id, detail.getMassAccumulated(), lastDetail.getMassAccumulated());
                    throw BusinessException.builder()
                            .message(String.format("La masa acumulada (mass_accumulated) debe ser mayor o igual a la anterior (%s)", 
                                    lastDetail.getMassAccumulated()))
                            .build();
                }
            }
        }
        
        // 4. Validar 1 > density > 0  (es decir: 0 < density < 1)
        if (detail.getDensity() == null || detail.getDensity() <= 0 || detail.getDensity() >= 1) {
            log.warn("Descartado detalle para orden {}: density={} debe estar entre 0 y 1 (exclusivo)", 
                    id, detail.getDensity());
            throw BusinessException.builder()
                    .message("La densidad (density) debe ser mayor a 0 y menor a 1")
                    .build();
        }
        
        // Si todas las validaciones pasan, guardar el detalle
        detail.setOrder(order);
        detail.setTimeStamp(java.time.LocalDateTime.now());
        
        try {
            // Guardar el detalle
            orderDetailDAO.save(detail);

            // Actualizar cabecera
            order.setLastMassAccumulated(detail.getMassAccumulated());
            order.setLastDensity(detail.getDensity());
            order.setLastTemperature(detail.getTemperature());
            order.setLastFlow(detail.getFlow());
            order.setLastTimestamp(detail.getTimeStamp());

            Order saved = orderDAO.save(order);

            // Disparar alarma de temperatura (no bloqueante)
            try {
                temperatureAlarmBusiness.handle(saved, detail.getTemperature());
            } catch (Exception alarmEx) {
                log.warn("No se pudo procesar alarma de temperatura para orden {}: {}", id, alarmEx.getMessage());
            }
            
            // Las anotaciones @JsonManagedReference y @JsonBackReference previenen la recursión infinita
            return saved;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Order closeOrder(int id) throws NotFoundException, BusinessException {
        Order o = load(id);
        try {
            if (o.getState() != 2) {
                throw BusinessException.builder().message("Orden no en estado 2").build();
            }
            int from = o.getState();
            o.setState(3);
            Order saved = orderDAO.save(o);
            try {
                saveStateLog(saved, from, 3, "user", "Order closed for loading");
            } catch (Exception ex) {
                log.warn("Could not save state log: " + ex.getMessage());
            }
            return saved;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Reconciliation finalizeWeighing(int id, Double finalWeighing) throws NotFoundException, BusinessException {
        Order o = load(id);
        try {
            if (o.getState() != 3) {
                throw BusinessException.builder().message("Orden no en estado 3").build();
            }
            
            //  Validación: finalWeighing debe ser mayor que initialWeighing
            if (finalWeighing == null || finalWeighing <= 0) {
                throw BusinessException.builder()
                        .message("El pesaje final debe ser mayor que cero")
                        .build();
            }
            
            if (o.getInitialWeighing() == null) {
                throw BusinessException.builder()
                        .message("No existe pesaje inicial registrado para esta orden")
                        .build();
            }
            
            if (finalWeighing <= o.getInitialWeighing()) {
                throw BusinessException.builder()
                        .message(String.format(
                            "El pesaje final (%.2f) debe ser mayor que el pesaje inicial (%.2f)", 
                            finalWeighing, o.getInitialWeighing()))
                        .build();
            }
            
            o.setFinalWeighing(finalWeighing);
            o.setTimeFinalWeighing(LocalDateTime.now());
            int from = o.getState();
            o.setState(4);
            Order saved = orderDAO.save(o);
            try {
                saveStateLog(saved, from, 4, "TMS", "Final weighing registered");
            } catch (Exception ex) {
                log.warn("Could not save state log: " + ex.getMessage());
            }

            // compute reconciliation
            Double initial = o.getInitialWeighing();
            Double productLoaded = o.getLastMassAccumulated() == null ? 0d : o.getLastMassAccumulated();
            Double netByScale = (initial == null || finalWeighing == null) ? null : (finalWeighing - initial);

            // averages over stored details
            java.util.List<OrderDetail> details = orderDetailDAO.findByOrderId(o.getId());
            Double avgTemp = null, avgDensity = null, avgFlow = null;
            if (details != null && !details.isEmpty()) {
                java.util.OptionalDouble t = details.stream().filter(d -> d.getTemperature() != null).mapToDouble(d -> d.getTemperature()).average();
                java.util.OptionalDouble den = details.stream().filter(d -> d.getDensity() != null).mapToDouble(d -> d.getDensity()).average();
                java.util.OptionalDouble f = details.stream().filter(d -> d.getFlow() != null).mapToDouble(d -> d.getFlow()).average();
                if (t.isPresent()) avgTemp = t.getAsDouble();
                if (den.isPresent()) avgDensity = den.getAsDouble();
                if (f.isPresent()) avgFlow = f.getAsDouble();
            }

            Double diff = (netByScale == null) ? null : (netByScale - productLoaded);

            Reconciliation rec = new Reconciliation();
            rec.setInitialWeighing(initial);
            rec.setFinalWeighing(finalWeighing);
            rec.setProductLoaded(productLoaded);
            rec.setNetByScale(netByScale);
            rec.setDifferenceScaleFlow(diff);
            rec.setAvgTemperature(avgTemp);
            rec.setAvgDensity(avgDensity);
            rec.setAvgFlow(avgFlow);

            return rec;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }


    @Override
    public Reconciliation getReconciliation(int id) throws NotFoundException, BusinessException {
        Order o = load(id);
        
        try {
            // Validar que la orden esté en estado 4 (finalizada)
            if (o.getState() != 4) {
                throw BusinessException.builder()
                        .message("La orden no está finalizada. No se puede generar la conciliación.")
                        .build();
            }
            
            // Validar que existan los pesajes necesarios
            if (o.getInitialWeighing() == null || o.getFinalWeighing() == null) {
                throw BusinessException.builder()
                        .message("Faltan datos de pesaje para generar la conciliación")
                        .build();
            }
            
            // Obtener valores base
            Double initial = o.getInitialWeighing();
            Double finalWeighing = o.getFinalWeighing();
            Double productLoaded = o.getLastMassAccumulated() == null ? 0d : o.getLastMassAccumulated();
            Double netByScale = finalWeighing - initial;
            
            // Calcular promedios sobre los detalles registrados
            List<OrderDetail> details = orderDetailDAO.findByOrderId(o.getId());
            Double avgTemp = null, avgDensity = null, avgFlow = null;
            
            if (details != null && !details.isEmpty()) {
                java.util.OptionalDouble t = details.stream()
                        .filter(d -> d.getTemperature() != null)
                        .mapToDouble(OrderDetail::getTemperature)
                        .average();
                
                java.util.OptionalDouble den = details.stream()
                        .filter(d -> d.getDensity() != null)
                        .mapToDouble(OrderDetail::getDensity)
                        .average();
                
                java.util.OptionalDouble f = details.stream()
                        .filter(d -> d.getFlow() != null && d.getFlow() > 0)
                        .mapToDouble(OrderDetail::getFlow)
                        .average();
                
                if (t.isPresent()) avgTemp = t.getAsDouble();
                if (den.isPresent()) avgDensity = den.getAsDouble();
                if (f.isPresent()) avgFlow = f.getAsDouble();
            }
            
            // Calcular diferencia entre balanza y caudalímetro
            Double diff = netByScale - productLoaded;
            
            // Construir objeto de conciliación
            Reconciliation rec = new Reconciliation();
            rec.setInitialWeighing(initial);
            rec.setFinalWeighing(finalWeighing);
            rec.setProductLoaded(productLoaded);
            rec.setNetByScale(netByScale);
            rec.setDifferenceScaleFlow(diff);
            rec.setAvgTemperature(avgTemp);
            rec.setAvgDensity(avgDensity);
            rec.setAvgFlow(avgFlow);
            
            return rec;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al generar conciliación para orden {}: {}", id, e.getMessage(), e);
            throw BusinessException.builder()
                    .ex(e)
                    .message("Error al generar la conciliación")
                    .build();
        }
    }
}
