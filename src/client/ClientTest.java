package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import global.Candidate;

public class ClientTest {
	public static Socket serverSocket;
	public static BufferedReader serverReader; // usado para receber mensagens
												// do cliente
	public static PrintWriter serverWriter; // usado para mandar mensagens para
											// o cliente
	public static List<Candidate> localVotes;
	
	public static void registerVote(int code){
		for(int i = 0; i < localVotes.size(); ++i){
			if(localVotes.get(i).code == code){
				localVotes.get(i).addVotes(1);
				System.out.println("Voto realizado com sucesso!");
				return;
			}
		}
		System.out.println("Candidato não encontrado. Tente novamente.");
	}
	
	public static void main(String[] args) {
		boolean neverConnected = true;
		Scanner scan = null;
		try {
			serverSocket = new Socket("localhost", 1234);
			neverConnected = false;
			serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
			scan = new Scanner(System.in);
			
			boolean isConnected = true;
			boolean loadedCandidates = false;
			while(isConnected){
				System.out.print("\nEscolha uma operação: "
						+ "\n [1] Votar em um candidato"
						+ "\n [2] Votar nulo"
						+ "\n [3] Votar em branco"
						+ "\n [999] Carregar lista de candidatos"
						+ "\n [888] Enviar votos ao servidor"
						+ "\n>> ");
				int opcode = scan.nextInt();
				switch(opcode){
					case 1: // votar em alguém
						// <<<<<<<< listar os candidatos
						if(!loadedCandidates){
							System.out.println("Não e possível votar, pois não há candidatos registrados no sistema.");
							continue;
						}
						System.out.println("Insira o código do seu candidato:");
						int candidate = scan.nextInt();
						registerVote(candidate);
						break;
					case 2: // votar nulo
						if(!loadedCandidates){
							System.out.println("Não e possível votar, pois não há candidatos registrados no sistema.");
							continue;
						}
						registerVote(0);
						
						break;
					case 3: // votar em branco
						if(!loadedCandidates){
							System.out.println("Nao e possivel votar, pois nao ha candidatos registrados no sistema.");
							continue;
						}
						registerVote(1);
						break;
					case 999: // pedir candidatos ao servidor
						serverWriter.println("999");
						System.out.println("Pedido de candidatos enviados ao servidor!");
						Type CANDIDATES_TYPE = new TypeToken<List<Candidate>>(){}.getType();
						Gson gson = new Gson();
						String noVotesJSON = serverReader.readLine();
						localVotes = gson.fromJson(noVotesJSON, CANDIDATES_TYPE);
						System.out.println("Lista de candidatos recebida com sucesso!");
						System.out.println("recebido do servidor: " + noVotesJSON); // <<
						loadedCandidates = true;
						break;
					case 888: // avisar ao servidor que vai enviar votos e enviá-los
						if(!loadedCandidates){
							System.out.println("Nao e possivel enviar os votos, pois nao ha candidatos registrados no sistema.");
							continue;
						}
						serverWriter.println("888");
						System.out.println("Aviso de envio enviado!");
						gson = new Gson();  // <<<<<<<<<, ver isso
						String localVotesJSON = gson.toJson(localVotes);  
						System.out.println("o que vou enviar pro servidor: " + localVotesJSON); // <<
						serverWriter.println(localVotesJSON);
						System.out.println("Votos enviados com sucesso!");
						isConnected = false;
						break;
					default:
						System.out.println("Operacao invalida!");
				}
				
			}
			
		} catch (IOException e) {
			if(neverConnected){
				System.out.println("Não foi possível conectar ao servidor!");
			} else {
				System.out.println("A conexão com o servidor caiu.");
			}
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
				scan.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Aplicação do cliente encerrada!");
	}

}
