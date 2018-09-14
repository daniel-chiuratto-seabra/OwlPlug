package com.dropsnorz.owlplug.core.tasks;

import com.dropsnorz.owlplug.core.dao.PluginDAO;
import com.dropsnorz.owlplug.core.model.Plugin;
import java.io.File;

public class PluginRemoveTask extends AbstractTask {

	protected Plugin plugin;
	protected PluginDAO pluginDAO;
	
	public PluginRemoveTask(Plugin plugin, PluginDAO pluginDAO){
		
		this.plugin = plugin;
		this.pluginDAO = pluginDAO;
		
		setName("Remove Plugin - " + plugin.getName());
	}
	
	@Override
	protected TaskResult call() throws Exception {
		
		this.updateProgress(0, 1);
		this.updateMessage("Deleting plugin " + plugin.getName() + " ...");
		
		File pluginFile = new File(plugin.getPath());
		if (pluginFile.delete()) {
			pluginDAO.delete(plugin);
			
			this.updateProgress(1, 1);
			this.updateMessage("Plugin successfully deleted");
			
			return null;
		} else {
			this.updateMessage("Error during plugin removal");
			throw new TaskException("Error during plugin removal");
			
		}
		
	}

}