package communication;

public class PortsManagement {
	// Declaration des variables
	private int nbEmission;
    private int nbDestination;
    
	public PortsManagement() {
		super();
		nbEmission=0;
		nbDestination=0;
	}
	
	// Methode permettant de retourner le nombre d'emission
	public int getNbEmission() {
		return nbEmission;
	}
	
	// Methode permettant de modifier le nombre d'emission
	public void setNbEmission() {
		this.nbEmission++;
	}
	
	// Methode permettant de retourner le nombre de reception
	public int getNbDestination() {
		return nbDestination;
	}
	
	// Methode permettant de modifier le nombre de reception
	public void setNbDestination() {
		this.nbDestination++;
	}    
}
