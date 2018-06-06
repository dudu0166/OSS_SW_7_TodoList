
public class Main {

	public static void main(String[] args) {
		

		View view = new View();
		
		if(args.length >=1) {//메인 프로그램 실행
			new ArgparseController(args,view);
		}else {//argparse4j를 통한 메소드 호출
			boolean run = true;
			view.initialScreen();
			
			while (run) {
				String command = view.inputCommand("Choose what to do.\n"
						+ "(a: Add todo, l: List todo, m: modify contents, r: Remove  todo, o: Order option, t: Tag settings, q: Quit)\n: ");
				switch (command) {
				case "a":
					view.inputForAdd();
					break;
				case "l":
					view.chooseShowOption();
					break;
				case "m":
					view.inputToModify();
					break;
				case "r":
					view.inputToRemove();
					break;
				case "o":
					view.setOrderOption();
					break;
				case "t":
					view.tagSettings();
					break;
				case "q":
					view.exit();
					run = false;
					break;
				default:
					view.optionalList(command);
					break;
				}
			}
		}
		
	}
}
