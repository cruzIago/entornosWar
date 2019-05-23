package spacewar;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class royaleSala extends SalaObject {

	private final int X_BOUNDS = 4000;
	private final int Y_BOUNDS = 4000;
	private final int VIDAS_ROYALE = 1;

	private final int FPS = 30;
	private final int TICK_DELAY = 1000 / FPS;

	private double borde_x;
	private double borde_y;
	private final int TIEMPO_CIERRE = 1;
	private double tiempo_entre_cierre = 0;

	public royaleSala(int NJUGADORES, String MODOJUEGO, String NOMBRE, Player creador) {
		super(NJUGADORES, MODOJUEGO, NOMBRE, creador);
		borde_x = X_BOUNDS;
		borde_y = Y_BOUNDS;
	}

	@Override
	public void startGameLoop() {
		ObjectNode json = mapper.createObjectNode();
		
		json.put("event", "START GAME");
		json.put("x_bounds", X_BOUNDS);
		json.put("y_bounds", Y_BOUNDS);
		
		String message = json.toString();
		
		for (Player player : getPlayers()) {
			player.setVidas(VIDAS_ROYALE);
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

		long thisInstant = System.currentTimeMillis();
		Set<Integer> bullets2Remove = new HashSet<>();
		boolean removeBullets = false;

		try {
			// Update players
			for (Player player : getPlayers()) {
				ObjectNode jsonPlayer = mapper.createObjectNode();
				jsonPlayer.put("id", player.getPlayerId());
				jsonPlayer.put("shipType", player.getShipType());
				jsonPlayer.put("posX", player.getPosX());
				jsonPlayer.put("posY", player.getPosY());
				jsonPlayer.put("facingAngle", player.getFacingAngle());
				arrayNodePlayers.addPOJO(jsonPlayer);

			}

			// Update bullets and handle collision
			for (Projectile projectile : getProjectiles().values()) {
				projectile.applyVelocity2Position();

				// Handle collision
				for (Player player : getPlayers()) {
					if ((projectile.getOwner().getPlayerId() != player.getPlayerId()) && player.intersect(projectile)) {
						player.setSalud(player.getSalud() - projectile.getDamage());
						if (player.getSalud() <= 0) {
							if (player.getVidas() <= 0) { // Otra manera para entrar en arrayNodePlayers funcionaría
															// mejor?
								removePlayer(player);
							}
						}
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
			tiempo_entre_cierre += TIEMPO_CIERRE;

			if (tiempo_entre_cierre >= TICK_DELAY) {
				borde_x = borde_x * 0.99;
				borde_y = borde_y * 0.99;
				tiempo_entre_cierre = 0;
			}

			json.put("event", "GAME STATE UPDATE");
			json.putPOJO("players", arrayNodePlayers);
			json.putPOJO("projectiles", arrayNodeProjectiles);
			json.put("circle_x", borde_x);
			json.put("circle_y", borde_y);

			this.broadcast(json.toString());
		} catch (Throwable ex) {

		}
	}
}
