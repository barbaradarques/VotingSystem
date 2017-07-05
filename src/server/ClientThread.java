package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


class ClientThread extends Thread {
	Socket clientSocket;
	boolean isConnected;
	String noVotesJSON;

	public ClientThread() {
		super();
	}

	ClientThread(Socket s, String noVotesJSON) {
		clientSocket = s;
		isConnected = true;
		this.noVotesJSON = noVotesJSON;
	}
	
	
	public void run() {
		BufferedReader clientReader = null; // usado para receber mensagens do cliente
		PrintWriter clientWriter = null; // usado para escrever mensagens para o cliente
		String clientAddress = clientSocket.getInetAddress().getHostName();
		System.out.println("Novo cliente conectado: " + clientAddress);
		try {
			clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			clientWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

			while(isConnected){
				String opcode = clientReader.readLine();
				switch(opcode){
					case "999": // pedido de envio de candidatos
						System.out.println("Pedido de envio de candidatos recebido!");
						System.out.println("o que vou enviar ao cliente: " + noVotesJSON); // <<<
						clientWriter.println(noVotesJSON);
						clientWriter.flush(); // <<< mudar arg pra true
						break;
					case "888": // aviso de envio de votos
						System.out.println("Aviso de envio de votos recebido!");
						String newVotesJSON = clientReader.readLine();
						System.out.println("o que eu recebi do cliente: " + newVotesJSON); // <<<
						ServerTest.registerVotes(newVotesJSON);
						isConnected = false;
						break;
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				clientReader.close();
				clientWriter.close();
				clientSocket.close();
				System.out.println("Cliente desconectado: " + clientAddress);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}