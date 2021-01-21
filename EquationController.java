package com.rest.API.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rest.API.exception.EquationNotFoundException;
import com.rest.API.model.Equation;
import com.rest.API.repository.EquationRepository;

@RestController
@RequestMapping("/equations")
public class EquationController {
	private EquationRepository repository = null;

	public EquationController(EquationRepository repository) {
	    this.repository = repository;
	}

	@GetMapping
	public Iterable<Equation> getEquations() {
	    return repository.findAll();
	}

	@GetMapping("/id/{id}")
	public Equation getEquationById(@PathVariable Long id) {
	    return repository.findById(id).orElseThrow(EquationNotFoundException::new);
	}

	@GetMapping("/list")
	public Iterable<Equation> getEquationList() {
		return repository.listEquations();
	}

	@GetMapping("/filter/{equation}/{description}/{advice}/{category}")
	public Iterable<Equation> getFilteredEquations(@PathVariable String equation, @PathVariable String description,
													@PathVariable String advice, @PathVariable String category) {
		return repository.filterEquations(equation, description, advice, category);
	}

	@GetMapping("/category/{category}")
	public Iterable<Equation> findByCategory(@PathVariable Long category) {
		return repository.findByCategory(category);
	}

	@GetMapping("/newid")
	public BigDecimal getNewId() {
	    return repository.newId();
	}

	@PostMapping(path="/create", consumes="application/json", produces="application/json")
	@ResponseBody
	public Equation createEquation(@RequestBody Equation equation) {
	    return repository.save(equation);
	}

	@PutMapping("/update/{id}")
	public Equation updateEquation(@PathVariable String id, @RequestBody Equation equation) {
		Long longId = Long.parseLong(id);
		Equation equationToUpdate = repository.findById(longId).orElseThrow(EquationNotFoundException::new);

        equationToUpdate.setEquation(equation.getEquation());
        equationToUpdate.setDescription(equation.getDescription());
        equationToUpdate.setAdvice(equation.getAdvice());
        equationToUpdate.setCategory(equation.getCategory());

	    return repository.save(equationToUpdate);
	}

	@DeleteMapping("/delete/{id}")
	public void deleteEquation(@PathVariable String id) {
		Long longId = Long.parseLong(id);
	    repository.findById(longId).orElseThrow(EquationNotFoundException::new);
	    repository.deleteById(longId);
	}
}
