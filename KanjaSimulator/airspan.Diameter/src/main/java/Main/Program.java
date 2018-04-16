package Main;

import Controller.Controller;

public class Program {

	public static void main(String[] args) {
		Controller controller = Controller.getInstance();
		controller.init();
	}
}