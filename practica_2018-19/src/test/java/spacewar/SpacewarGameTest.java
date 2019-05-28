package spacewar;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.tomcat.util.json.JSONParser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;

public class SpacewarGameTest {

	@BeforeClass
	public static void startServer() {
		Application.main(new String[] { "--server.port=9000" });
	}

	@Test
	public void testConnection() throws Exception {

		WebSocketClient ws = new WebSocketClient();
		ws.connect("ws://127.0.0.1:9000/spacewar");
		ws.disconnect();
	}

	@Test
	public void testJoin() throws Exception {
		AtomicReference<String> firstMsg = new AtomicReference<String>();

		WebSocketClient ws = new WebSocketClient();

		ws.onMessage((session, msg) -> {
			System.out.println("TestMessage: " + msg);
			firstMsg.compareAndSet(null, msg);
		});

		ws.connect("ws://127.0.0.1:9000/spacewar");
		System.out.println("Connected");
		Thread.sleep(1000);
		String msg = firstMsg.get();

		assertTrue("The fist message should contain 'join', but it is " + msg, msg.contains("JOIN"));
		ws.disconnect();
	}

	// Test para comprobar que dos se unen a una partida, uno se muere y los dos
	// pasan a resultados
	@Test
	public void testJoinStart() throws Exception {
		// EMPEZAR PARTIDA
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode mensaje = mapper.createObjectNode();

		AtomicReference<String> mensajeComienzo = new AtomicReference<String>();
		AtomicReference<String> mensajeMenu = new AtomicReference<String>();
		AtomicReference<String> mensajeLogin = new AtomicReference<String>();
		AtomicReference<String> mensajeComienzoPartida = new AtomicReference<String>();
		AtomicReference<String> mensajeUpdatePartidaJavier = new AtomicReference<String>();
		AtomicReference<String> mensajeUpdatePartidaIago = new AtomicReference<String>();

		WebSocketClient clienteJavier = new WebSocketClient();
		WebSocketClient clienteIago = new WebSocketClient();

		clienteJavier.onMessage((session, msg) -> {
			if (msg.contains("JOIN")) {
				System.out.println("Cliente Javier: " + msg);
				mensajeComienzo.compareAndSet(null, msg);
			} else if (msg.contains("LOGIN")) {
				System.out.println("Cliente Javier: " + msg);
				mensajeLogin.compareAndSet(null, msg);
			} else if (msg.contains("MENU STATE UPDATE")) {
				//System.out.println("Cliente Javier: " + msg);
				mensajeMenu.compareAndSet(null, msg);
			} else if (msg.contains("START GAME")) {
				System.out.println("Cliente Javier: " + msg);
				mensajeComienzoPartida.compareAndSet(null, msg);
			} else if (msg.contains("START GAME")) {
				System.out.println("Cliente Javier: " + msg);
				mensajeComienzoPartida.compareAndSet(null, msg);
			} else if (msg.contains("GAME STATE UPDATE")) {
				//System.out.println("Cliente Javier: " + msg);
				mensajeUpdatePartidaJavier.compareAndSet(null, msg);
			}
		});

		clienteIago.onMessage((session, msg) -> {
			if (msg.contains("JOIN")) {
				System.out.println("Cliente Iago: " + msg);
				mensajeComienzo.compareAndSet(null, msg);
			} else if (msg.contains("LOGIN")) {
				System.out.println("Cliente Iago: " + msg);
				mensajeLogin.compareAndSet(null, msg);
			} else if (msg.contains("MENU STATE UPDATE")) {
				//System.out.println("Cliente Iago: " + msg);
				mensajeMenu.compareAndSet(null, msg);
			} else if (msg.contains("START GAME")) {
				System.out.println("Cliente Iago: " + msg);
				mensajeComienzoPartida.compareAndSet(null, msg);
			} else if (msg.contains("GAME STATE UPDATE")) {
				//System.out.println("Cliente Iago: " + msg);
				mensajeUpdatePartidaIago.compareAndSet(null, msg);
			}
		});

		clienteJavier.connect("ws://127.0.0.1:9000/spacewar");
		clienteIago.connect("ws://127.0.0.1:9000/spacewar");

		System.out.println("WS Connected");
		System.out.println("WS2 Connected");
		Thread.sleep(1000);

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "javi");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("WS Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "iago");
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("WS2 Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("WS Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("WS2 Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "NEW SALA");
		mensaje.put("njugadores", 2);
		mensaje.put("modo", "Classic");
		mensaje.put("nombre", "javi");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("Creando una sala...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "JOIN SALA");
		mensaje.put("indiceSala", 0);
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("Uniendose a una sala...");
		Thread.sleep(1000);

		String msgJavier = mensajeUpdatePartidaJavier.get();
		String msgIago = mensajeUpdatePartidaIago.get();

		assertTrue("El mensaje debería ser GAME STATE UPDATE pero es: " + msgJavier + "\ny: " + msgIago,
				msgJavier.contains("GAME STATE UPDATE") && msgIago.contains("GAME STATE UPDATE"));

		clienteJavier.disconnect();
		clienteIago.disconnect();
		

	}

	@Test
	public void testJoinStartManually() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode mensaje = mapper.createObjectNode();

		AtomicReference<String> mensajeUpdatePartidaJavier = new AtomicReference<String>();
		AtomicReference<String> mensajeUpdatePartidaIago = new AtomicReference<String>();
		AtomicReference<String> mensajeUpdatePartidaHulio = new AtomicReference<String>();

		WebSocketClient clienteJavier = new WebSocketClient();
		WebSocketClient clienteIago = new WebSocketClient();
		WebSocketClient clienteHulio = new WebSocketClient();

		clienteJavier.onMessage((session, msg) -> {
			if (msg.contains("GAME STATE UPDATE")) {
				//System.out.println("Cliente Javier: " + msg);
				mensajeUpdatePartidaJavier.compareAndSet(null, msg);
			}
		});

		clienteIago.onMessage((session, msg) -> {
			if (msg.contains("GAME STATE UPDATE")) {
				//System.out.println("Cliente Iago: " + msg);
				mensajeUpdatePartidaIago.compareAndSet(null, msg);
			}
		});

		clienteHulio.onMessage((session, msg) -> {
			if (msg.contains("GAME STATE UPDATE")) {
				//System.out.println("Cliente Iago: " + msg);
				mensajeUpdatePartidaHulio.compareAndSet(null, msg);
			}
		});

		clienteJavier.connect("ws://127.0.0.1:9000/spacewar");
		clienteIago.connect("ws://127.0.0.1:9000/spacewar");
		clienteHulio.connect("ws://127.0.0.1:9000/spacewar");

		System.out.println("Javier Connected");
		System.out.println("Iago Connected");
		System.out.println("Hulio Connected");
		Thread.sleep(1000);

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "javi");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("Javier Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "iago");
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("Iago Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "hulio");
		clienteHulio.sendMessage(mensaje.toString());
		System.out.println("Hulio Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("Javier Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("Iago Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteHulio.sendMessage(mensaje.toString());
		System.out.println("Hulio Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "NEW SALA");
		mensaje.put("njugadores", 10);
		mensaje.put("modo", "Battle Royal");
		mensaje.put("nombre", "javi");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("Creando una sala...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "JOIN SALA");
		mensaje.put("indiceSala", 0);
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("Uniendose a una sala...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "JOIN SALA");
		mensaje.put("indiceSala", 0);
		clienteHulio.sendMessage(mensaje.toString());
		System.out.println("Uniendose a una sala...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "EMPEZAR PARTIDA");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("Comenzando partida...");
		Thread.sleep(1000);

		String msgJavier = mensajeUpdatePartidaJavier.get();
		String msgIago = mensajeUpdatePartidaIago.get();
		String msgHulio = mensajeUpdatePartidaHulio.get();

		assertTrue(
				"El mensaje debería ser GAME STATE UPDATE pero es: " + msgJavier + "\ny: " + msgIago + "\ny: "
						+ msgHulio,
				msgJavier.contains("GAME STATE UPDATE") && msgIago.contains("GAME STATE UPDATE")
						&& msgHulio.contains("GAME STATE UPDATE"));

		clienteJavier.disconnect();
		clienteIago.disconnect();
		clienteHulio.disconnect();
	}

	@Test
	public void testEndGame() throws Exception {
		// EMPEZAR PARTIDA

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode mensaje = mapper.createObjectNode();

		AtomicReference<String> mensajeUpdatePartidaJavier = new AtomicReference<String>();

		WebSocketClient clienteJavier = new WebSocketClient();
		WebSocketClient clienteIago = new WebSocketClient();

		clienteJavier.onMessage((session, msg) -> {
			if (msg.contains("END GAME")) {
				System.out.println("Cliente Javier: " + msg);
				mensajeUpdatePartidaJavier.compareAndSet(null, msg);
			}
		});

		clienteIago.onMessage((session, msg) -> {

		});

		clienteJavier.connect("ws://127.0.0.1:9000/spacewar");
		clienteIago.connect("ws://127.0.0.1:9000/spacewar");

		System.out.println("WS Connected");
		System.out.println("WS2 Connected");
		Thread.sleep(1000);

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "javi");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("WS Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "LOGIN");
		mensaje.put("text", "iago");
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("WS2 Logeando...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("WS Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "ADD PLAYER");
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("WS2 Añadiendo jugador...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "NEW SALA");
		mensaje.put("njugadores", 2);
		mensaje.put("modo", "Classic");
		mensaje.put("nombre", "javi");
		clienteJavier.sendMessage(mensaje.toString());
		System.out.println("Creando una sala...");
		Thread.sleep(1000);

		mensaje = mapper.createObjectNode();

		mensaje.put("event", "JOIN SALA");
		mensaje.put("indiceSala", 0);
		clienteIago.sendMessage(mensaje.toString());
		System.out.println("Uniendose a una sala...");
		Thread.sleep(1000);

		clienteIago.disconnect();

		Thread.sleep(1000);

		String msgJavier = mensajeUpdatePartidaJavier.get();

		assertTrue("El mensaje debería ser END GAME pero es: " + msgJavier, msgJavier.contains("END GAME"));

		clienteJavier.disconnect();

	}
}
