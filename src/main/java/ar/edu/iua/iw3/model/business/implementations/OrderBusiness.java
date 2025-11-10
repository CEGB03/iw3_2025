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
import ar.edu.iua.iw3.model.persistence.OrderRepository;
import ar.edu.iua.iw3.model.persistence.OrderDetailRepository;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.DoubleStream;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderBusiness implements IOrderBusiness {
    
    @Autowired
    private OrderRepository orderDAO;

    @Autowired
    private OrderDetailRepository orderDetailDAO;

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
            return orderDAO.save(orden);
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
            o.setInitialWeighing(tare);
            o.setTimeInitialWeighing(LocalDateTime.now());
            // generate 5-digit password
            int pass = new Random().nextInt(90000) + 10000;
            o.setActivationPassword(pass);
            o.setState(2);
            return orderDAO.save(o);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Order addDetail(int id, OrderDetail detail, Integer password) throws NotFoundException, BusinessException {
        Order o = load(id);
        try {
            if (o.getState() != 2) {
                // Only accept details when in loading state
                return o;
            }
            if (o.getActivationPassword() != null && password != null) {
                if (!o.getActivationPassword().equals(password)) {
                    // invalid password -> discard
                    return o;
                }
            }

            // validate detail
            if (detail.getFlow() == null || detail.getFlow() <= 0) {
                return o;
            }
            if (detail.getMassAccumulated() == null || detail.getMassAccumulated() <= 0) {
                return o;
            }
            if (o.getLastMassAccumulated() != null && detail.getMassAccumulated() <= o.getLastMassAccumulated()) {
                return o;
            }

            // set timestamps and associations
            if (detail.getTimeStamp() == null) {
                detail.setTimeStamp(LocalDateTime.now());
            }
            detail.setOrder(o);
            // persist detail
            orderDetailDAO.save(detail);

            // update header last values
            o.setLastMassAccumulated(detail.getMassAccumulated());
            o.setLastDensity(detail.getDensity());
            o.setLastTemperature(detail.getTemperature());
            o.setLastFlow(detail.getFlow());
            o.setLastTimestamp(detail.getTimeStamp());
            if (o.getTimeInitialLoading() == null) {
                o.setTimeInitialLoading(detail.getTimeStamp());
            }
            o.setTimeFinalLoading(detail.getTimeStamp());

            return orderDAO.save(o);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Order closeOrder(int id) throws NotFoundException, BusinessException {
        Order o = load(id);
        try {
            if (o.getState() != 2) {
                throw BusinessException.builder().message("Orden no en estado 2").build();
            }
            o.setState(3);
            return orderDAO.save(o);
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
            o.setFinalWeighing(finalWeighing);
            o.setTimeFinalWeighing(LocalDateTime.now());
            o.setState(4);
            orderDAO.save(o);

            // compute reconciliation
            Double initial = o.getInitialWeighing();
            Double productLoaded = o.getLastMassAccumulated() == null ? 0d : o.getLastMassAccumulated();
            Double netByScale = (initial == null || finalWeighing == null) ? null : (finalWeighing - initial);

            // averages over stored details
            java.util.List<OrderDetail> details = orderDetailDAO.findByOrderId(o.getId());
            Double avgTemp = null, avgDensity = null, avgFlow = null;
            if (details != null && !details.isEmpty()) {
                avgTemp = details.stream().filter(d -> d.getTemperature() != null).mapToDouble(d -> d.getTemperature()).average().orElse(Double.NaN);
                avgDensity = details.stream().filter(d -> d.getDensity() != null).mapToDouble(d -> d.getDensity()).average().orElse(Double.NaN);
                avgFlow = details.stream().filter(d -> d.getFlow() != null).mapToDouble(d -> d.getFlow()).average().orElse(Double.NaN);
            }

            Double diff = (netByScale == null) ? null : (netByScale - productLoaded);

            return new Reconciliation(initial, finalWeighing, productLoaded, netByScale, diff, avgTemp, avgDensity, avgFlow);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

}
