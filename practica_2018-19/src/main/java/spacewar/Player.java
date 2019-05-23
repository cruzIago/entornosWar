package spacewar;

import java.util.Random;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player extends Spaceship {

	private final WebSocketSession session;
	private final int playerId;
	private String nombre;
	private final String shipType;
	private boolean inMatch;
	private int salaId;

	// Variables Ingame
	private int salud;
	private int municion;
	private int vidas;
	private int fuel;

	public Player(int playerId, WebSocketSession session) {
		this.playerId = playerId;
		this.session = session;
		this.shipType = this.getRandomShipType();
		this.inMatch = false;
	}

	public int getSalud() {
		return this.salud;
	}

	public void setSalud(int salud) {
		this.salud = salud;
	}

	public void setFuel(int fuel) {
		this.fuel = fuel;
	}

	public int getFuel() {
		return this.fuel;
	}

	public int getMunicion() {
		return this.municion;
	}

	public void setMunicion(int municion) {
		this.municion = municion;
	}

	public int getVidas() {
		return vidas;
	}

	public void setVidas(int vidas) {
		this.vidas = vidas;
	}

	public int getPlayerId() {
		return this.playerId;
	}

	public int getSala() {
		return salaId;
	}

	public void setSala(int salaId) {
		this.salaId = salaId;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public WebSocketSession getSession() {
		return this.session;
	}

	public void sendMessage(String msg) throws Exception {
		this.session.sendMessage(new TextMessage(msg));
	}

	public String getShipType() {
		return shipType;
	}

	public boolean getInMatch() {
		return inMatch;
	}

	public void setInMatch(boolean inMatch) {
		this.inMatch = inMatch;
	}

	private String getRandomShipType() {
		String[] randomShips = { "blue", "darkgrey", "green", "metalic", "orange", "purple", "red" };
		String ship = (randomShips[new Random().nextInt(randomShips.length)]);
		ship += "_0" + (new Random().nextInt(5) + 1) + ".png";
		return ship;
	}

}
