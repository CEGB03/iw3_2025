package ar.edu.iua.iw3.model.business.interfaces;

import ar.edu.iua.iw3.model.Driver;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;

import java.util.List;

public interface IDriverBusiness {
    public List<Driver> list() throws BusinessException;

    public Driver load(String id) throws NotFoundException, BusinessException;

    public Driver load(long dni) throws NotFoundException, BusinessException;

    public Driver add(Driver driver) throws FoundException, BusinessException;

    public Driver update(Driver driver) throws NotFoundException, BusinessException, FoundException;

    public void delete(Driver driver) throws NotFoundException, BusinessException;

    public void delete(String id) throws NotFoundException, BusinessException;

}