package edu.uiuc.zenvisage.zql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uiuc.zenvisage.zqlcomplete.executor.ZQLTable;
import edu.uiuc.zenvisage.zqlcomplete.executor.Processe;
import edu.uiuc.zenvisage.zqlcomplete.executor.XColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.YColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.ZColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.ZQLRow;

public class ZQLParser {

	public static void main(String[] args) {
		
		String pattern = "(\\w)(\\s+)([\\.,])";
		System.out.println("hi .".replaceAll(pattern, "$1$3"));
		
		
		//(space before word)(word)(space after word)(comma)* : last word is (space)(word)
		pattern = "(ax)\\s([a-zA-Z0-9_]+)\\s*=\\s*(\\[?[a-zA-Z0-9_]+\\]?)";
		Matcher match = Pattern.compile(pattern).matcher("ax v_1=[year]");
	    while (match.find()) {
	        for (int i = 0; i <= match.groupCount(); i++) {
	            System.out.println(i + " " + match.group(i));
	            //System.out.println(match.start(i) + " " + match.end(i));
	            // match start: regexMatcher.start(i)
	            // match end: regexMatcher.end(i)
	        }
	        System.out.println("---");
	    }
	    parseList("[CA]");
	    parseList("[CA, NY,LA]");
	    parseList("CA");
	    parseList("CA,NY");
	    parseList("state.*");
	    parseList("*");
	    parseList("[*]");
	    parseScript("ax x1 = [year]");
	    
        System.out.println("-----");
		//pattern = "(ax)\\s([a-zA-Z0-9_]+)\\s*=\\s*(\\[?[a-zA-Z0-9_]+\\]?)";
        //vc f1 = {x1, y1, z1}
        //pattern = "\\{(?:" + variablePattern  + ")|(?:" + listPattern + ")|(?:" + assignPattern  + ")\\}";
        pattern = "vc\\s" + variablePattern + "\\s*=\\s*\\{(.+)\\}";
		match = Pattern.compile(vcPattern).matcher("vc f1 = {ax v1 = [123], soldprice, state.*}");
	    while (match.find()) {
	        for (int i = 0; i <= match.groupCount(); i++) {
	            System.out.println(i + " " + match.group(i));
	            //System.out.println(match.start(i) + " " + match.end(i));
	            // match start: regexMatcher.start(i)
	            // match end: regexMatcher.end(i)
	        }
	        System.out.println("---");
	    }
        System.out.println("-----");
		match = Pattern.compile(variablePattern).matcher("vara1");
	    while (match.find()) {
	        for (int i = 0; i <= match.groupCount(); i++) {
	            System.out.println(i + " " + match.group(i));
	            //System.out.println(match.start(i) + " " + match.end(i));
	            // match start: regexMatcher.start(i)
	            // match end: regexMatcher.end(i)
	        }
	        System.out.println("---");
	    }
	    String temp = "process(argmin={x1,y1}, k=1, DEuclidean(f1,f2))";
		match = Pattern.compile(processPattern).matcher(temp);
	    while (match.find()) {
	        for (int i = 0; i <= match.groupCount(); i++) {
	            System.out.println(i + " " + match.group(i));
	            //System.out.println(match.start(i) + " " + match.end(i));
	            // match start: regexMatcher.start(i)
	            // match end: regexMatcher.end(i)
	        }
	        System.out.println("---");
	    }
	    pattern = "(argmax)\\s*=\\s*\\{("+listPattern+")\\}";
		//pattern = "(argmin)\\s*=\\s*\\{("+listPattern+")\\}";
	    temp = "argmax={x1}";
		match = Pattern.compile(pattern).matcher(temp);
	    while (match.find()) {
	        for (int i = 0; i <= match.groupCount(); i++) {
	            System.out.println(i + " " + match.group(i));
	            //System.out.println(match.start(i) + " " + match.end(i));
	            // match start: regexMatcher.start(i)
	            // match end: regexMatcher.end(i)
	        }
	        System.out.println("---");
	    }
	}
	/**
	 * Parse a ZQL Script, line by line
	 * Converts into a ZQLGraph to process
	 * @param script
	 */
	public static QueryGraph parseScript(String script) {
		ZQLTable table = new ZQLTable();
		Scanner scan = new Scanner(script);
		LookUpTable lookUpTable = new LookUpTable();
		while(scan.hasNext()) {
			String line = scan.nextLine();
			
			if (isAxisVariableAssignment(line)) {
				handleAxisVariableAssignment(line, lookUpTable);
			} else if (isVCAssignment(line)) {
				handleVCAssignment(line, lookUpTable, table);
			} else if (isProcess(line)) {
				handleProcess(line, lookUpTable, table);
			}
		}
		
		return null;
	}
	
