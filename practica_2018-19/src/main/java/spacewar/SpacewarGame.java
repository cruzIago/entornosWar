package spacewar;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
	private final static int MAXLINECHAT = 15;
	private final static long TICK_DELAY = 1000 / FPS;
	public final static boolean DEBUG_MODE = true;
	public final static boolean VERBOSE_MODE = true;
	public final static int MAXTHREADS = 100;
	public final int MAXPUNTUACIONES = 10;
	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// GLOBAL GAME ROOM
	public SalaObject[] salas = new SalaObject[MAXSALAS]; // necesitamos agilizar las lecturas y
															// las pocas escrituras estan protegidas con sinchronized,
															// para que estas no requieren copiar todo de nuevo
															// además, necesitamos controlar su tamaño maximo
	private Deque<String> chat = new ArrayDeque<String>();// necesitamos que esten ordenados por llegada y no usamos
															// blockindeque pues necesitamos controlar el tamaño
	private Set<String> nombres = ConcurrentHashMap.newKeySet();
	private Map<String, Player> players = new ConcurrentHashMap<>();
	private AtomicInteger numPlayers = new AtomicInteger();

	private SpacewarGame() {
	}

	// gestion chat
	public synchronized void insertChat(String chatLine) {
		if (chat.size() >= MAXLINECHAT) {
			chat.pollFirst();
		}
		chat.addLast(chatLine);
	}

	// Gestion salas
	public synchronized int createSala(int NJUGADORES, String MODOJUEGO, String NOMBRE, Player CREADOR) {
		int indiceSalaLibre = getSalaLibre();
		if (indiceSalaLibre != -1) {
			if (MODOJUEGO.equals("Classic")) {
				salas[indiceSalaLibre] = new classicSala(NJUGADORES, MODOJUEGO, NOMBRE, CREADOR, indiceSalaLibre);
			} else if (MODOJUEGO.equals("Battle Royal")) {
				salas[indiceSalaLibre] = new royaleSala(NJUGADORES, MODOJUEGO, NOMBRE, CREADOR, indiceSalaLibre);
			}
		}
		return indiceSalaLibre;
	}

	public synchronized boolean joinSalaMatchmaking(Player player) {
		ObjectNode msg = mapper.createObjectNode();
		for (SalaObject sala : salas) {
			if (sala != null && !sala.isInProgress()) {
				float media = sala.getMediaSala();
				float pMedia = player.getMedia();
				if (((0 <= media && media < 0.2f) && (0 <= pMedia && pMedia < 0.2f))
						|| ((0.2 <= media && media <= 0.4) && (0.2 <= pMedia && pMedia <= 0.4))
						|| ((0.4 < media && media <= 1) && (0.4 < pMedia && pMedia <= 1))) {
					try {
						msg.put("event", "MATCHMAKING SUCCESS");
						msg.put("indiceSala", sala.getIndice());
						player.getSession().sendMessage(new TextMessage(msg.toString()));
					} catch (IOException e) {
					}
					sala.joinSala(player);
					return true;
				}
			}
		}
		try {
			msg.put("event", "MATCHMAKING FAIL");
			player.getSession().sendMessage(new TextMessage(msg.toString()));
		} catch (IOException e) {
		}
		return false;
	}

	private int getSalaLibre() {// Comprueba el primer indice de salas que esté libre
		for (int i = 0; i < salas.length; i++) {
			if (salas[i] == null) {
				return i;
			}
		}
		return -1;
	}

	public void removeSala(String NOMBRE) {
		for (int i = 0; i < salas.length; i++) {
			if (salas[i] != null && salas[i].getCreador().equals(NOMBRE)) {
				salas[i].drainPermitsOfSala();
				for (Player player : salas[i].getPlayers()) {
					if (!player.getNombre().equals(salas[i].getCreador())) {
						try {
							ObjectNode msg = mapper.createObjectNode();
							msg.put("event", "CANCEL SALA BY HOST");
							msg.put("indiceSala", i);
							player.getSession().sendMessage(new TextMessage(msg.toString()));
						} catch (IOException e) {
						}
					}
				}
				salas[i] = null;
			}
		}
	}

	// gestion de nombres en el servidor
	public String addNombre(String nombre) {
		if (nombres.add(nombre)) {
			return nombre;
		}
		return "";
	}

	public boolean removeNombre(String nombre) {
		return nombres.remove(nombre);
	}

	// gestion jugadores en salas y mensajes al cliente
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
				removePlayer(player);
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
				removePlayer(player);
			}
		}
	}

	private ObjectNode putSalaNull() {
		ObjectNode jsonSala = mapper.createObjectNode();
		jsonSala.put("nPlayers", 0);
		jsonSala.put("nombre", "");
		jsonSala.put("modoJuego", "");
		jsonSala.put("inProgress", false);
		return jsonSala;
	}

	public void checkAndRemoveSalas() {
		for (int i = 0; i < salas.length; i++) {
			if (salas[i] != null && salas[i].getNumberPlayersWaiting() <= 0) {
				salas[i] = null;
			}
		}
	}

	public void tick() {
		ObjectNode json = mapper.createObjectNode();
		ArrayNode arrayNodeSalas = mapper.createArrayNode();
		ArrayNode arrayNodeChat = mapper.createArrayNode();
		ArrayNode arrayNodePuntuaciones = mapper.createArrayNode();

		for (SalaObject sala : salas) {
			if (sala != null) {
				if (sala.getNumberPlayersWaiting() > 0) {
					ObjectNode jsonSala = mapper.createObjectNode();
					jsonSala.put("nPlayers", sala.getNumberPlayersWaiting());
					jsonSala.put("nombre", sala.getNombreSala());
					jsonSala.put("modoJuego", sala.getModoJuego());
					jsonSala.put("inProgress", sala.isInProgress());
					jsonSala.put("creador", sala.getCreador());
					arrayNodeSalas.addPOJO(jsonSala);
				} else {
					arrayNodeSalas.addPOJO(putSalaNull());
				}
			} else {
				arrayNodeSalas.addPOJO(putSalaNull());
			}
		}

		checkAndRemoveSalas();

		Iterator<String> chatI = chat.iterator();
		for (int i = 0; i < MAXLINECHAT; i++) {
			if (chatI.hasNext()) {
				arrayNodeChat.add(chatI.next());
			} else {
				arrayNodeChat.add("");
			}
		}
		// Para mostrar las puntuaciones en el menu
		Player[] play = getBetterPlayers();
		for(int i=0;i<MAXPUNTUACIONES;i++) {
			if(play[i]!=null) {
			ObjectNode jsonPuntuacion = mapper.createObjectNode();
			jsonPuntuacion.put("pos",i);
			jsonPuntuacion.put("nombreJugador", play[i].getNombre());
			jsonPuntuacion.put("media", play[i].getMedia());
			arrayNodePuntuaciones.addPOJO(jsonPuntuacion);
			}
		}

		json.put("event", "MENU STATE UPDATE");
		json.putPOJO("salas", arrayNodeSalas);
		json.putPOJO("chat", arrayNodeChat);
		json.putPOJO("puntuaciones", arrayNodePuntuaciones);
		this.broadcast(json.toString());
	}

	public Player[] getBetterPlayers() {
		ArrayList<Player> play = new ArrayList<Player>(players.values());
		Collections.sort(play, new Comparator<Player>() {
		    @Override
		    public int compare(Player p, Player p1) {
		        return Float.compare(p1.getMedia(), p.getMedia());
		    }
		});
		
		Player []mejores=new Player[MAXPUNTUACIONES];
		for(int i=0;i<MAXPUNTUACIONES;i++) {
			if(play.size()>i) {
			mejores[i]=play.get(i);
			}
		}
		return mejores;
	}

	public void handleCollision() {

	}
}
