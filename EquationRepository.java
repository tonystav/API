package com.rest.API.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rest.API.model.Equation;

@Repository
public interface EquationRepository extends CrudRepository<Equation, Long> {
	@Query("select equation from Equation e where e.category = :category")
	public Iterable<Equation> findByCategory(@Param("category") Long category);

    @Query("select new map(e.id as id, e.equation as equation, e.description as description, e.advice as advice, t.category as category, e.edit as edit)\r\n" + 
    		"from Equation e, EquationType t where e.category = t.id\r\n")
    public Iterable<Equation> listEquations();

    // Returns standard JSON-formatted results (ex. "Name:Value" pairs) but not in arrangement declared in query
    @Query("select new map(e.id as id, e.equation as equation, e.description as description, e.advice as advice, t.category as category, e.edit as edit)\r\n" + 
    		"from Equation e, EquationType t where e.category = t.id\r\n" +
    		"and e.equation like %:equation% and e.description like %:description%\r\n" +
    		"and e.advice like %:advice% and t.category like %:category%")
    public Iterable<Equation> filterEquations(@Param("equation") String equation, @Param("description") String description,
    										@Param("advice") String advice, @Param("category") String category);

    @Query("select max(id)+1 as newid from Equation")
    public BigDecimal newId();
}