	static String listPattern = "(\\s*([a-zA-Z0-9_\\*]+(?:\\.\\*)?)\\s*,)*\\s*([a-zA-Z0-9_\\*]+(?:\\.\\*)?)";
	static String variablePattern = "\\s*([a-zA-Z0-9_\\*]+)\\s*";
	static String assignPattern = "\\s*([a-zA-Z0-9_]+)\\s*=\\s*(\\[?" + listPattern + "\\]?)|(?<=\\[)?(\\*)(?=\\])?";
	static String axisPattern =  "(ax)\\s" + assignPattern;
	static String vcPattern = "vc\\s" + variablePattern + "\\s*=\\s*\\{(.+),(.+),(.+)\\}";
	static String processPattern = "process\\((.+\\}),([^,]+),(.+)\\)";

	private static boolean isProcess(String input) {
		//process(argmin_{v1}, k=1, DEuclidean(f1,f2))
		Matcher match = Pattern.compile(processPattern).matcher(input);
		return match.find();
	}
	
	private static void handleProcess(String input, LookUpTable lookUpTable, ZQLTable table) {
		ZQLRow row = new ZQLRow();
		Processe process = row.getProcesse();
		Matcher match = Pattern.compile(processPattern).matcher(input);
		if (match.find()) {
			String arg = match.group(1);
			String pattern = "(argmax)\\s*=\\s*\\{("+listPattern+")\\}";
			Matcher argMatch = Pattern.compile(pattern).matcher(arg);
			String argType = null;
			List<String> values = null;
			if (argMatch.find()) {
				argType = argMatch.group(1);
				values = parseList(argMatch.group(2));
			} else {
				pattern = "(argmin)\\s*=\\s*\\{("+listPattern+")\\}";
				argMatch = Pattern.compile(pattern).matcher(arg);
				if (argMatch.find()) {
					argType = argMatch.group(1);
					values = parseList(argMatch.group(2));
				}
			}
			process.setMetric(argType); // sets argmin
			process.getAxisList1().addAll(values); // sets axis variable we iterate over argmin_{x1,y2}
			
			String count = match.group(2);
			pattern = "[a-zA-Z]+\\s*=\\s*([0-9]+)";
			Matcher countMatch = Pattern.compile(pattern).matcher(count);
			if (countMatch.find()) {
				String realCount = countMatch.group(1);
				process.setCount(realCount);
			}
			String method = match.group(3);
			pattern = "([a-zA-Z0-9_]+)\\(("+listPattern+")\\)";
			Matcher methodMatch = Pattern.compile(pattern).matcher(method);
			if (methodMatch.find()) {
				String methodName = methodMatch.group(1);
				String methodArgs = methodMatch.group(2);
				process.setMethod(methodName);
				process.getArguments().addAll(parseList(methodArgs));
			}
		}
	}
	
	private static boolean isVCAssignment(String input) {
        //String pattern = "vc\\s" + variablePattern + "\\s*=\\s*\\{(.+)\\}";
		Matcher match = Pattern.compile(vcPattern).matcher(input);
		return match.find();
	}
	
