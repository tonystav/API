package com.rest.API.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="equation")
public class Equation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String equation;
    private String description;
    private String advice;
    private Long category;
    private Boolean edit;

	public Equation() {
		super();
	}

    // Check if need to include id, since it should be automatically generated
    //public Equation(Long id, String equation, String description, String advice, Long category, Boolean edit) {
    public Equation(String equation, String description, String advice, Long category, Boolean edit) {
		super();
		//this.id = id;
		this.equation = equation;
		this.description = description;
		this.advice = advice;
		this.category = category;
		this.edit = edit;
	}

	public Long getId() {
		return id;
	}

    public void setId(Long id) {
		this.id = id;
	}

    public String getEquation() {
		return equation;
	}

    public void setEquation(String equation) {
		this.equation = equation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAdvice() {
		return advice;
	}

	public void setAdvice(String advice) {
		this.advice = advice;
	}

	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	public Boolean getEdit() {
		return edit;
	}

	public void setEdit(Boolean edit) {
		this.edit = edit;
	}

	@Override
	public String toString() {
		return "Equation [id=" + id + ", equation=" + equation + ", description=" + description
				+ ", advice=" + advice + ", category=" + category + ", edit=" + edit + "]";
	}

	public String EquationAsJson() {
        String jsonString = "{" +	"\"id\":\"" + this.getId() + "\"," +
									"\"equation\":\"" + this.getEquation() + "\"," +
									"\"description\":\"" + this.getDescription() + "\"," +
									"\"advice\":\"" + this.getAdvice() + "\"," +
									"\"category\":\"" + this.getCategory() + "\"," +
									"\"edit\":\"" + this.getEdit() + "\""	
        					+ "}";

        return jsonString;
	}
}
