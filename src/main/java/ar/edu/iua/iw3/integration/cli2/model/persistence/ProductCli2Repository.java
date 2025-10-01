package ar.edu.iua.iw3.integration.cli2.model.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.iua.iw3.integration.cli2.model.ProductCli2;
import ar.edu.iua.iw3.integration.cli2.model.ProductCli2SlimView;

@Repository
@Profile("cli2")
public interface ProductCli2Repository extends JpaRepository<ProductCli2, Long> {
	public List<ProductCli2> findByExpirationDateBeforeOrderByExpirationDateDesc(Date expirationDate);
	
	public List<ProductCli2SlimView> findByOrderByPrecioDesc();
	
	// Métodos para filtros de precio - Punto 3
	public List<ProductCli2> findByPrecioBetweenOrderByPrecio(Double startPrice, Double endPrice);
	
	public List<ProductCli2> findByPrecioGreaterThanEqualOrderByPrecio(Double startPrice);
	
	public List<ProductCli2> findByPrecioLessThanEqualOrderByPrecio(Double endPrice);
	
	public List<ProductCli2> findAllByOrderByPrecio();
}