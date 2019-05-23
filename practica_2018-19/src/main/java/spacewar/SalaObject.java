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
	private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>();
	private int xBound;
	private int yBound;

	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public SalaObject(int NJUGADORES, String MODOJUEGO, String NOMBRE, String CREADOR) {
		this.nPlayers = new CyclicBarrier(NJUGADORES, ePartida);
		this.MODOJUEGO = MODOJUEGO;
		this.NOMBRE = NOMBRE;
		this.CREADOR = CREADOR;
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
		try {
			playersSala.put(player.getSession().getId(), player);
			nPlayers.await();
		} catch (InterruptedException e) {
			playersSala.remove(player);
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
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

	// gestion salas
	public void setGame(String kind) {
		switch (kind) {
		case "VERSUS":
			xBound = 1280;
			yBound = 1280;
			break;
		case "ROYALE":
			xBound = 4800;
			yBound = 4800;
			break;
		default:
			break;
		}
	}

	public int getXBound() {
		return this.xBound;
	}

	public int getYBound() {
		return this.yBound;
	}

	// gestion game loop
	public void addProjectile(int id, Projectile projectile) {
		projectiles.put(id, projectile);
	}
	
	public Collection<Projectile> getProjectiles() {
		return projectiles.values();
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

	private void broadcast(String message) {
		for (Player player : getPlayers()) {
			try {
				if (!player.getInMatch()) {
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
				player.calculateMovement(xBound, yBound);

				ObjectNode jsonPlayer = mapper.createObjectNode();
				jsonPlayer.put("id", player.getPlayerId());
				jsonPlayer.put("shipType", player.getShipType());
				jsonPlayer.put("posX", player.getPosX());
				jsonPlayer.put("posY", player.getPosY());
				jsonPlayer.put("facingAngle", player.getFacingAngle());
				arrayNodePlayers.addPOJO(jsonPlayer);
			}

			// Update bullets and handle collision
			for (Projectile projectile : getProjectiles()) {
				projectile.applyVelocity2Position();

				// Handle collision
				for (Player player : getPlayers()) {
					if ((projectile.getOwner().getPlayerId() != player.getPlayerId()) && player.intersect(projectile)) {
						
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