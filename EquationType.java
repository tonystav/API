package com.rest.API.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity
@Table(name="equationtype")
public class EquationType {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String category;
    private String example;

    public Long getId() {
		return id;
	}

    public void setId(Long id) {
		this.id = id;
	}

    public String getCategory() {
		return category;
	}

    public void setCategory(String category) {
		this.category = category;
	}

    public String getExample() {
		return example;
	}

    public void setExample(String example) {
		this.example = example;
	}
}
