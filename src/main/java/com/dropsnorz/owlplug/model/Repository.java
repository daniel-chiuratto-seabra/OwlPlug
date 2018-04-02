package com.dropsnorz.owlplug.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Repository {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	protected String name;
	protected String localPath;
	protected String remoteUrl;
	
	Repository(String name){
		this.name = name;
	}
	
	
	@Override
	public String toString() {
		return name;
	}

	
}