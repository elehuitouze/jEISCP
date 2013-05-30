package de.csmp.jeiscp.eiscp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * parses the YAML-file of git-project
 * miracle2k-onkyo-eiscp
 * 
 * @see https://github.com/miracle2k/onkyo-eiscp
 * 
 * @author marcelpokrandt
 *
 */
public class EiscpCommandsParser {
	private static final String YAML_MODELS = "models";
	private static final String YAML_DESCRIPTION = "description";
	private static final String YAML_NAME = "name";
	private static final String YAML_VALUES = "values";

	private static final String EISCP_COMMANDS_YAML = "miracle2k-onkyo-eiscp/eiscp-commands.yaml";

	private static final Log log = LogFactory.getLog(EiscpCommandsParser.class);

	static Map<String, List<String>> modelsets = null;
	static Map<String, Object> ec = null;
	
	static List<CommandBlock> mainCommandBlocks = null;
	public static Map<String, Command> mainCommands = null;
	
	static Map<String, List<String>> keysetForModel = new HashMap<String, List<String>>();
	

	public static Map<String, Command> getMainCommands() {
		if (mainCommands == null) {
			mainCommands = new HashMap<String, Command>();
			for (CommandBlock cmdBlock : getMainCommandBlocks()) {
				for (Command cmd : cmdBlock.getValues()) {
					mainCommands.put(cmd.getIdentifier(), cmd);
				}
			}
		}
		return mainCommands;
	}
	
	public static Command getCommand(String commandId) {
		return getMainCommands().get(commandId);
	}
	
	public static String getIscpCommand(String commandId) {
		return getCommand(commandId).getIscpCommand();
	}
	
	public static List<CommandBlock> getMainCommandBlocks() {
		if (mainCommandBlocks == null) {
			Map<String, Object> main = (Map<String, Object>) getEiscpCommands().get("main");
			
			List<CommandBlock> mc = new LinkedList<CommandBlock>();
			for (String mainCommand : main.keySet()) {
				Map<String, Object> cmdMap = (Map<String, Object>) main.get(mainCommand);
				
				CommandBlock cmdBlock = new CommandBlock();
				cmdBlock.setCommand(mainCommand);
				cmdBlock.setName((String) cmdMap.get(YAML_NAME));
				cmdBlock.setDescription((String) cmdMap.get(YAML_DESCRIPTION));
				mc.add(cmdBlock);
				//log.info(cmdBlock);
				
				Map subCmdMap = (Map) cmdMap.get(YAML_VALUES);
				Set subCmdMapKeyset = subCmdMap.keySet();
				for (Object subCommandKeyO : subCmdMapKeyset) {
					Map<String, Object> subcommandO = (Map<String, Object>) subCmdMap.get(subCommandKeyO);
					if (subCommandKeyO instanceof String) {
						String subCommand = (String) subCommandKeyO;
						Command subC = new Command(cmdBlock);
						subC.setCommand(subCommand);
						
						String name = null;
						Object nameO = subcommandO.get(YAML_NAME);
						if (nameO == null) {
							//log.debug("no name for subcommand " + subCommand);
						} else if (nameO instanceof String) {
							name = (String) nameO;
						} else {
							name = (String) ((ArrayList) nameO).get(0);
						}
						subC.setName( name );
						
						subC.setDescription((String) subcommandO.get(YAML_DESCRIPTION));
						subC.setModels((String) subcommandO.get(YAML_MODELS));
						
						cmdBlock.getValues().add(subC);
						// log.info("   " + subC); 
					} else {
						if (log.isDebugEnabled())
							log.debug("subCommandKey unhandeled: " + subCommandKeyO.getClass().getName() + " - " + subCommandKeyO + " for " + cmdBlock);
					}
				}
				
			}
			
			mainCommandBlocks = mc;
		}
		return mainCommandBlocks;
	}
	
	public static Map<String, Object> getEiscpCommands() {
		if (ec == null) {
			InputStream is = EiscpCommandsParser.class.getClassLoader().getResourceAsStream(EISCP_COMMANDS_YAML);
			
			Yaml y = new Yaml();
			ec = (Map<String, Object>) y.load(is);
			log.debug("parsed YAML");
		}
		return ec;
	}
	
	public static Map<String, List<String>> getModelsets() {
		if (modelsets == null) {
			modelsets = (Map<String, List<String>>) getEiscpCommands().get("modelsets");
			log.debug("loaded " + modelsets.size() + " modelsets");
		}
		return modelsets;
	}
	
	public static List<String> getKeysetForModel(String model) {
		if (! keysetForModel.containsKey(model)) {
			log.debug("build keyset for model " + model);
			
			List<String> keysetList = new LinkedList<String>();
			Map<String, List<String>> modelsets = getModelsets();
			for (String keyset : modelsets.keySet()) {
				List<String> modelList = modelsets.get(keyset);
				if (modelList.contains(model)) keysetList.add(keyset);
			}
			
			keysetForModel.put(model, keysetList);
			return keysetList;
		}
		
		return keysetForModel.get(model);
	}
}