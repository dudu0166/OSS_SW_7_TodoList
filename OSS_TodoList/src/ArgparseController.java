import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class ArgparseController {
	ArgparseController(String args[],View view) {
		ArgumentParser parser = ArgumentParsers.newFor("OSS_TodoList").build().
				defaultHelp(true)
				.usage("${prog} [OPTIONS]")
				.description("It is a program to manage the schedule.")
				.version("${prog} 0.0.1");

		parser.addArgument("--version").action(Arguments.version());
		
		parser.addArgument("-t", "--today")
		.dest("today")
		.help(" ")
		.action(Arguments.storeTrue());

		
		parser.addArgument("-a", "--all")
		.dest("all")
		.help(" ")
		.action(Arguments.storeTrue());

		
		parser.addArgument("-w", "--week")
		.dest("week")
		.help("")
		.action(Arguments.storeTrue());

		
		parser.addArgument("-m", "--month")
		.dest("month")
		.help("")
		.metavar("Month")
			.type(Integer.class)
				.choices(Arguments.range(1, 12));

//		parser.addArgument("--add")
//		.dest("add")
//		.nargs("+")
//		.help("Input your todo (What Due Priority Tags...)"
//		+System.lineSeparator()
//		+"ex) TestTodo 2018-06-07 00:00:00 1 Life School...")
//		.metavar("Info","What","Due","Priority","Tags");

		
		try {
			Namespace ns = parser.parseArgs(args);
			
			if((boolean)ns.getBoolean("today")) {
				view.optionalList("l -t");
			}else if((boolean)ns.getBoolean("all")) {
				view.optionalList("l -a");
			}else if((boolean)ns.getBoolean("week")) {
				view.optionalList("l -w");
			}else if(ns.getInt("month") != null) {
				view.optionalList("l -"+(ns.getInt("month")));
			}
//				else if(((List<Object>)ns.getList("add")) != null) {
//				
//			}
			
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

	}
}
