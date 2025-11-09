package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Truck;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.ITruckBusiness;
import ar.edu.iua.iw3.model.persistence.TruckRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TruckBusiness implements ITruckBusiness {

    @Autowired
    private TruckRepository truckDAO;

    @Override
    public List<Truck> list() throws BusinessException {
        try {
            return truckDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Truck load(String id) throws NotFoundException, BusinessException {
        Optional<Truck> r;
        try {
            r = truckDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el camión con id " + id).build();
        }

        return r.get();
    }

    @Override
    public Truck loadLicensePlate(String licensePlate) throws NotFoundException, BusinessException {
        Optional<Truck> r;
        try {
            r = truckDAO.findByLicensePlate(licensePlate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el camión con patente " + licensePlate).build();
        }

        return r.get();
    }

    @Override
    public Truck add(Truck truck) throws FoundException, BusinessException {
        try {
            load(truck.getId());
            throw FoundException.builder().message("El camión con id " + truck.getId() + " ya existe").build();
        } catch (NotFoundException e) {
            // Si no existe, se continúa
        }

        try {
            return truckDAO.save(truck);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public Truck update(Truck truck) throws NotFoundException, BusinessException {
        load(truck.getId()); // Verifica que exista
        try {
            return truckDAO.save(truck);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public void delete(String id) throws NotFoundException, BusinessException {
        load(id);
        try {
            truckDAO.deleteById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).message(e.getMessage()).build();
        }
    }

    @Override
    public void delete(Truck truck) throws NotFoundException, BusinessException {
        if (truck == null) {
            throw BusinessException.builder().message("El camión o su ID son nulos").build();
        }
        delete(truck.getId());
    }
}
