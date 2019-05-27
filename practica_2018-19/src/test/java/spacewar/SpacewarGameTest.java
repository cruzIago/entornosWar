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
		AtomicReference<String> mensajeComienzo = new AtomicReference<String>();

		WebSocketClient ws = new WebSocketClient();
		ws.connect("ws://127.0.0.1:9000/spacewar");
		
		System.out.println("Connected");
		Thread.sleep(1000);

		ObjectNode mensaje=mapper.createObjectNode();
		mensaje.put("event", "NEW SALA");
		mensaje.put("njugadores",  2);
		mensaje.put("modo", "Classic");
		mensaje.put("nombre", "javi");
		ws.sendMessage(mensaje.toString());

		Thread.sleep(1000);
		ws.onMessage((session, msg) -> {
			try {
				System.out.println(msg);
			JsonNode node=mapper.readTree(msg);
			mensajeComienzo.compareAndSet(null, msg);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		});
		
		System.out.println(mensajeComienzo.get());
		
		ws.disconnect();
		

	}
}
