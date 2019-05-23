package spacewar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SalaObject {
	private CyclicBarrier nPlayers;
	private Map<String, Player> playersSala = new ConcurrentHashMap<>();
	private Runnable ePartida = () -> startGameLoop();
	private final String MODOJUEGO;
	private final String NOMBRE;
	private final String CREADOR;
	private final static int FPS = 30;
	private final static long TICK_DELAY = 1000 / FPS;
	private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>(); //Protected para que los hijos puedan leer de ella

	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


	public SalaObject(int NJUGADORES, String MODOJUEGO, String NOMBRE, Player creador) {
		this.nPlayers = new CyclicBarrier(NJUGADORES, ePartida);
		this.MODOJUEGO = MODOJUEGO;
		this.NOMBRE = NOMBRE;
		this.CREADOR = creador.getNombre();
	}

	public String getModoJuego() {
		return MODOJUEGO;
	}

	public String getCreador() {
		return CREADOR;
	}

	public synchronized int getNumberPlayersWaiting() {
		return nPlayers.getNumberWaiting();
	}

	public void joinSala(Player player) {
		if (!nPlayers.isBroken()) {
			try {
				playersSala.put(player.getSession().getId(), player);
				nPlayers.await();
			} catch (InterruptedException e) {
				if (!this.CREADOR.equals(player.getNombre())) {
					playersSala.remove(player.getSession().getId());
				} else {
					removePlayer(player);
					nPlayers.reset();
				}
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				removePlayer(player);
			} finally {
			}
		}
	}

	public void removePlayer(Player player) {
		playersSala.remove(player.getSession().getId());

		if (playersSala.size() == 0) {
			this.stopGameLoop();
		}
	}

	public Collection<Player> getPlayers() {
		return playersSala.values();
	}

	public String getNombreSala() {
		return NOMBRE;
	}

	// gestion game loop
	public void addProjectile(int id, Projectile projectile) {
		projectiles.put(id, projectile);
	}

	public Map<Integer, Projectile> getProjectiles() {
		return this.projectiles;
	}

	public void removeProjectile(Projectile projectile) {
		playersSala.remove(projectile.getId(), projectile);
	}

	public void startGameLoop() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopGameLoop() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}

	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler=scheduler;
	}
	public ScheduledExecutorService getScheduler() {
		return this.scheduler;
	}
	
	public void broadcast(String message) {
		for (Player player : getPlayers()) {
			try {
				if (player.getInMatch()) {
					player.getSession().sendMessage(new TextMessage(message.toString()));
				}
			} catch (Throwable ex) {
				System.err.println("Execption sending message to player " + player.getSession().getId());
				ex.printStackTrace(System.err);
				this.removePlayer(player);
			}
		}
	}

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
							removePlayer(player);
						}
						
						// System.out.println("Player " + player.getPlayerId() + " was hit!!!");
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
				this.projectiles.keySet().removeAll(bullets2Remove);

			json.put("event", "GAME STATE UPDATE");
			json.putPOJO("players", arrayNodePlayers);
			json.putPOJO("projectiles", arrayNodeProjectiles);

			this.broadcast(json.toString());
		} catch (Throwable ex) {

		}
	}
}
