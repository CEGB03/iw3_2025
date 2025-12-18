package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Driver;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.IDriverBusiness;
import ar.edu.iua.iw3.model.persistence.DriverRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverBusiness implements IDriverBusiness {

    @Autowired
    private DriverRepository driverDAO;

    @Override
    public List<Driver> list() throws BusinessException {
        try {
            return driverDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Page<Driver> listPaginated(Pageable pageable) throws BusinessException {
        try {
            return driverDAO.findAll(pageable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Driver load(String id) throws NotFoundException, BusinessException {
        Optional<Driver> r;
        try {
            r = driverDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el Driver id=" + id).build();
        }
        return r.get();
    }

    @Override
    public Driver load(long dni) throws NotFoundException, BusinessException {
        Optional<Driver> r;
        try {
            r = driverDAO.findByDni(dni);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el Driver dni=" + dni).build();
        }
        return r.get();
    }

    @Override
    public Driver add(Driver driver) throws FoundException, BusinessException {
        //   NO verificar por ID si es null (caso de inserción nueva)
        // Solo verificar si el DNI ya existe
        if (driverDAO.findByDni(driver.getDni()).isPresent()) {
            throw FoundException.builder().message("Ya existe un Driver con DNI=" + driver.getDni()).build();
        }

        try {
            return driverDAO.save(driver);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Driver update(Driver driver) throws NotFoundException, BusinessException {
        load(driver.getId()); // Verifica que exista
        try {
            return driverDAO.save(driver);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public void delete(String id) throws NotFoundException, BusinessException {
        load(id);
        try {
            driverDAO.deleteById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public void delete(Driver driver) throws NotFoundException, BusinessException {
        if (driver == null || driver.getId() == null) {
            throw BusinessException.builder().message("El driver o su ID son nulos").build();
        }
        delete(driver.getId());
    }
}
