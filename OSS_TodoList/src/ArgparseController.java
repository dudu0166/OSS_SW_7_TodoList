import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ArgparseController {
	ArgparseController(String args[], View view) {
		ArgumentParser parser = ArgumentParsers.newFor("OSS_TodoList").build().defaultHelp(true)
				.usage("${prog} [OPTIONS]").description("It is a program to manage the schedule.")
				.version("${prog} 0.7.2");

		parser.addArgument("--version").action(Arguments.version());
    
		parser.addArgument("-t", "--today")
		.dest("today")
		.help("Yon can see your schedule that you need to do today.")
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

		parser.addArgument("--add")
		.dest("add")
		.help("")
		.action(Arguments.storeTrue());

		parser.addArgument("--list")
		.dest("list")
		.help("")
		.action(Arguments.storeTrue());

		parser.addArgument("--modify")
		.dest("modify")
		.help("")
		.action(Arguments.storeTrue());

		parser.addArgument("--remove")
		.dest("remove")
		.help("")
		.action(Arguments.storeTrue());

		parser.addArgument("--setOrder")
		.dest("order")
		.help("")
		.action(Arguments.storeTrue());

		parser.addArgument("--setTag")
		.dest("tag")
		.help("")
		.action(Arguments.storeTrue());

		try {
			Namespace ns = parser.parseArgs(args);

			if (ns.getBoolean("today")) {
				view.optionalList("l -t");
			} 
			else if (ns.getBoolean("all")) {
				view.optionalList("l -a");
			} 
			else if (ns.getBoolean("week")) {
				view.optionalList("l -w");
			}
			else if (ns.getInt("month") != null) {
				view.optionalList("l -" + (ns.getInt("month")));
			}
			else if (ns.getBoolean("add") != null) {
				view.inputForAdd();
			}
			else if (ns.getBoolean("list") != null) {
				view.chooseShowOption();
			}
			else if (ns.getBoolean("modify") != null) {
				view.inputToModify();
			}
			else if (ns.getBoolean("remove") != null) {
				view.inputToRemove();
			}
			else if (ns.getBoolean("order") != null) {
				view.setOrderOption();
			}
			else if (ns.getBoolean("tag") != null) {
				view.tagSettings();
			}

		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

	}
}
