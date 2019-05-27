package spacewar;

public class Municion extends SpaceObject {
	
	private final int id;
	private boolean isHit;
	private final int MUNITION_COLLISION_FACTOR=200;
	
	public Municion (int id) {
		this.setCollisionFactor(MUNITION_COLLISION_FACTOR);
		this.id=id;
	}
	
	public int getId() {
		return this.id%10;
	}
	
	public void setIsHit(boolean isHit) {
		this.isHit=isHit;
	}
	
	public boolean getIsHit() {
		return this.isHit;
	}
}
