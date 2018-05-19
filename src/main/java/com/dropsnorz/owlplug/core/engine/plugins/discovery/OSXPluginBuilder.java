package com.dropsnorz.owlplug.core.engine.plugins.discovery;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.dropsnorz.owlplug.core.model.Plugin;
import com.dropsnorz.owlplug.core.model.PluginType;
import com.dropsnorz.owlplug.core.model.VST2Plugin;
import com.dropsnorz.owlplug.core.model.VST3Plugin;

public class OSXPluginBuilder extends NativePluginBuilder {

	OSXPluginBuilder(PluginType pluginType) {
		super(pluginType);
	}

	@Override
	public Plugin build(File pluginFile) {

		if(pluginType == PluginType.VST2){
			return buildVst2Plugin(pluginFile);
		}

		return null;
	}

	private Plugin buildVst2Plugin(File pluginFile) {
		
		String pluginName = FilenameUtils.removeExtension(pluginFile.getName());
		String pluginPath = pluginFile.getAbsolutePath().replace("\\", "/");
		
		Plugin plugin = new VST2Plugin(pluginName, pluginPath);
		
		File pList = new File(pluginFile.getAbsolutePath() + "/Contents/Info.plist");
		if(pList.exists()) {
			buildPluginFromVST2Plist(plugin, pList);
		}


		return plugin;
	}
	
	private Plugin buildVST3Plugin(File pluginFile) {
		String pluginName = FilenameUtils.removeExtension(pluginFile.getName());
		String pluginPath = pluginFile.getAbsolutePath().replace("\\", "/");
		
		Plugin plugin = new VST3Plugin(pluginName, pluginPath);
		
		return plugin;
	}

	private void buildPluginFromVST2Plist(Plugin plugin, File pList) {

		try {
			NSDictionary rootDict = (NSDictionary)PropertyListParser.parse(pList);
			NSObject NSBundleId = rootDict.objectForKey("CFBundleIdentifier");
			NSObject NSBundleVersion = rootDict.objectForKey("CFBundleShortVersionString");
			
			if(NSBundleVersion == null) {
				NSBundleVersion = rootDict.objectForKey("CFBundleVersion");
			}
			
			if(NSBundleId != null) {
				plugin.setBundleId(NSBundleId.toString());
			}
			
			if(NSBundleVersion != null) {
				plugin.setVersion(NSBundleVersion.toString());
			}
			
			
		} catch (IOException | PropertyListFormatException | ParseException | ParserConfigurationException
				| SAXException e) {
			e.printStackTrace();

		}	

	}

}