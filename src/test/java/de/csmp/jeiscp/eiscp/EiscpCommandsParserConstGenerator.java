package de.csmp.jeiscp.eiscp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class EiscpCommandsParserConstGenerator {

	@Test
	public void generateConstFile() throws Exception {
		String fileName = "target/EiscpCommmandsConstants.java";
		
		FileWriter fw = new FileWriter(new File(fileName));
		fw.write("package " + this.getClass().getPackage().getName() + ";\n\n\n");
		fw.write("/** GENERATED FILE - do not modify */\n");
		fw.write("public class EiscpCommmandsConstants {\n\n");
		
		
		Map<String, CommandBlock> idToCommandBlockMap = EiscpCommandsParser.getIdToCommandBlockMap();
		Set<String> sortedIds = new TreeSet<String>(idToCommandBlockMap.keySet());
		
		for (String cbid : sortedIds) {
			CommandBlock cmdBlock = idToCommandBlockMap.get(cbid);
			writeEntry(fw, cmdBlock);
			
			Map<String, Command> idToCommandMap = new HashMap<String, Command>();
			for (Command cmd : cmdBlock.values) {
				idToCommandMap.put(cmd.getIdentifier(), cmd);
			}
			
			TreeSet<String> cmdIds = new TreeSet<String>(idToCommandMap.keySet());
			for (String cid : cmdIds) {
				writeEntry(fw, idToCommandMap.get(cid));
			}
		}
		fw.write("}");
		fw.close();
		System.out.println("wrote to " + fileName);
		
	}

	private static void writeEntry(FileWriter fw, Command cmd) throws IOException {
		String cid = cmd.getIdentifier();
		fw.write("/**\n");
		fw.write(" * ISCP Command: " + cmd.getIscpCommand() + "\n");
		fw.write(" * " + cmd.getDescription() + "\n */\n");
		
		String constId = cid.replaceAll("-", "_").replaceAll("::", "_");
		if (constId.startsWith("1")) {
			constId = "_" + constId;
		}
		constId = constId.toUpperCase();
		fw.write("public static final String " + constId + " = \"" + cid + "\";\n");
		fw.write("// public static final String " + constId + "_ISCP = \"" + cmd.getIscpCommand() + "\";\n");

		fw.write("\n");
	}

	private static void writeEntry(FileWriter fw, CommandBlock cmdBlock)
			throws IOException {
		fw.write("/**\n");
		fw.write(" * CommandBlock\n");
		fw.write(" * ISCP Command prefix: " + cmdBlock.getCommand() + "\n");
		fw.write(" * " + cmdBlock.getDescription() + "\n */\n");
		
		String constId = cmdBlock.getName().replaceAll("-", "_").replaceAll("::", "_");
		if (constId.startsWith("1")) {
			constId = "_" + constId;
		}
		constId = constId.toUpperCase();
		fw.write("public static final String " + constId + " = \"" + cmdBlock.getName() + "\";\n");
		fw.write("public static final String " + constId + "_ISCP = \"" + cmdBlock.getCommand() + "\";\n");
		
		fw.write("\n");
	}

}
