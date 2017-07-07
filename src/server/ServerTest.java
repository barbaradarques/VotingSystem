package server;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import global.Candidate;

public class ServerTest implements Runnable {
	public static ServerSocket serverSocket;
	public static volatile boolean isUp;
	public static List<Candidate> candidates;
	public static String noVotesJSON; // JSON da lista de candidatos com votos zerados
	public static final String JSONfilename = "votes.json";
	
	
	public static String candidates2json(){
		Gson gson = new Gson();  
		return gson.toJson(candidates);  
	}
	
	public static void candidates2file(){
		try (Writer writer = new FileWriter(JSONfilename)) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(candidates, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void loadCandidates(){
		Type CANDIDATES_TYPE = new TypeToken<List<Candidate>>(){}.getType();
		Gson gson = new Gson();
		JsonReader reader;
		try {
		
			reader = new JsonReader(new FileReader(JSONfilename));
			candidates = gson.fromJson(reader, CANDIDATES_TYPE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
	public static synchronized void registerVotes(String clientJSON){
		Type CANDIDATES_TYPE = new TypeToken<List<Candidate>>(){}.getType();
		Gson gson = new Gson();
		List<Candidate> listFromClient = gson.fromJson(clientJSON, CANDIDATES_TYPE);
		
		for(int i = 0; i < candidates.size(); ++i){
			candidates.get(i).addVotes(listFromClient.get(i).num_votes);
		}
		printCandidates();
		candidates2file();
	}
	
	public static void printCandidates(){
		candidates.forEach(System.out::println);
	}
	
	public static void genNoVotesJSON() throws CloneNotSupportedException{
		List<Candidate> noVotes = new ArrayList<Candidate>(candidates.size());

		for (Candidate cand: candidates) {
			noVotes.add((Candidate)cand.clone());
		}
		
		noVotes.forEach(cand-> cand.num_votes=0);
		Gson gson = new Gson();  
		noVotesJSON =  gson.toJson(noVotes); 		
	}
	
	public static void main(String args[]) {
		loadCandidates();

		try {
			genNoVotesJSON();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
		
		isUp = false;
		try {
			serverSocket = new ServerSocket(40011);
			isUp = true;
			System.out.println("Esperando conexoes...");
			Thread inputThread = new Thread(new ServerTest());
			inputThread.setDaemon(true);
			inputThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		while (isUp) {
			try {
				Socket clientSocket = serverSocket.accept();
				ClientThread clientThread = new ClientThread(clientSocket, noVotesJSON); 
				clientThread.start(); 
			} catch (IOException ioe) {
				if(isUp) // se o servidor tiver sido derrubado de proposito, nao precisa imprimir
					ioe.printStackTrace();
			}
		}
		
		System.out.println("Servidor encerrado.");
	}

	@Override
	public void run() {
		try (Scanner scan = new Scanner(System.in)) {
			while (!scan.nextLine().equals("stop")); 
			isUp = false;
		}
		try {
			serverSocket.close(); 
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}
}
