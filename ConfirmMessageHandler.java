package main;

import java.io.IOException;

public class ConfirmMessageHandler extends Thread {
	
	private String checking;
	private FileHandler confirm_socket;
	
	public ConfirmMessageHandler(String checking, FileHandler confirm_socket) {
		this.checking = checking;
		this.confirm_socket = confirm_socket;
	}
	
	public void run() {
		if(!checking.equals(" "))
			try {
				confirm_socket.sendConfirmation(this.checking);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
