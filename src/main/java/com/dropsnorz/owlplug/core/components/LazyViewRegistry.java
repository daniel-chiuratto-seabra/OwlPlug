package com.dropsnorz.owlplug.core.components;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

@Component
public class LazyViewRegistry {
	
	public static String NEW_FILESYSTEM_REPOSITORY_VIEW = "NEW_FILESYSTEM_REPOSITORY_VIEW";
	public static String NEW_REPOSITORY_MENU_VIEW = "NEW_REPOSITORY_MENU_VIEW";
	public static String NEW_ACCOUNT_VIEW = "NEW_ACCOUNT_VIEW";

	
	@Autowired
	private ApplicationContext context;
	
	private HashMap<String, Parent> viewRegistry;
	
	LazyViewRegistry(){
		
		viewRegistry = new HashMap<String, Parent>();
		
	}
	
	/**
	 * Preload all detached views. 
	 * Must be called after spring components setup to allow fxml bindings on controllers
	 */
	public void preload() {
		preloadFxml(NEW_FILESYSTEM_REPOSITORY_VIEW,"/fxml/dialogs/NewFileSystemRepository.fxml");
		preloadFxml(NEW_REPOSITORY_MENU_VIEW,"/fxml/NewRepositoryMenu.fxml");
		preloadFxml(NEW_ACCOUNT_VIEW,"/fxml/dialogs/NewAccount.fxml");
	}
	
	public Parent get(String key) {
		return viewRegistry.get(key);
	}
	
	public Node getAsNode(String key) {
		return viewRegistry.get(key);
	}
	
	
	private void preloadFxml(String key, String ressource) {
		
		viewRegistry.put(key, loadFxml(ressource));
	}
	
	
	
	private Parent loadFxml(String ressource) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ressource));
		loader.setControllerFactory(context::getBean);
		try {
			Parent node = loader.load();
			return node;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}


}