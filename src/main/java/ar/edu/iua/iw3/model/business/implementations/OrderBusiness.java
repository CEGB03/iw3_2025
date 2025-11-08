package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.IOrderBusiness;
import ar.edu.iua.iw3.model.persistence.OrderRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderBusiness implements IOrderBusiness {
    
    @Autowired
    private OrderRepository orderDAO;

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

}
