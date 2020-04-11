package communication;

public class PortsManagement {
	private int nbEmission;
    private int nbDestination;
    
	public PortsManagement() {
		super();
		nbEmission=0;
		nbDestination=0;
	}
	
	public int getNbEmission() {
		return nbEmission;
	}
	
	public void setNbEmission() {
		this.nbEmission++;
	}
	
	public int getNbDestination() {
		return nbDestination;
	}

	public void setNbDestination() {
		this.nbDestination++;
	}
    
}
