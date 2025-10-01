package ar.edu.iua.iw3.integration.cli2.model.business;


import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.iw3.integration.cli2.model.ProductCli2;
import ar.edu.iua.iw3.integration.cli2.model.ProductCli2SlimView;
import ar.edu.iua.iw3.integration.cli2.model.persistence.ProductCli2Repository;
import ar.edu.iua.iw3.model.business.BusinessException;
import ar.edu.iua.iw3.model.business.FoundException;
import ar.edu.iua.iw3.model.business.IProductBusiness;
import ar.edu.iua.iw3.model.business.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductCli2Business implements IProductCli2Business {

	@Autowired
	private ProductCli2Repository productDAO;

	@Autowired
	private IProductBusiness productBaseBusiness;

	@Override
	public List<ProductCli2> list() throws BusinessException {
		try {
			return productDAO.findAll();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

	@Override
	public List<ProductCli2> listExpired(Date date) throws BusinessException {
		try {
			return productDAO.findByExpirationDateBeforeOrderByExpirationDateDesc(date);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

	@Override
	public List<ProductCli2SlimView> listSlim() throws BusinessException {
		try {
			return productDAO.findByOrderByPrecioDesc();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

	@Override
	public ProductCli2 add(ProductCli2 product) throws FoundException, BusinessException {
		// Verificar que no exista producto con el mismo ID en la tabla base
		try {
			productBaseBusiness.load(product.getId());
			throw FoundException.builder().message("Se encontró el Producto id=" + product.getId()).build();
		} catch (NotFoundException e) {
			// Producto no existe, podemos continuar
		}

		try {
			return productDAO.save(product);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

	@Override
	public List<ProductCli2> listByPrice(Double startPrice, Double endPrice) throws BusinessException {
		try {
			// Lógica para los 4 casos requeridos
			if (startPrice != null && endPrice != null) {
				// Caso 1: Ambos parámetros - rango entre start y end
				return productDAO.findByPrecioBetweenOrderByPrecio(startPrice, endPrice);
			} else if (startPrice != null) {
				// Caso 2: Solo start-price - mayor o igual a start
				return productDAO.findByPrecioGreaterThanEqualOrderByPrecio(startPrice);
			} else if (endPrice != null) {
				// Caso 3: Solo end-price - menor o igual a end  
				return productDAO.findByPrecioLessThanEqualOrderByPrecio(endPrice);
			} else {
				// Caso 4: Sin parámetros - todos los productos
				return productDAO.findAllByOrderByPrecio();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw BusinessException.builder().ex(e).build();
		}
	}

}

