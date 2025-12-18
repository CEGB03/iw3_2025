package ar.edu.iua.iw3.model.business.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.model.Product;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.interfaces.IProductBusiness;
import ar.edu.iua.iw3.model.persistence.ProductRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductBusiness implements IProductBusiness{
    
    @Autowired
    private ProductRepository productDAO;

    @Override
    public List<Product> list() throws BusinessException {
        try {
            return productDAO.findAll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override
    public Page<Product> listPaginated(Pageable pageable) throws BusinessException {
        try {
            return productDAO.findAll(pageable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }


    //Fijarme esta función al probarla puede q tire los BusinessException en caso de q exista id pero no
    //nombre o viceversa
    @Override
    public Product load(String id) throws NotFoundException, BusinessException {
        Optional<Product> productFound;

        try {
            productFound = productDAO.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        try{
            productFound = productDAO.findByProductName(id);
        } catch (Exception e){
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        if (productFound.isEmpty())
            throw NotFoundException.builder().message("No se encuentra el Producto id= " + id).build();
        return productFound.get();
    }

    @Override
    public Product add(Product product) throws FoundException, BusinessException {
        //   NO verificar por ID si es null (caso de inserción nueva)
        // Solo verificar si el productName ya existe
        if (productDAO.findByProductName(product.getProductName()).isPresent()) {
            throw FoundException.builder().message("Ya existe el Producto " + product.getProductName()).build();
        }

        try {
            return productDAO.save(product);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("Error al Crear Nuevo Producto").build();
        }
    }

    @Override
    public Product update(Product product) throws NotFoundException, FoundException, BusinessException {

        load(product.getId());

        Optional<Product> productFound;
        try {
            productFound = productDAO.findByProductNameAndIdNot(product.getProductName(), product.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

        if (productFound.isPresent()) {
            throw FoundException.builder().message("Ya Existe un Producto con el Nombre =" + product.getProductName()).build();
        }

        try {
            return productDAO.save(product);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //throw BusinessException.builder().ex(e).build();
            throw BusinessException.builder().message("Error al Actualizar Producto").build();
        }
    }

    @Override
    public void delete(Product product) throws NotFoundException, BusinessException {
        delete(product.getId());
    }

    @Override
    public void delete(String id) throws NotFoundException, BusinessException {
        load(id);
        try {
            productDAO.deleteById(id);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }

    }
    
}