	private static void handleVCAssignment(String input, LookUpTable lookUpTable, ZQLTable table) {
		Matcher match = Pattern.compile(vcPattern).matcher(input);
		if (match.find()) {
			String variable = match.group(1);
			ZQLRow row = new ZQLRow();
			String x = match.group(2);
			handleX(x, lookUpTable,row);
			String y = match.group(3);
			handleY(y, lookUpTable,row);
			String z = match.group(4);
			handleZ(z, lookUpTable,row);
			table.getZqlRows().add(row);
		}
		
	}
	// TODO: add support for assignment and lists
	private static void handleX(String input, LookUpTable lookUpTable, ZQLRow row) {
		Matcher match = Pattern.compile(variablePattern).matcher(input);
		if (match.find()) {
			String variable = match.group(1);
			AxisVariable axisVar = (AxisVariable) lookUpTable.get(variable);
			if ( axisVar != null) {
				XColumn x = row.getX();
				x.setVariable(variable);
				x.setAttributes(axisVar.getValues());
			}
		}
	}
	
	// TODO: add support for assignment and lists
	private static void handleY(String input, LookUpTable lookUpTable, ZQLRow row) {
		Matcher match = Pattern.compile(variablePattern).matcher(input);
		if (match.find()) {
			String variable = match.group(1);
			AxisVariable axisVar = (AxisVariable) lookUpTable.get(variable);
			if ( axisVar != null) {
				YColumn y = row.getY();
				y.setVariable(variable);
				y.setAttributes(axisVar.getValues());
			}
		}
	}
	
	// TODO: add support for assignment and lists
	private static void handleZ(String input, LookUpTable lookUpTable, ZQLRow row) {
		Matcher match = Pattern.compile(variablePattern).matcher(input);
		if (match.find()) {
			String variable = match.group(1);
			AxisVariable axisVar = (AxisVariable) lookUpTable.get(variable);
			if ( axisVar != null) {
				ZColumn z = row.getZ();
				z.setVariable(variable);
				z.setAttribute(axisVar.getAttribute());
				z.setValues(axisVar.getValues());
			}
		}
	}
	
	
	private static boolean isAxisVariableAssignment(String input) {
		// pattern like ax v1 = [year, month] where RHS matches at least 1 char and optional brackets
		System.out.println("Checking AxisVariable for: " + input);
		//String pattern = "(ax)\\s([a-zA-Z0-9_]+)\\s*=\\s*(\\w+)";
		//String pattern = "(ax)\\s([a-zA-Z0-9_]+)\\s*=\\s*(\\[?" + listPattern + "\\]?)|(?<=\\[)?(\\*)(?=\\])?";
		Matcher match = Pattern.compile(axisPattern).matcher(input);	
		return match.find();
	}
	
	private static void handleAxisVariableAssignment(String input, LookUpTable lookUpTable) {
		//String pattern = "(ax)\\s([a-zA-Z0-9_]+)\\s*=\\s*(\\[?" + listPattern + "\\]?)|(?<=\\[)?(\\*)(?=\\])?";
		Matcher match = Pattern.compile(axisPattern).matcher(input);
		if (match.find()) {
			String variable = match.group(2);
			String rhs = match.group(3);
			System.out.println(rhs);
			System.out.println(match.group(7));
			List<String> values = new ArrayList<String>();
			values.add("*");
			AxisVariable axisVar = new AxisVariable("", "", values);
			// rhs is just * or [*]
			if(match.group(7) != null) {
				lookUpTable.put(variable, axisVar);
				return;
			}
			// normal case
			values = parseList(rhs);
			axisVar = new AxisVariable("", "", values);
			// check special case for z, such as state.*
			// (doesn't support [state.*, product.*] currently)
			String pattern = "([a-zA-Z0-9_]+)\\.\\*";
			match = Pattern.compile(pattern).matcher(values.get(0));
			if (match.find()) {
				values = new ArrayList<String>();
				values.add("*");
				System.out.println(match.group(1));
				axisVar = new AxisVariable("Z", match.group(1), values);
			}
			lookUpTable.put(variable, axisVar);
		}
	}
	private static List<String> parseList(String input) {
		List<String> res = null;
		// matches [CA] or [CA,NY,...] or [state.*] CA or CA,NY 
		String pattern = "(?<=\\[)?" + listPattern + "(?=\\])?";
		Matcher match = Pattern.compile(pattern).matcher(input);
		if (match.find()) {
			String list = match.group(); // of format CA, NY, LA
			res = new ArrayList<String>(Arrays.asList(list.replaceAll(" ",  "").split("\\s*,\\s*")));
		}
		System.out.println(res);
		return res;
	}
}