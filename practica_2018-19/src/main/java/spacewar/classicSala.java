package spacewar;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class classicSala extends SalaObject {

	private final int X_BOUNDS = 1280;
	private final int Y_BOUNDS = 720;
	private final int VIDAS_CLASICO = 3;
	private final int SALUD_CLASICO = 100;
	private final int MUNICION_INICIAL = 30;
	private final int FUEL_INICIAL=100;
	private final int tiempo_entre_municion = 350;
	private int tiempo_hasta_municion = 0;
	private final int MAXIMA_MUNICION = 20;
	
	private final int FPS = 30;
	private final int TICK_DELAY = 1000 / FPS;

	public classicSala(int NJUGADORES, String MODOJUEGO, String NOMBRE, Player creador, int INDICE) {
		super(NJUGADORES, MODOJUEGO, NOMBRE, creador, INDICE);
	}

	@Override
	public void startGameLoop() {

		this.setInProgress(true);
		ObjectNode json = mapper.createObjectNode();

		json.put("event", "START GAME");
		json.put("x_bounds", X_BOUNDS);
		json.put("y_bounds", Y_BOUNDS);
		json.put("municion", MUNICION_INICIAL);

		String message = json.toString();

		for (Player player : getPlayers()) {
			player.incrementPartidasJugadas();
			player.setPosition((Math.random() * (X_BOUNDS * 0.85)) + (X_BOUNDS * 0.15),
					(Math.random() * (Y_BOUNDS * 0.85)) + (Y_BOUNDS * 0.15));
			player.setFuel(FUEL_INICIAL);
			player.setVidas(VIDAS_CLASICO);
			player.setSalud(SALUD_CLASICO);
			player.setMunicion(MUNICION_INICIAL);
			player.setInMatch(true);
			try {

				player.getSession().sendMessage(new TextMessage(message.toString()));

			} catch (Throwable ex) {
				System.err.println("Execption sending message to player " + player.getSession().getId());
				ex.printStackTrace(System.err);
				this.removePlayer(player);
			}
		}

		setScheduler(Executors.newScheduledThreadPool(1));
		getScheduler().scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stopGameLoop() {
		if (getScheduler() != null) {
			getScheduler().shutdown();
		}
	}

	

	@Override
	public void tick() {
		ObjectNode json = mapper.createObjectNode();
		ArrayNode arrayNodePlayers = mapper.createArrayNode();
		ArrayNode arrayNodeProjectiles = mapper.createArrayNode();
		ArrayNode arrayNodeMuniciones = mapper.createArrayNode();

		long thisInstant = System.currentTimeMillis();
		Set<Integer> bullets2Remove = new HashSet<>();
		Set<Integer> municion2Remove = new HashSet<>();
		boolean removeMunicion = false;
		boolean removeBullets = false;

		try {
			// Update players
			for (Player player : getPlayers()) {
				
				player.calculateMovement(X_BOUNDS, Y_BOUNDS,0,0);
				
				ObjectNode jsonPlayer = mapper.createObjectNode();
				jsonPlayer.put("id", player.getPlayerId());
				jsonPlayer.put("shipType", player.getShipType());
				jsonPlayer.put("municion", player.getMunicion());
				jsonPlayer.put("fuel", player.getFuel());
				jsonPlayer.put("nombre", player.getNombre());

				if (player.getSalud() <= 0) {
					player.setVidas(player.getVidas() - 1);
					if (player.getVidas() <= 0) { // Otra manera para entrar en arrayNodePlayers funcionaría mejor?
						endGame(player,false);
						removePlayer(player);
					} else {
						player.setSalud(SALUD_CLASICO);
						player.setPosition((Math.random() * (X_BOUNDS * 0.85)) + (X_BOUNDS * 0.15),
								(Math.random() * (Y_BOUNDS * 0.85)) + (Y_BOUNDS * 0.15));
						jsonPlayer.put("posX", player.getPosX());
						jsonPlayer.put("posY", player.getPosY());
						jsonPlayer.put("facingAngle", player.getFacingAngle());
					}

				} else {
					jsonPlayer.put("posX", player.getPosX());
					jsonPlayer.put("posY", player.getPosY());
					jsonPlayer.put("facingAngle", player.getFacingAngle());
				}

				jsonPlayer.put("vida", player.getSalud());
				arrayNodePlayers.addPOJO(jsonPlayer);
			}

			// Update bullets and handle collision
			for (Projectile projectile : getProjectiles().values()) {
				projectile.applyVelocity2Position();

				// Handle collision
				for (Player player : getPlayers()) {
					if ((projectile.getOwner().getPlayerId() != player.getPlayerId()) && player.intersect(projectile)) {
						projectile.getOwner().incrementDisparosAcertados();
						player.setSalud(player.getSalud() - projectile.getDamage());
						projectile.setHit(true);
						break;
					}
				}

				ObjectNode jsonProjectile = mapper.createObjectNode();
				jsonProjectile.put("id", projectile.getId());

				if (!projectile.isHit() && projectile.isAlive(thisInstant)) {
					jsonProjectile.put("posX", projectile.getPosX());
					jsonProjectile.put("posY", projectile.getPosY());
					jsonProjectile.put("facingAngle", projectile.getFacingAngle());
					jsonProjectile.put("isAlive", true);
				} else {
					removeBullets = true;
					bullets2Remove.add(projectile.getId());
					jsonProjectile.put("isAlive", false);
					if (projectile.isHit()) {
						jsonProjectile.put("isHit", true);
						jsonProjectile.put("posX", projectile.getPosX());
						jsonProjectile.put("posY", projectile.getPosY());
					}
				}
				arrayNodeProjectiles.addPOJO(jsonProjectile);
			}

			if (removeBullets)
				this.getProjectiles().keySet().removeAll(bullets2Remove);

			// para eliminar la municion recogida
			for(Municion munition : getMuniciones().values()) {
				for(Player player : getPlayers()) {
					if(!munition.getIsHit() &&player.intersect(munition)) {
						munition.setIsHit(true);
						//Si al recoger la municion, tendría una mayor munición de la máxima, se le asigna la máxima, si no se suma el valor
						player.setMunicion(player.getMunicion()+MAXIMA_MUNICION > MUNICION_INICIAL ? MUNICION_INICIAL : player.getMunicion()+MAXIMA_MUNICION );
						break;
					}
				}

				ObjectNode jsonMunicion = mapper.createObjectNode();
				jsonMunicion.put("id", munition.getId());
				jsonMunicion.put("posX", munition.getPosX());
				jsonMunicion.put("posY", munition.getPosY());
				if(!munition.getIsHit()) {
					jsonMunicion.put("isAlive", true);
					arrayNodeMuniciones.addPOJO(jsonMunicion);
				}else {
					removeMunicion=true;
					municion2Remove.add(munition.getId());
					jsonMunicion.put("isAlive", false);
				}

				arrayNodeMuniciones.addPOJO(jsonMunicion);
			}

			if (removeMunicion) {
				this.getMuniciones().keySet().removeAll(municion2Remove);
			}

			tiempo_hasta_municion += (FPS / 10);
			// Creamos la municion, posicionamos en el mapa y comprobamos quien la coje
			if (tiempo_hasta_municion >= tiempo_entre_municion && obtenerIdMunicion() < MAXIMA_MUNICION) {
				tiempo_hasta_municion=0;
				Municion municion = new Municion(obtenerIdMunicionYSumar());
				municion.setPosition((Math.random() * (X_BOUNDS * 0.85)) + (X_BOUNDS * 0.15),
						(Math.random() * (Y_BOUNDS * 0.85)) + (Y_BOUNDS * 0.15));
				addMunicion(municion.getId(), municion);
			}
			
			json.put("event", "GAME STATE UPDATE");
			json.putPOJO("players", arrayNodePlayers);
			json.putPOJO("projectiles", arrayNodeProjectiles);
			json.putPOJO("municiones", arrayNodeMuniciones);

			this.broadcast(json.toString());
		} catch (Throwable ex) {

		}
	}

}
