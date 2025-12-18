package ar.edu.iua.iw3.model.business.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ar.edu.iua.iw3.model.Product;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;

public interface IProductBusiness {

    public List<Product> list() throws BusinessException;

    public Page<Product> listPaginated(Pageable pageable) throws BusinessException;

    public Product load(String id) throws NotFoundException, BusinessException;

    //public Product load(String productName) throws NotFoundException, BusinessException;

    public Product add(Product product) throws FoundException, BusinessException;

    public Product update(Product product) throws NotFoundException, BusinessException, FoundException;

    public void delete(Product product) throws NotFoundException, BusinessException;

    public void delete(String id) throws NotFoundException, BusinessException;

}