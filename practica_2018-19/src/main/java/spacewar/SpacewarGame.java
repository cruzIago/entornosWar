package spacewar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SpacewarGame {

	public final static SpacewarGame INSTANCE = new SpacewarGame();
	private final static int FPS = 30;
	private final static int MAXSALAS = 10;
	private final static long TICK_DELAY = 1000 / FPS;
	public final static boolean DEBUG_MODE = true;
	public final static boolean VERBOSE_MODE = true;

	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// GLOBAL GAME ROOM
	public SalaObject[] salas = new SalaObject[MAXSALAS];
	private Set<String> nombres = ConcurrentHashMap.newKeySet();
	private Map<String, Player> players = new ConcurrentHashMap<>();
	private AtomicInteger numPlayers = new AtomicInteger();

	private SpacewarGame() {

	}

	// gestion de nombres en el servidor
	public boolean addNombre(String nombre) {
		return nombres.add(nombre);
	}
	
	public boolean createSala(int NJUGADORES, String MODOJUEGO, String NOMBRE) {
		int indiceSalaLibre=getSalaLibre();
		if (indiceSalaLibre==-1) {
			return false;
		}
		salas[indiceSalaLibre]=new SalaObject(NJUGADORES, MODOJUEGO, NOMBRE);
		return true;
	}
	
	//Comprueba el primer indice de salas que esté libre
	private int getSalaLibre() {
		for (int i=0;i<salas.length;i++) {
			if(salas[i]==null) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean removeNombre(String nombre) {
		return nombres.remove(nombre);
	}

	public void addPlayer(Player player) {
		players.put(player.getSession().getId(), player);

		int count = numPlayers.getAndIncrement();
		if (count == 0) {
			this.startMenuLoop();
		}
	}

	public Collection<Player> getPlayers() {
		return players.values();
	}

	public void removePlayer(Player player) {
		players.remove(player.getSession().getId());

		int count = this.numPlayers.decrementAndGet();
		if (count == 0) {
			this.stopMenuLoop();
		}
	}

	public void startMenuLoop() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopMenuLoop() {
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
	
	public void broadcastLostPlayer(String message) {
		for (Player player : getPlayers()) {
			try {
				player.getSession().sendMessage(new TextMessage(message.toString()));
			} catch (Throwable ex) {
				System.err.println("Execption sending message to player " + player.getSession().getId());
				ex.printStackTrace(System.err);
				this.removePlayer(player);
			}
		}
	}

	public void tick() {
		ObjectNode json=mapper.createObjectNode();
		ArrayNode arrayNodeSalas = mapper.createArrayNode();

		for (SalaObject sala : salas) {
			if(sala!=null) {
				ObjectNode jsonSala = mapper.createObjectNode();
				jsonSala.put("nPlayers", sala.getNumberPlayersWaiting());
				jsonSala.put("nombre", sala.getNombreSala());
				jsonSala.put("modoJuego", sala.getModoJuego());
				arrayNodeSalas.addPOJO(jsonSala);
			}
		}
			
		json.put("event", "MENU STATE UPDATE");
		json.putPOJO("salas",arrayNodeSalas);
		
		this.broadcast(json.toString());
	}

	public void handleCollision() {

	}
}
