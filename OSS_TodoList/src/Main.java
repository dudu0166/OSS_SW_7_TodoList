
public class Main {

	public static void main(String[] args) {
		Controller controller = new Controller();

		while (controller.isRunning()) {
			String command = controller.inputMainCommand();
			switch (command) {
			case "a":
				controller.addList();
				break;
			case "l":
				controller.chooseShowOption();
				break;
			case "o":
				controller.setOrderOption();
				break;
			case "t":
				controller.tagSettings();
				break;
			case "q":
				controller.end();
				break;
			default:
				controller.others(command);
				break;
			}
		}
	}
}
