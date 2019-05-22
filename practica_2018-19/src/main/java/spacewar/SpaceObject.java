package spacewar;

public class SpaceObject {

	private int collisionFactor;

	private double posX, posY, velX, velY, facingAngle;

	private double ancho, alto;

	public double getAncho() {
		return this.ancho;
	}

	public double getAlto() {
		return this.alto;
	}

	public void setAncho(double ancho) {
		this.ancho = ancho;
	}

	public void setAlto(double alto) {
		this.alto = alto;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getFacingAngle() {
		return this.facingAngle;
	}

	public void setFacingAngle(double facingAngle) {
		this.facingAngle = facingAngle;
	}

	public void incFacingAngle(double rotSpeed) {
		this.facingAngle += rotSpeed;
	}

	public void setPosition(double x, double y) {
		this.posX = x;
		this.posY = y;
	}

	public void setVelocity(double x, double y) {
		this.velX = x;
		this.velY = y;
	}

	public void incVelocity(double velX, double velY) {
		this.velX += velX;
		this.velY += velY;
	}

	public void multVelocity(double delta) {
		this.velX *= delta;
		this.velY *= delta;
	}

	// Usada para los jugadores
	public void applyVelocity2Position(int xBound, int yBound) {
		this.posX += this.velX;
		this.posY += this.velY;

		if (posX - 24 < 0) {
			this.velX = -velX;
			this.posX = 24;
		} else if (posX + 24 > xBound) {
			this.velX = -velX;
			this.posX = xBound - 24;
		}
		if (posY - 24 < 0) {
			this.velY = -velY;
			this.posY = 24;
		} else if (posY + 24 > xBound) {
			this.velY = -velY;
			this.posY = xBound - 24;
		}

	}

	// Usada para los proyectiles
	public void applyVelocity2Position() {
		this.posX += this.velX;
		this.posY += this.velY;
	}

	public int getCollisionFactor() {
		return collisionFactor;
	}

	public void setCollisionFactor(int radius) {
		this.collisionFactor = radius;
	}

	// Handle collision
	public boolean intersect(SpaceObject other) {
		int maxRadiusToCollide = this.collisionFactor + other.getCollisionFactor();
		double x = this.posX - other.getPosX();
		double y = this.posY - other.getPosY();
		return (maxRadiusToCollide > (Math.pow(x, 2) + Math.pow(y, 2)));
	}
}
