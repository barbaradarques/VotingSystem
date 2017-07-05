package global;

public class Candidate implements Cloneable {

//    int código_votacao;
//    String nome_candidato;
//    String partido;
//    int num_votos;
	
	public int code;
	public String name;
	public String party;
	public int num_votes;
	
	public Candidate(){
	}
	
	public Candidate(int code, String name, String party, int num_votes){
		this.code = code;
		this.name = name;
		this.party = party;
		this.num_votes = num_votes;
	}
	
	public void addVotes(int numNewVotes){
		num_votes += numNewVotes;
	}
	
	@Override
	public String toString() {
		return name + " | Código: " + code + " Partido: " + party 
				+ " Número de Votos: " + num_votes;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}
}
