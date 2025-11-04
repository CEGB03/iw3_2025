package ar.edu.iua.iw3.model.business;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Orden;
import ar.edu.iua.iw3.model.persistence.OrdenRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrdenBusiness implements IOrdenBusiness {
    
    @Autowired
    private OrdenRepository ordenDAO;

    @Override
    public List<Orden> list() throws BusinessException{
        try {
            return ordenDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Orden load(int id) throws NotFoundException, BusinessException {
        Optional<Orden> r;
        try {
            r = ordenDAO.findById(id);
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
    public Orden add(Orden orden) throws FoundException, BusinessException {
        try {
            load(orden.getId());
            throw FoundException.builder().message("La orden con id " + orden.getId() + " ya existe").build();
        } catch (NotFoundException e) {
            
        }
    
        try {
            return ordenDAO.save(orden);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public void delete(int id) throws NotFoundException, BusinessException {
        load(id);
        try {
            ordenDAO.deleteById(id);

    } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

}
