package ar.edu.iua.iw3.model.business.interfaces;

import ar.edu.iua.iw3.model.Customer;
//import ar.edu.iua.iw3.model.Product;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;

import java.util.List;

public interface ICustomerBusiness {
    public List<Customer> list() throws BusinessException;

    public Customer load(String id) throws NotFoundException, BusinessException;

    public Customer load(long phoneNumber) throws NotFoundException, BusinessException;

    public Customer add(Customer customer) throws FoundException, BusinessException;

    public Customer update(Customer customer) throws NotFoundException, BusinessException, FoundException;

    public void delete(Customer customer) throws NotFoundException, BusinessException;

    public void delete(String id) throws NotFoundException, BusinessException;


}