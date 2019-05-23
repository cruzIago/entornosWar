package spacewar;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WebsocketGameHandler extends TextWebSocketHandler {

	private SpacewarGame game = SpacewarGame.INSTANCE;
	private static final String PLAYER_ATTRIBUTE = "PLAYER";
	private ObjectMapper mapper = new ObjectMapper();
	private AtomicInteger playerId = new AtomicInteger(0);
	private AtomicInteger projectileId = new AtomicInteger(0);

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Player player = new Player(playerId.incrementAndGet(), session);
		session.getAttributes().put(PLAYER_ATTRIBUTE, player);

		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "JOIN");
		msg.put("id", player.getPlayerId());
		msg.put("shipType", player.getShipType());
		player.getSession().sendMessage(new TextMessage(msg.toString()));

		// game.addPlayer(player);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		try {
			JsonNode node = mapper.readTree(message.getPayload());
			ObjectNode msg = mapper.createObjectNode();
			Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);

			switch (node.get("event").asText()) {
			case "LOGIN":
				String nombreJugador = game.addNombre(node.get("text").asText());
				player.setNombre(nombreJugador);
				msg.put("event", "LOGIN");
				msg.put("nombreJugador", nombreJugador);
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
				
			case "ADD PLAYER":
				game.addPlayer(player);
				
			case "JOIN":
				msg.put("event", "JOIN");
				msg.put("id", player.getPlayerId());
				msg.put("shipType", player.getShipType());
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;
				
			/*case "NEW GAME":
				game.setGame(node.get("kind").asText());
				game
				break;*/
			//gestion chat
			case "CHAT":
				game.insertChat(node.get("text").asText());
				break;
				
			//gestion salas
			case "NEW SALA":
				int indiceSalaLibre = game.createSala(node.get("njugadores").asInt(),node.get("modo").asText(),node.get("nombre").asText(), player);
				if (indiceSalaLibre != -1) {
					Thread newThread = new Thread(()->game.salas[indiceSalaLibre].joinSala(player));
					game.threads.put(player.getNombre(), newThread);
					newThread.start();
					newThread.join(100);
				} else {
					msg.put("event", "SALAS LIMIT");
					player.getSession().sendMessage(new TextMessage(msg.toString()));
				}
				break;
				
			case "JOIN SALA":
				int indiceSala = node.get("indiceSala").asInt();
				Thread newJoinThread = new Thread(()->game.salas[indiceSala].joinSala(player));
				game.threads.put(player.getNombre(), newJoinThread);
				newJoinThread.start();
				break;
				
			case "CANCEL SALA":
				game.removeSala(node.get("creador").asText());
				break;
				
			case "JOIN ROOM":
				msg.put("event", "NEW ROOM");
				msg.put("room", "GLOBAL");
				/*msg.put("xBounds", game.salas[node.get("salaID").asInt()].getXBound());
				msg.put("yBounds", game.salas[node.get("salaID").asInt()].getYBound());*/
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				break;

			case "UPDATE MOVEMENT":
				player.loadMovement(node.path("movement").get("thrust").asBoolean(),
						node.path("movement").get("brake").asBoolean(),
						node.path("movement").get("rotLeft").asBoolean(),
						node.path("movement").get("rotRight").asBoolean());
				break;
				
			case "SHOOT":
				player.setTimeGame(node.get("gameTime").asInt());
				
				if(player.getTimeGame()>player.getBulletTime()) {
					player.setBulletTime(player.getTimeGame()+250);
					Projectile projectile = new Projectile(player, this.projectileId.incrementAndGet());
					game.salas[node.get("salaID").asInt()].addProjectile(projectile.getId(), projectile);
				}
				
				break;
			default:
				break;
			}

		} catch (Exception e) {
			System.err.println("Exception processing message " + message.getPayload());
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);
		game.removeNombre(player.getNombre());
		game.removePlayer(player);

		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "REMOVE PLAYER");
		msg.put("id", player.getPlayerId());
		game.broadcastLostPlayer(msg.toString());
	}
}
