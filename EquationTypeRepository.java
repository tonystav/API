package com.rest.API.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rest.API.model.Equation;
import com.rest.API.model.EquationType;

@Repository
public interface EquationTypeRepository extends CrudRepository<EquationType, Long> {
	@Query("select t.category from EquationType t")
	public Iterable<EquationType> findAllCategories();

	@Query("select t.id from EquationType t where t.category like %:categoryName%")
	public Iterable<EquationType> findByCategoryName(@Param("categoryName") String categoryName);

	@Query("select t.category from EquationType t where t.id = :categoryCode")
	public Iterable<EquationType> findByCategoryCode(@Param("categoryCode") Long categoryCode);

    @Query("select t.category, t.example from EquationType t where t.category = t.id\r\n")
    public Iterable<EquationType> listEquationTypes();
}
