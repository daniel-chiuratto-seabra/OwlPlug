package com.dropsnorz.owlplug.core.engine.repositories;

public class RepositoryStrategyException extends Exception {
	
	public RepositoryStrategyException(Exception e) {
		super(e);
	}
	
	public RepositoryStrategyException(String message) {
		super(message);
	}
	
	public RepositoryStrategyException(String message, Exception e) {
		super(message, e);
	}

}
