package spacewar;

public class Municion extends SpaceObject {
	
	private final int id;
	private boolean isHit;
	
	public Municion (int id) {
		this.id=id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setIsHit(boolean isHit) {
		this.isHit=isHit;
	}
	
	public boolean getIsHit() {
		return this.isHit;
	}
}
