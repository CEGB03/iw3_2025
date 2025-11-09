package ar.edu.iua.iw3.model.business.interfaces;

import java.util.List;

import ar.edu.iua.iw3.model.Truck;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;

public interface ITruckBusiness {
    
    public List<Truck> list() throws BusinessException;

    public Truck load(String id) throws NotFoundException, BusinessException;

    public Truck loadLicensePlate(String licensePlate) throws NotFoundException, BusinessException;

    public Truck add(Truck truck) throws FoundException, BusinessException;

    public Truck update(Truck truck) throws NotFoundException, BusinessException;

    public void delete(String id) throws NotFoundException, BusinessException;

    public void delete(Truck truck) throws NotFoundException, BusinessException;

}
