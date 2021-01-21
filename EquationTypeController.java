package com.rest.API.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rest.API.exception.EquationNotFoundException;
import com.rest.API.model.Equation;
import com.rest.API.model.EquationType;
import com.rest.API.repository.EquationTypeRepository;

@RestController
@RequestMapping("/equationTypes")
public class EquationTypeController {
	private EquationTypeRepository repository = null;

	public EquationTypeController(EquationTypeRepository repository) {
	    this.repository = repository;
	}

	@GetMapping
	public Iterable<EquationType> getEquations() {
	    return repository.findAll();
	}

	@GetMapping("/id/{id}")
	public EquationType getEquationTypeById(@PathVariable Long id) {
	    return repository.findById(id).orElseThrow(EquationNotFoundException::new);
	}

	@GetMapping("/list")
	public Iterable<EquationType> getEquationTypeList() {
		return repository.listEquationTypes();
	}

	@GetMapping("/category/name/{categoryName}")
	public Iterable<EquationType> findByCategoryName(@PathVariable String categoryName) {
		return repository.findByCategoryName(categoryName);
	}

	@GetMapping("/category/code/{categoryCode}")
	public Iterable<EquationType> findByCategoryCode(@PathVariable Long categoryCode) {
		return repository.findByCategoryCode(categoryCode);
	}

	@GetMapping("/category/all")
	public Iterable<EquationType> findAllCategories() {
		return repository.findAllCategories();
	}
}
